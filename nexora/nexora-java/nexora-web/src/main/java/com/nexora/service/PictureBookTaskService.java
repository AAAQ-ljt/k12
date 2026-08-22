package com.nexora.service;

import com.nexora.dto.PictureBookTaskVO;

/**
 * 绘本生成异步任务业务：提交入队 / 状态查询 / 状态更新（Redis 持久化）
 */
public interface PictureBookTaskService {

    /**
     * 提交绘本生成任务：创建任务(PENDING)并入队，立即返回；由消费者异步执行
     */
    PictureBookTaskVO submit(String userId, String stage, String topic);

    /**
     * 查询任务状态（校验归属）
     */
    PictureBookTaskVO get(String userId, String taskId);

    /**
     * 按任务ID读取（内部消费端使用，不校验归属；不存在返回 null）
     */
    PictureBookTaskVO loadInternal(String taskId);

    /**
     * 更新任务并落 Redis（消费者使用）
     */
    void update(PictureBookTaskVO task);
}