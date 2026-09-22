package com.nexora.service.impl;

import com.alibaba.fastjson2.JSON;
import com.nexora.component.LearningPathComponent;
import com.nexora.component.RedisComponent;
import com.nexora.constants.Constants;
import com.nexora.dto.NodeQuizTaskVO;
import com.nexora.entity.po.LearningPath;
import com.nexora.entity.po.LearningPathItem;
import com.nexora.exception.BusinessException;
import com.nexora.service.LearningPathItemService;
import com.nexora.service.NodeQuizTaskService;
import com.nexora.utils.StringTools;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 学习路径节点快测异步出题任务业务实现：Redis 任务体持久化 + 队列解耦 + 用户级运行锁互斥
 */
@Service
public class NodeQuizTaskServiceImpl implements NodeQuizTaskService {

    /** 任务保留 2 小时，超时自动清理 */
    private static final long TASK_TTL_HOURS = 2;

    /** 运行锁保留 5 分钟（出题一般十几秒，余量充足；超时自动释放兜底） */
    private static final long RUNNING_TTL_SECONDS = 300;

    @Resource
    private RedisComponent redisComponent;

    @Resource
    private LearningPathItemService learningPathItemService;

    @Resource
    private LearningPathComponent learningPathComponent;

    @Override
    public NodeQuizTaskVO submit(String userId, String itemId) {
        if (StringTools.isEmpty(itemId)) {
            throw new BusinessException("节点不能为空");
        }
        LearningPathItem item = learningPathItemService.getLearningPathItemByItemId(itemId);
        if (item == null || !userId.equals(item.getUserId())) {
            throw new BusinessException("节点不存在或无权操作");
        }
        if (item.getStatus() != null && item.getStatus() == LearningPathComponent.ITEM_STATUS_LOCKED) {
            throw new BusinessException("该节点还未解锁，请先完成前置节点");
        }
        LearningPath path = learningPathComponent.requireOwnedPath(userId, item.getPathId());

        // 用户级互斥：同一学生同一时刻只允许一个快测出题任务在跑，防止连续点击产生多个任务
        String runningKey = runningKey(userId);
        String runningTaskId = redisComponent.getString(runningKey);
        if (!StringTools.isEmpty(runningTaskId)) {
            NodeQuizTaskVO existing = loadInternal(runningTaskId);
            if (existing != null) {
                return existing;
            }
            redisComponent.removeKey(runningKey);
        }

        Date now = new Date();
        NodeQuizTaskVO task = new NodeQuizTaskVO();
        task.setTaskId(UUID.randomUUID().toString().replace("-", ""));
        task.setUserId(userId);
        task.setStage(path.getStage());
        task.setItemId(item.getItemId());
        task.setKnowledgePointId(item.getKnowledgePointId());
        task.setKnowledgePointName(item.getKnowledgePointName());
        task.setStatus("PENDING");
        task.setCreateTime(now);
        task.setUpdateTime(now);
        save(task);
        redisComponent.setString(runningKey, task.getTaskId(), RUNNING_TTL_SECONDS, TimeUnit.SECONDS);
        redisComponent.leftPush(Constants.REDIS_KEY_LEARNING_PATH_QUIZ_TASK_QUEUE, task.getTaskId());
        return task;
    }

    @Override
    public NodeQuizTaskVO get(String userId, String taskId) {
        if (StringTools.isEmpty(taskId)) {
            throw new BusinessException("任务ID不能为空");
        }
        String json = redisComponent.getString(Constants.REDIS_KEY_LEARNING_PATH_QUIZ_TASK_PREFIX + taskId);
        if (StringTools.isEmpty(json)) {
            throw new BusinessException("任务不存在或已过期");
        }
        NodeQuizTaskVO task = JSON.parseObject(json, NodeQuizTaskVO.class);
        if (task == null || !userId.equals(task.getUserId())) {
            throw new BusinessException("任务不存在或无权查看");
        }
        return task;
    }

    @Override
    public NodeQuizTaskVO loadInternal(String taskId) {
        String json = redisComponent.getString(Constants.REDIS_KEY_LEARNING_PATH_QUIZ_TASK_PREFIX + taskId);
        if (StringTools.isEmpty(json)) {
            return null;
        }
        return JSON.parseObject(json, NodeQuizTaskVO.class);
    }

    @Override
    public void update(NodeQuizTaskVO task) {
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

    private void save(NodeQuizTaskVO task) {
        redisComponent.setString(Constants.REDIS_KEY_LEARNING_PATH_QUIZ_TASK_PREFIX + task.getTaskId(),
                JSON.toJSONString(task), TASK_TTL_HOURS, TimeUnit.HOURS);
    }

    private String runningKey(String userId) {
        return Constants.REDIS_KEY_LEARNING_PATH_QUIZ_RUNNING_PREFIX + userId;
    }
}