package com.nexora.service.impl;

import com.alibaba.fastjson2.JSON;
import com.nexora.component.RedisComponent;
import com.nexora.constants.Constants;
import com.nexora.dto.LearningPathGenTaskVO;
import com.nexora.entity.po.UserWikiProfile;
import com.nexora.exception.BusinessException;
import com.nexora.service.LearningPathGenTaskService;
import com.nexora.service.UserWikiProfileService;
import com.nexora.utils.StringTools;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 学习路径 AI 生成异步任务业务实现：Redis 任务体持久化 + 队列解耦 + 用户级运行锁互斥
 */
@Service
public class LearningPathGenTaskServiceImpl implements LearningPathGenTaskService {

    /** 任务保留 2 小时，超时自动清理 */
    private static final long TASK_TTL_HOURS = 2;

    /** 运行锁保留 5 分钟（路线生成一般几十秒，余量充足；超时自动释放兜底） */
    private static final long RUNNING_TTL_SECONDS = 300;

    @Resource
    private RedisComponent redisComponent;

    @Resource
    private UserWikiProfileService userWikiProfileService;

    @Override
    public LearningPathGenTaskVO submit(String userId, String stage) {
        // 与生成主流程保持一致：没有学习目标时模型只能凭教材/学段自由发挥，产出很泛，先引导填写档案
        UserWikiProfile profile = userWikiProfileService.getUserWikiProfileByUserId(userId);
        if (profile == null || StringTools.isEmpty(profile.getLearningGoal())) {
            throw new BusinessException("请先填写「学习目标」，AI 才能规划出贴合你的学习路径");
        }

        // 用户级互斥：同一学生同一时刻只允许一个路线生成任务在跑，防止连续点击重复生成
        String runningKey = runningKey(userId);
        String runningTaskId = redisComponent.getString(runningKey);
        if (!StringTools.isEmpty(runningTaskId)) {
            LearningPathGenTaskVO existing = loadInternal(runningTaskId);
            if (existing != null) {
                return existing;
            }
            redisComponent.removeKey(runningKey);
        }

        Date now = new Date();
        LearningPathGenTaskVO task = new LearningPathGenTaskVO();
        task.setTaskId(UUID.randomUUID().toString().replace("-", ""));
        task.setUserId(userId);
        task.setStage(stage);
        task.setStatus("PENDING");
        task.setCreateTime(now);
        task.setUpdateTime(now);
        save(task);
        redisComponent.setString(runningKey, task.getTaskId(), RUNNING_TTL_SECONDS, TimeUnit.SECONDS);
        redisComponent.leftPush(Constants.REDIS_KEY_LEARNING_PATH_GEN_TASK_QUEUE, task.getTaskId());
        return task;
    }

    @Override
    public LearningPathGenTaskVO get(String userId, String taskId) {
        if (StringTools.isEmpty(taskId)) {
            throw new BusinessException("任务ID不能为空");
        }
        String json = redisComponent.getString(Constants.REDIS_KEY_LEARNING_PATH_GEN_TASK_PREFIX + taskId);
        if (StringTools.isEmpty(json)) {
            throw new BusinessException("任务不存在或已过期");
        }
        LearningPathGenTaskVO task = JSON.parseObject(json, LearningPathGenTaskVO.class);
        if (task == null || !userId.equals(task.getUserId())) {
            throw new BusinessException("任务不存在或无权查看");
        }
        return task;
    }

    @Override
    public LearningPathGenTaskVO loadInternal(String taskId) {
        String json = redisComponent.getString(Constants.REDIS_KEY_LEARNING_PATH_GEN_TASK_PREFIX + taskId);
        if (StringTools.isEmpty(json)) {
            return null;
        }
        return JSON.parseObject(json, LearningPathGenTaskVO.class);
    }

    @Override
    public void update(LearningPathGenTaskVO task) {
        if (task == null || StringTools.isEmpty(task.getTaskId())) {
            return;
        }
        task.setUpdateTime(new Date());
        save(task);
    }

    @Override
    public void releaseRunning(String userId, String taskId) {
        String key = runningKey(userId);
        Object current = redisComponent.getObject(key);
        if (current != null && taskId.equals(current.toString())) {
            redisComponent.removeKey(key);
        }
    }

    private void save(LearningPathGenTaskVO task) {
        redisComponent.setString(Constants.REDIS_KEY_LEARNING_PATH_GEN_TASK_PREFIX + task.getTaskId(),
                JSON.toJSONString(task), TASK_TTL_HOURS, TimeUnit.HOURS);
    }

    private String runningKey(String userId) {
        return Constants.REDIS_KEY_LEARNING_PATH_GEN_RUNNING_PREFIX + userId;
    }
}