package com.nexora.service;

import com.nexora.dto.LearningPathGenTaskVO;

/**
 * 学习路径 AI 生成异步任务服务：
 * Redis 任务体 + 队列解耦，用户级运行锁互斥（同一学生同一时刻只允许一个路线生成任务在跑）。
 */
public interface LearningPathGenTaskService {

    /**
     * 提交路线生成任务：要求已填写学习目标（空档案生成的路线过于空泛，直接拦下）；
     * 运行中有未完成任务时直接返回该任务，不再重复生成
     */
    LearningPathGenTaskVO submit(String userId, String stage);

    /**
     * 学生端查询任务状态（校验归属）
     */
    LearningPathGenTaskVO get(String userId, String taskId);

    /**
     * 内部读取任务体（消费者使用，不校验归属，任务不存在返回 null）
     */
    LearningPathGenTaskVO loadInternal(String taskId);

    /**
     * 更新任务体（消费进度/终态）
     */
    void update(LearningPathGenTaskVO task);

    /**
     * 任务到达终态后释放用户运行锁（仅当锁仍指向本任务时释放）
     */
    void releaseRunning(String userId, String taskId);
}