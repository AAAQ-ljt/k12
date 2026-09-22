package com.nexora.component;

import com.nexora.constants.Constants;
import com.nexora.dto.LearningPathGenTaskVO;
import com.nexora.service.LearningPathGenTaskService;
import com.nexora.service.StudentLearningPathService;
import com.nexora.vo.LearningPathVO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 学习路径 AI 生成异步任务消费者：
 * 状态机 PENDING → PATH_GENERATING → COMPLETED / FAILED；
 * 消费者执行完整的「叙事层 + 结构层」生成（复用 StudentLearningPathService.generate），
 * 完成后把新路线标题与 pathId 写回任务体，前端轮询到终态后跳转详情页。
 */
@Slf4j
@Component
public class LearningPathGenTaskConsumer {

    @Resource
    private RedisComponent redisComponent;

    @Resource
    private LearningPathGenTaskService learningPathGenTaskService;

    @Resource
    private StudentLearningPathService studentLearningPathService;

    @Scheduled(fixedDelay = 1000)
    public void consume() {
        Object taskIdObj = redisComponent.rightPop(Constants.REDIS_KEY_LEARNING_PATH_GEN_TASK_QUEUE);
        if (taskIdObj == null) {
            return;
        }
        String taskId = taskIdObj.toString();
        LearningPathGenTaskVO task;
        try {
            task = learningPathGenTaskService.loadInternal(taskId);
        } catch (Exception e) {
            log.warn("学习路径生成任务读取失败 taskId={}", taskId, e);
            return;
        }
        if (task == null) {
            log.warn("学习路径生成任务不存在或已过期 taskId={}", taskId);
            return;
        }
        try {
            execute(task);
        } catch (Exception e) {
            log.error("学习路径生成任务执行异常 taskId={}", taskId, e);
            task.setStatus("FAILED");
            task.setMessage("路线生成失败：" + e.getMessage());
            learningPathGenTaskService.update(task);
        } finally {
            learningPathGenTaskService.releaseRunning(task.getUserId(), taskId);
        }
    }

    private void execute(LearningPathGenTaskVO task) throws Exception {
        task.setStatus("PATH_GENERATING");
        task.setMessage("AI 正在结合你的学习档案规划路线...");
        learningPathGenTaskService.update(task);

        LearningPathVO pathVO = studentLearningPathService.generate(task.getUserId(), task.getStage());

        task.setStatus("COMPLETED");
        task.setMessage("路线生成完成");
        task.setTitle(pathVO.getTitle());
        task.setPathId(pathVO.getPathId());
        learningPathGenTaskService.update(task);
        log.info("学习路径生成任务完成 taskId={} userId={} pathId={} 标题={}",
                task.getTaskId(), task.getUserId(), pathVO.getPathId(), pathVO.getTitle());
    }
}