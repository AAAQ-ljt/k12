package com.nexora.service.impl;

import com.alibaba.fastjson2.JSON;
import com.nexora.component.RedisComponent;
import com.nexora.constants.Constants;
import com.nexora.dto.PictureBookTaskVO;
import com.nexora.exception.BusinessException;
import com.nexora.service.PictureBookTaskService;
import com.nexora.utils.StringTools;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 绘本生成异步任务业务实现：Redis 任务体持久化 + 队列解耦
 */
@Slf4j
@Service
public class PictureBookTaskServiceImpl implements PictureBookTaskService {

    /** 任务保留 2 小时，超时自动清理 */
    private static final long TASK_TTL_HOURS = 2;

    @Resource
    private RedisComponent redisComponent;

    @Override
    public PictureBookTaskVO submit(String userId, String stage, String topic) {
        Date now = new Date();
        PictureBookTaskVO task = new PictureBookTaskVO();
        task.setTaskId(UUID.randomUUID().toString().replace("-", ""));
        task.setUserId(userId);
        task.setStage(stage);
        task.setTopic(topic);
        task.setStatus("PENDING");
        task.setCurrent(0);
        task.setTotal(0);
        task.setCreateTime(now);
        task.setUpdateTime(now);
        save(task);
        redisComponent.leftPush(Constants.REDIS_KEY_PICTURE_BOOK_TASK_QUEUE, task.getTaskId());
        return task;
    }

    @Override
    public PictureBookTaskVO get(String userId, String taskId) {
        if (StringTools.isEmpty(taskId)) {
            throw new BusinessException("任务ID不能为空");
        }
        return load(userId, taskId);
    }

    @Override
    public PictureBookTaskVO loadInternal(String taskId) {
        String json = redisComponent.getString(Constants.REDIS_KEY_PICTURE_BOOK_TASK_PREFIX + taskId);
        if (StringTools.isEmpty(json)) {
            return null;
        }
        return JSON.parseObject(json, PictureBookTaskVO.class);
    }

    @Override
    public void update(PictureBookTaskVO task) {
        if (task == null || StringTools.isEmpty(task.getTaskId())) {
            return;
        }
        task.setUpdateTime(new Date());
        save(task);
    }

    @Override
    public void failInterruptedTasks() {
        Set<String> queued = new HashSet<>();
        List<Object> queueMembers = redisComponent.listMembers(Constants.REDIS_KEY_PICTURE_BOOK_TASK_QUEUE);
        for (Object member : queueMembers) {
            if (member != null) {
                queued.add(member.toString());
            }
        }
        for (String key : redisComponent.keys(Constants.REDIS_KEY_PICTURE_BOOK_TASK_PREFIX + "*")) {
            // 跳过队列列表本身的 key（picturebook:task:queue 匹配前缀通配）
            if (Constants.REDIS_KEY_PICTURE_BOOK_TASK_QUEUE.equals(key)) {
                continue;
            }
            String taskId = key.substring(Constants.REDIS_KEY_PICTURE_BOOK_TASK_PREFIX.length());
            // 仍在队列中的任务会由消费者正常执行，不处理
            if (queued.contains(taskId)) {
                continue;
            }
            PictureBookTaskVO task = loadInternal(taskId);
            if (task == null || "COMPLETED".equals(task.getStatus()) || "FAILED".equals(task.getStatus())) {
                continue;
            }
            String previous = task.getStatus();
            task.setStatus("FAILED");
            task.setMessage("任务被服务重启中断，请重新生成");
            update(task);
            log.info("绘本孤儿任务已标记失败 taskId={} 原状态={}", taskId, previous);
        }
    }

    private void save(PictureBookTaskVO task) {
        redisComponent.setString(Constants.REDIS_KEY_PICTURE_BOOK_TASK_PREFIX + task.getTaskId(),
                JSON.toJSONString(task), TASK_TTL_HOURS, TimeUnit.HOURS);
    }

    private PictureBookTaskVO load(String userId, String taskId) {
        String json = redisComponent.getString(Constants.REDIS_KEY_PICTURE_BOOK_TASK_PREFIX + taskId);
        if (StringTools.isEmpty(json)) {
            throw new BusinessException("任务不存在或已过期");
        }
        PictureBookTaskVO task = JSON.parseObject(json, PictureBookTaskVO.class);
        if (task == null || !userId.equals(task.getUserId())) {
            throw new BusinessException("任务不存在或无权查看");
        }
        return task;
    }
}