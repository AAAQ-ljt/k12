package com.nexora.component;

import com.nexora.constants.Constants;
import com.nexora.dto.AnimationTaskVO;
import com.nexora.entity.po.ResourceInfo;
import com.nexora.service.AnimationTaskService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 动画讲解生成异步任务消费者：
 * 状态机 PENDING → ANIMATION_GENERATING → COMPLETED / FAILED；
 * 单次 LLM 生成分步 SVG 脚本后落库（生成记录 + 个人知识库 ANIMATION 资源），
 * 进度与终态持久化到 Redis 任务体供前端轮询。
 */
@Slf4j
@Component
public class AnimationTaskConsumer {

    @Resource
    private RedisComponent redisComponent;

    @Resource
    private AnimationTaskService animationTaskService;

    @Resource
    private AnimationScriptComponent animationScriptComponent;

    @Resource
    private AnimationSaveComponent animationSaveComponent;

    @Scheduled(fixedDelay = 1000)
    public void consume() {
        Object taskIdObj = redisComponent.rightPop(Constants.REDIS_KEY_ANIMATION_TASK_QUEUE);
        if (taskIdObj == null) {
            return;
        }
        String taskId = taskIdObj.toString();
        AnimationTaskVO task;
        try {
            task = animationTaskService.loadInternal(taskId);
        } catch (Exception e) {
            log.warn("动画任务读取失败 taskId={}", taskId, e);
            return;
        }
        if (task == null) {
            log.warn("动画任务不存在或已过期 taskId={}", taskId);
            return;
        }
        try {
            execute(task);
        } catch (Exception e) {
            log.error("动画生成任务执行异常 taskId={}", taskId, e);
            task.setStatus("FAILED");
            task.setMessage("动画生成失败：" + e.getMessage());
            animationTaskService.update(task);
        }
    }

    private void execute(AnimationTaskVO task) throws Exception {
        task.setStatus("ANIMATION_GENERATING");
        task.setMessage("AI 正在编排分步动画...");
        animationTaskService.update(task);

        AnimationScriptComponent.AnimationScript script =
                animationScriptComponent.generate(task.getStage(), task.getTopic());
        String scriptJson = script.toJson();

        ResourceInfo resource = animationSaveComponent.save(
                task.getUserId(), task.getStage(), script.title(), scriptJson);

        task.setStatus("COMPLETED");
        task.setMessage("动画生成完成");
        task.setTitle(script.title());
        task.setAnimationResourceId(resource.getResourceId());
        animationTaskService.update(task);
        log.info("动画异步任务完成 taskId={} userId={} title={} steps={}",
                task.getTaskId(), task.getUserId(), script.title(), script.steps().size());
    }
}