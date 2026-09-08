package com.nexora.admin.component;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.nexora.constants.Constants;
import com.nexora.admin.vo.LessonQuizTaskVO;
import com.nexora.component.RedisComponent;
import com.nexora.entity.po.CourseLessonQuiz;
import com.nexora.exception.BusinessException;
import com.nexora.service.CourseLessonQuizService;
import com.nexora.utils.StringTools;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 课时测验 AI 出题异步任务组件（admin）：Redis 任务状态持久化 + 逐题生成上报进度。
 * 任务参数快照存任务体，后端线程池执行；admin 重启丢任务属已知限制（与绘本任务一致）。
 */
@Slf4j
@Component
public class LessonQuizTaskComponent {

    /** 任务保留 2 小时，超时自动清理 */
    private static final long TASK_TTL_HOURS = 2;

    /** AI 出题任务并发度 2（不同课时可同时出题） */
    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(2);

    @Resource
    private RedisComponent redisComponent;

    @Resource
    private LessonQuizAiComponent lessonQuizAiComponent;

    @Resource
    private CourseLessonQuizService courseLessonQuizService;

    /**
     * 提交 AI 出题任务：参数快照存 Redis，立即返回任务状态
     */
    public LessonQuizTaskVO start(LessonQuizTaskVO task) {
        Date now = new Date();
        task.setTaskId(UUID.randomUUID().toString().replace("-", ""));
        task.setStatus("PENDING");
        task.setStep("排队中");
        task.setGenerated(0);
        if (task.getTotal() == null) {
            task.setTotal(5);
        }
        task.setCreateTime(now);
        task.setUpdateTime(now);
        save(task);
        EXECUTOR.execute(() -> run(task));
        return task;
    }

    /**
     * 查询任务状态（不存在/过期抛业务异常）
     */
    public LessonQuizTaskVO get(String taskId) {
        if (StringTools.isEmpty(taskId)) {
            throw new BusinessException("任务ID不能为空");
        }
        LessonQuizTaskVO task = load(taskId);
        if (task == null) {
            throw new BusinessException("任务不存在或已过期");
        }
        return task;
    }

    private void run(LessonQuizTaskVO task) {
        try {
            task.setStatus("GENERATING");
            task.setStep("AI 正在出题");
            save(task);
            Date now = new Date();
            List<String> questionIds = new ArrayList<>();
            for (int i = 1; i <= task.getTotal(); i++) {
                LessonQuizAiComponent.QuizDraft draft = lessonQuizAiComponent.generateSingle(
                        task.getStage(), task.getGrade(), task.getKnowledgePointName(),
                        task.getTopic(), task.getDifficulty());
                String questionId = lessonQuizAiComponent.saveDraft(task.getStage(), task.getGrade(),
                        task.getDifficulty(), task.getKnowledgePointId(), draft, now);
                questionIds.add(questionId);
                task.setGenerated(i);
                task.setStep("已生成 " + i + "/" + task.getTotal() + " 题");
                save(task);
            }
            saveQuizConfig(task, questionIds, now);
            task.setStatus("SUCCESS");
            task.setStep("出题完成，已关联到课时测验");
            save(task);
            log.info("课时测验 AI 出题任务完成 taskId={} lessonId={} count={}", task.getTaskId(), task.getLessonId(), questionIds.size());
        } catch (Exception e) {
            log.error("课时测验 AI 出题任务失败 taskId={}", task.getTaskId(), e);
            task.setStatus("FAILED");
            task.setStep("出题失败");
            task.setMessage(e.getMessage() == null ? "AI 出题失败，请稍后重试" : e.getMessage());
            save(task);
        }
    }

    /** 题目分值自动均分 100 分（每题 base 分，前 remainder 题各补 1 分），写课时测验配置 */
    private void saveQuizConfig(LessonQuizTaskVO task, List<String> questionIds, Date now) {
        int n = questionIds.size();
        int base = 100 / n;
        int remainder = 100 - base * n;
        Map<String, Integer> questionScores = new LinkedHashMap<>();
        for (int i = 0; i < n; i++) {
            questionScores.put(questionIds.get(i), base + (i < remainder ? 1 : 0));
        }
        JSONObject config = new JSONObject();
        config.put("questionScores", questionScores);
        if (Boolean.TRUE.equals(task.getPartialCredit())) {
            config.put("partialCredit", true);
        }

        CourseLessonQuiz bean = new CourseLessonQuiz();
        bean.setCourseId(task.getCourseId());
        bean.setQuizMode(2);
        bean.setQuestionIds(JSON.toJSONString(questionIds));
        bean.setQuestionCount(n);
        bean.setDifficulty(task.getDifficulty());
        bean.setPassScore(task.getPassScore());
        bean.setUnlockNext(task.getUnlockNext());
        bean.setQuizConfig(config.toJSONString());
        bean.setStatus(1);
        bean.setUpdateTime(now);
        CourseLessonQuiz exist = courseLessonQuizService.getCourseLessonQuizByLessonId(task.getLessonId());
        if (exist == null) {
            bean.setLessonId(task.getLessonId());
            bean.setCreateTime(now);
            courseLessonQuizService.add(bean);
        } else {
            courseLessonQuizService.updateCourseLessonQuizByLessonId(bean, task.getLessonId());
        }
    }

    private void save(LessonQuizTaskVO task) {
        task.setUpdateTime(new Date());
        redisComponent.setString(Constants.REDIS_KEY_QUIZ_TASK_PREFIX + task.getTaskId(),
                JSON.toJSONString(task), TASK_TTL_HOURS, TimeUnit.HOURS);
    }

    private LessonQuizTaskVO load(String taskId) {
        String json = redisComponent.getString(Constants.REDIS_KEY_QUIZ_TASK_PREFIX + taskId);
        if (StringTools.isEmpty(json)) {
            return null;
        }
        return JSON.parseObject(json, LessonQuizTaskVO.class);
    }
}
