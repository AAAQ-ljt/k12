package com.nexora.service;

import com.nexora.dto.AnimationTaskVO;

/**
 * 动画讲解生成异步任务服务：Redis 任务体 + 队列解耦（与绘本任务同构）
 */
public interface AnimationTaskService {

    /**
     * 提交动画生成任务，返回任务体（PENDING 状态）
     */
    AnimationTaskVO submit(String userId, String stage, String topic);

    /**
     * 学生端查询任务状态（校验归属）
     */
    AnimationTaskVO get(String userId, String taskId);

    /**
     * 内部读取任务体（消费者使用，不校验归属，任务不存在返回 null）
     */
    AnimationTaskVO loadInternal(String taskId);

    /**
     * 更新任务体（消费进度/终态）
     */
    void update(AnimationTaskVO task);
}