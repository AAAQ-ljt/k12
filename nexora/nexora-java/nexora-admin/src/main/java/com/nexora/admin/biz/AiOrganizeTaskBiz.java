package com.nexora.admin.biz;

import com.alibaba.fastjson2.JSON;
import com.nexora.admin.dto.AiOrganizeTaskVO;
import com.nexora.component.RedisComponent;
import com.nexora.constants.Constants;
import com.nexora.entity.po.ResourceInfo;
import com.nexora.exception.BusinessException;
import com.nexora.service.ResourceInfoService;
import com.nexora.utils.StringTools;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * AI 文档整理异步任务业务：Redis 任务体 + 队列解耦 + 资源级运行锁互斥。
 * 同一 resourceId 运行中重复提交直接返回进行中任务，防止重复发起大模型整理；
 * 完成结果（整理稿/对照原文）落任务体，切页后按 taskId 轮询即可恢复，不丢状态。
 */
@Slf4j
@Service
public class AiOrganizeTaskBiz {

    /** 任务保留 2 小时，超时自动清理 */
    private static final long TASK_TTL_HOURS = 2;

    /** 运行锁保留 10 分钟（大文档整理可能耗时较长，余量充足；超时自动释放兜底） */
    private static final long RUNNING_TTL_SECONDS = 600;

    @Resource
    private RedisComponent redisComponent;

    @Resource
    private ResourceInfoService resourceInfoService;

    /**
     * 提交 AI 整理任务（校验官方资源）：运行中有未完成任务时直接返回该任务
     */
    public AiOrganizeTaskVO submit(String resourceId) {
        if (StringTools.isEmpty(resourceId)) {
            throw new BusinessException("请选择要整理的资源");
        }
        ResourceInfo resource = resourceInfoService.getResourceInfoByResourceId(resourceId);
        if (resource == null || resource.getStatus() == null || resource.getStatus() != 1) {
            throw new BusinessException("资源不存在或暂不可用");
        }
        if (resource.getOwnerId() != null) {
            throw new BusinessException("仅官方资源可进行 AI 整理");
        }

        String runningKey = runningKey(resourceId);
        String runningTaskId = redisComponent.getString(runningKey);
        if (!StringTools.isEmpty(runningTaskId)) {
            AiOrganizeTaskVO existing = loadInternal(runningTaskId);
            if (existing != null) {
                return existing;
            }
            redisComponent.removeKey(runningKey);
        }

        Date now = new Date();
        AiOrganizeTaskVO task = new AiOrganizeTaskVO();
        task.setTaskId(UUID.randomUUID().toString().replace("-", ""));
        task.setResourceId(resourceId);
        task.setResourceName(resource.getResourceName());
        task.setStage(resource.getStage());
        task.setStatus("PENDING");
        task.setMessage("任务已提交，等待执行");
        task.setCreateTime(now);
        task.setUpdateTime(now);
        save(task);
        redisComponent.setString(runningKey, task.getTaskId(), RUNNING_TTL_SECONDS, TimeUnit.SECONDS);
        redisComponent.leftPush(Constants.REDIS_KEY_AI_ORGANIZE_TASK_QUEUE, task.getTaskId());
        log.info("AI 文档整理任务已提交 resourceId={} taskId={}", resourceId, task.getTaskId());
        return task;
    }

    /**
     * 查询任务状态（前端重新进入页面时按 taskId 恢复）
     */
    public AiOrganizeTaskVO get(String taskId) {
        if (StringTools.isEmpty(taskId)) {
            throw new BusinessException("任务ID不能为空");
        }
        AiOrganizeTaskVO task = loadInternal(taskId);
        if (task == null) {
            throw new BusinessException("任务不存在或已过期");
        }
        return task;
    }

    /**
     * 内部读取任务体（消费者使用，任务不存在返回 null）
     */
    public AiOrganizeTaskVO loadInternal(String taskId) {
        String json = redisComponent.getString(Constants.REDIS_KEY_AI_ORGANIZE_TASK_PREFIX + taskId);
        if (StringTools.isEmpty(json)) {
            return null;
        }
        return JSON.parseObject(json, AiOrganizeTaskVO.class);
    }

    /**
     * 更新任务体（消费进度/终态）
     */
    public void update(AiOrganizeTaskVO task) {
        if (task == null || StringTools.isEmpty(task.getTaskId())) {
            return;
        }
        task.setUpdateTime(new Date());
        save(task);
    }

    /**
     * 任务到达终态后释放资源运行锁（仅当锁仍指向本任务时释放）
     */
    public void releaseRunning(String resourceId, String taskId) {
        String key = runningKey(resourceId);
        Object current = redisComponent.getObject(key);
        if (current != null && taskId.equals(current.toString())) {
            redisComponent.removeKey(key);
        }
    }

    private void save(AiOrganizeTaskVO task) {
        redisComponent.setString(Constants.REDIS_KEY_AI_ORGANIZE_TASK_PREFIX + task.getTaskId(),
                JSON.toJSONString(task), TASK_TTL_HOURS, TimeUnit.HOURS);
    }

    private String runningKey(String resourceId) {
        return Constants.REDIS_KEY_AI_ORGANIZE_RUNNING_PREFIX + resourceId;
    }
}