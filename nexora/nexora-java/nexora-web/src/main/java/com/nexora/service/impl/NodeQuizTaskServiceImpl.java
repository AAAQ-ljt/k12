package com.nexora.service.impl;

import com.alibaba.fastjson2.JSON;
import com.nexora.component.KnowledgeMasteryComponent;
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

    @Resource
    private KnowledgeMasteryComponent knowledgeMasteryComponent;

    @Override
    public NodeQuizTaskVO submit(String userId, String itemId) {
        if (StringTools.isEmpty(itemId)) {
            throw new BusinessException("节点不能为空");
        }
        LearningPathItem item = learningPathItemService.getLearningPathItemByItemId(itemId);
        if (item == null || !userId.equals(item.getUserId())) {
            throw new BusinessException("节点不存在或无权操作");
        }
        if (isLockedForQuiz(userId, item)) {
            throw new BusinessException("该节点还未解锁，请先完成前置节点");
        }
        LearningPath path = learningPathComponent.requireOwnedPath(userId, item.getPathId());

        // 用户级互斥：同一学生同一时刻只允许一个快测出题任务在跑，防止连续点击产生多个任务。
        // 但必须校验是**同一个节点**：否则学生在出题途中点另一个节点，会拿到别人节点的题，
        // 提交后变化的是那个节点的掌握度（2026-10-08 修）
        String runningKey = runningKey(userId);
        String runningTaskId = redisComponent.getString(runningKey);
        if (!StringTools.isEmpty(runningTaskId)) {
            NodeQuizTaskVO existing = loadInternal(runningTaskId);
            if (existing != null) {
                if (itemId.equals(existing.getItemId())) {
                    return existing;
                }
                throw new BusinessException("还有一个节点快测正在出题，请等它出来再试（约十几秒）");
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
        // 反查：任务被丢弃时消费者凭 taskId 也能释放运行锁（否则该生 5 分钟内点快测一直拿不到新题）
        redisComponent.setString(Constants.REDIS_KEY_LEARNING_PATH_QUIZ_TASK_OWNER_PREFIX + task.getTaskId(),
                userId, RUNNING_TTL_SECONDS, TimeUnit.SECONDS);
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
        redisComponent.removeKey(Constants.REDIS_KEY_LEARNING_PATH_QUIZ_TASK_OWNER_PREFIX + taskId);
    }

    @Override
    public String ownerOf(String taskId) {
        if (StringTools.isEmpty(taskId)) {
            return null;
        }
        return redisComponent.getString(Constants.REDIS_KEY_LEARNING_PATH_QUIZ_TASK_OWNER_PREFIX + taskId);
    }

    private void save(NodeQuizTaskVO task) {
        redisComponent.setString(Constants.REDIS_KEY_LEARNING_PATH_QUIZ_TASK_PREFIX + task.getTaskId(),
                JSON.toJSONString(task), TASK_TTL_HOURS, TimeUnit.HOURS);
    }

    private String runningKey(String userId) {
        return Constants.REDIS_KEY_LEARNING_PATH_QUIZ_RUNNING_PREFIX + userId;
    }

    /**
     * 锁定节点是否禁止做快测：**已练过的知识点例外**（允许复习）。
     *
     * <p>主线前面某个节点回炉掉线时，后面的节点会被重新置为「未解锁」，但它们自己的复习计划还在跑，
     * 页面也会提示「该复习了」——若这里一律拒绝，那个提示就永远消不掉（2026-10-08 修）。
     */
    private boolean isLockedForQuiz(String userId, LearningPathItem item) {
        boolean locked = item.getStatus() != null && item.getStatus() == LearningPathComponent.ITEM_STATUS_LOCKED;
        if (!locked) {
            return false;
        }
        return !knowledgeMasteryComponent.hasRecord(userId, item.getKnowledgePointId());
    }
}