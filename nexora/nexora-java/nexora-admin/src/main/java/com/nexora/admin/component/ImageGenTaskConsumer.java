package com.nexora.admin.component;

import com.nexora.admin.biz.ImageGenTaskBiz;
import com.nexora.admin.dto.ImageGenTaskVO;
import com.nexora.component.ImageGenerateResult;
import com.nexora.component.ImageProvider;
import com.nexora.component.RedisComponent;
import com.nexora.constants.Constants;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 模型测试文生图异步任务消费者：
 * 状态机 PENDING → GENERATING → COMPLETED / FAILED；
 * 生图走当前生效的 ImageProvider（供应商在提交时记录进任务体），结果写入 Redis 任务体供前端轮询。
 */
@Slf4j
@Component
public class ImageGenTaskConsumer {

    @Resource
    private RedisComponent redisComponent;

    @Resource
    private ImageGenTaskBiz imageGenTaskBiz;

    @Resource
    private ImageProvider imageProvider;

    @Scheduled(fixedDelay = 1000)
    public void consume() {
        Object taskIdObj = redisComponent.rightPop(Constants.REDIS_KEY_MODEL_IMAGE_TASK_QUEUE);
        if (taskIdObj == null) {
            return;
        }
        String taskId = taskIdObj.toString();
        ImageGenTaskVO task;
        try {
            task = imageGenTaskBiz.loadInternal(taskId);
        } catch (Exception e) {
            log.warn("生图测试任务读取失败 taskId={}", taskId, e);
            return;
        }
        if (task == null) {
            log.warn("生图测试任务不存在或已过期 taskId={}", taskId);
            return;
        }
        try {
            execute(task);
        } catch (Exception e) {
            log.error("生图测试执行异常 taskId={}", taskId, e);
            task.setStatus("FAILED");
            task.setMessage("生图测试失败：" + e.getMessage());
            imageGenTaskBiz.update(task);
        } finally {
            imageGenTaskBiz.releaseRunning(taskId);
        }
    }

    private void execute(ImageGenTaskVO task) {
        task.setStatus("GENERATING");
        task.setMessage("正在生成图片（供应商：" + task.getProvider() + "，通常 10-120 秒，请耐心等待）...");
        imageGenTaskBiz.update(task);

        ImageGenerateResult result = imageProvider.generate(task.getPrompt());
        if (!result.success()) {
            task.setStatus("FAILED");
            task.setMessage(result.errorMessage() == null ? "生图测试失败" : result.errorMessage());
        } else {
            task.setStatus("COMPLETED");
            task.setMessage("图片生成成功（临时链接有效期较短，请及时查看保存）");
            task.setImageUrl(result.imageUrl());
        }
        imageGenTaskBiz.update(task);
        log.info("生图测试任务完成 taskId={} status={} provider={}",
                task.getTaskId(), task.getStatus(), task.getProvider());
    }
}