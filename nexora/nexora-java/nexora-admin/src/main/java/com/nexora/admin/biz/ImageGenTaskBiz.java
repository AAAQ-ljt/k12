package com.nexora.admin.biz;

import com.alibaba.fastjson2.JSON;
import com.nexora.admin.dto.ImageGenTaskVO;
import com.nexora.component.ImageProviderRouter;
import com.nexora.component.RedisComponent;
import com.nexora.constants.Constants;
import com.nexora.exception.BusinessException;
import com.nexora.utils.StringTools;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 模型测试文生图异步任务业务：Redis 任务体持久化 + 队列解耦 + 全局单任务锁互斥。
 * 前端提交后凭 taskId 轮询，切换页面不丢任务状态。
 */
@Service
public class ImageGenTaskBiz {

    /** 任务保留 2 小时，超时自动清理 */
    private static final long TASK_TTL_HOURS = 2;

    /** 运行锁保留 15 分钟（生图含重试最长约 12 分钟，余量充足；超时自动释放兜底） */
    private static final long RUNNING_TTL_SECONDS = 900;

    @Resource
    private RedisComponent redisComponent;

    @Resource
    private ImageProviderRouter imageProviderRouter;

    /**
     * 提交生图测试任务；已有进行中任务时直接返回该任务（前端继续轮询即可）
     */
    public ImageGenTaskVO submit(String prompt) {
        if (StringTools.isEmpty(prompt)) {
            throw new BusinessException("请输入画面描述");
        }
        String runningTaskId = redisComponent.getString(Constants.REDIS_KEY_MODEL_IMAGE_RUNNING);
        if (!StringTools.isEmpty(runningTaskId)) {
            ImageGenTaskVO existing = loadInternal(runningTaskId);
            if (existing != null && isActive(existing)) {
                return existing;
            }
            redisComponent.removeKey(Constants.REDIS_KEY_MODEL_IMAGE_RUNNING);
        }

        Date now = new Date();
        ImageGenTaskVO task = new ImageGenTaskVO();
        task.setTaskId(UUID.randomUUID().toString().replace("-", ""));
        task.setPrompt(prompt.trim());
        task.setProvider(imageProviderRouter.currentCode());
        task.setStatus("PENDING");
        task.setCreateTime(now);
        task.setUpdateTime(now);
        save(task);
        redisComponent.setString(Constants.REDIS_KEY_MODEL_IMAGE_RUNNING,
                task.getTaskId(), RUNNING_TTL_SECONDS, TimeUnit.SECONDS);
        redisComponent.leftPush(Constants.REDIS_KEY_MODEL_IMAGE_TASK_QUEUE, task.getTaskId());
        return task;
    }

    /**
     * 按 taskId 查询任务状态（前端轮询；任务不存在或已过期抛业务异常）
     */
    public ImageGenTaskVO get(String taskId) {
        if (StringTools.isEmpty(taskId)) {
            throw new BusinessException("任务ID不能为空");
        }
        ImageGenTaskVO task = loadInternal(taskId);
        if (task == null) {
            throw new BusinessException("任务不存在或已过期");
        }
        return task;
    }

    /**
     * 内部读取（消费者用，不存在返回 null）
     */
    public ImageGenTaskVO loadInternal(String taskId) {
        String json = redisComponent.getString(Constants.REDIS_KEY_MODEL_IMAGE_TASK_PREFIX + taskId);
        if (StringTools.isEmpty(json)) {
            return null;
        }
        return JSON.parseObject(json, ImageGenTaskVO.class);
    }

    /**
     * 更新任务体（刷新 updateTime 后写回）
     */
    public void update(ImageGenTaskVO task) {
        if (task == null || StringTools.isEmpty(task.getTaskId())) {
            return;
        }
        task.setUpdateTime(new Date());
        save(task);
    }

    /**
     * 释放运行锁（仅当锁内任务仍是本任务时）
     */
    public void releaseRunning(String taskId) {
        Object current = redisComponent.getObject(Constants.REDIS_KEY_MODEL_IMAGE_RUNNING);
        if (current != null && taskId.equals(current.toString())) {
            redisComponent.removeKey(Constants.REDIS_KEY_MODEL_IMAGE_RUNNING);
        }
    }

    /**
     * 任务是否仍在进行中（PENDING / GENERATING）
     */
    private boolean isActive(ImageGenTaskVO task) {
        return "PENDING".equals(task.getStatus()) || "GENERATING".equals(task.getStatus());
    }

    private void save(ImageGenTaskVO task) {
        redisComponent.setString(Constants.REDIS_KEY_MODEL_IMAGE_TASK_PREFIX + task.getTaskId(),
                JSON.toJSONString(task), TASK_TTL_HOURS, TimeUnit.HOURS);
    }
}