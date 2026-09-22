package com.nexora.service;

import com.nexora.dto.NodeQuizTaskVO;

/**
 * 学习路径节点快测异步出题任务服务：
 * Redis 任务体 + 队列解耦，用户级运行锁互斥（同一学生同一时刻只允许一个快测出题任务在跑）。
 */
public interface NodeQuizTaskService {

    /**
     * 提交快测出题任务（校验节点归属且未锁定）：运行中有未完成任务时直接返回该任务，不再重复出题
     */
    NodeQuizTaskVO submit(String userId, String itemId);

    /**
     * 学生端查询任务状态（校验归属）
     */
    NodeQuizTaskVO get(String userId, String taskId);

    /**
     * 内部读取任务体（消费者使用，不校验归属，任务不存在返回 null）
     */
    NodeQuizTaskVO loadInternal(String taskId);

    /**
     * 更新任务体（消费进度/终态）
     */
    void update(NodeQuizTaskVO task);

    /**
     * 任务到达终态后释放用户运行锁（仅当锁仍指向本任务时释放）
     */
    void releaseRunning(String userId, String taskId);
}