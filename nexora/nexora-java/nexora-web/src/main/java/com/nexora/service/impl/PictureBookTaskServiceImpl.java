package com.nexora.service.impl;

import com.alibaba.fastjson2.JSON;
import com.nexora.component.RedisComponent;
import com.nexora.constants.Constants;
import com.nexora.dto.PictureBookTaskVO;
import com.nexora.exception.BusinessException;
import com.nexora.service.PictureBookTaskService;
import com.nexora.utils.StringTools;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 绘本生成异步任务业务实现：Redis 任务体持久化 + 队列解耦
 */
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