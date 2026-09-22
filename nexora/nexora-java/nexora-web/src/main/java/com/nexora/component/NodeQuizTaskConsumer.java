package com.nexora.component;

import com.nexora.constants.Constants;
import com.nexora.dto.NodeQuizTaskVO;
import com.nexora.service.NodeQuizTaskService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 学习路径节点快测异步出题任务消费者：
 * 状态机 PENDING → QUIZ_GENERATING → COMPLETED / FAILED；
 * 消费者依节点知识点现场出 3 题（QuizGenerateComponent，不依赖课程题库），
 * 题目 JSON 写回任务体供前端轮询完成后解析成答题卡。
 */
@Slf4j
@Component
public class NodeQuizTaskConsumer {

    @Resource
    private RedisComponent redisComponent;

    @Resource
    private NodeQuizTaskService nodeQuizTaskService;

    @Resource
    private QuizGenerateComponent quizGenerateComponent;

    @Scheduled(fixedDelay = 1000)
    public void consume() {
        Object taskIdObj = redisComponent.rightPop(Constants.REDIS_KEY_LEARNING_PATH_QUIZ_TASK_QUEUE);
        if (taskIdObj == null) {
            return;
        }
        String taskId = taskIdObj.toString();
        NodeQuizTaskVO task;
        try {
            task = nodeQuizTaskService.loadInternal(taskId);
        } catch (Exception e) {
            log.warn("节点快测任务读取失败 taskId={}", taskId, e);
            return;
        }
        if (task == null) {
            log.warn("节点快测任务不存在或已过期 taskId={}", taskId);
            return;
        }
        try {
            execute(task);
        } catch (Exception e) {
            log.error("节点快测出题执行异常 taskId={}", taskId, e);
            task.setStatus("FAILED");
            task.setMessage("出题失败：" + e.getMessage());
            nodeQuizTaskService.update(task);
        } finally {
            nodeQuizTaskService.releaseRunning(task.getUserId(), taskId);
        }
    }

    private void execute(NodeQuizTaskVO task) throws Exception {
        task.setStatus("QUIZ_GENERATING");
        task.setMessage("AI 正在为《" + task.getKnowledgePointName() + "》出题...");
        nodeQuizTaskService.update(task);

        QuizGenerateComponent.QuizScript script =
                quizGenerateComponent.generate(task.getStage(), task.getKnowledgePointName());
        String quizJson = script.toJson();

        task.setStatus("COMPLETED");
        task.setMessage("出题完成，可以开始作答");
        task.setQuizJson(quizJson);
        nodeQuizTaskService.update(task);
        log.info("节点快测出题完成 taskId={} userId={} topic={} 题数={}",
                task.getTaskId(), task.getUserId(), task.getKnowledgePointName(), script.questions().size());
    }
}