package com.nexora.service;

import com.nexora.entity.po.AiGenerationRecord;

import java.util.List;

/**
 * 学生个性化学习路径业务：AI 结合学习档案与已学知识生成学习计划（存 ai_generation_record）
 */
public interface LearningPathService {

    /**
     * 生成并保存学习路径计划（每次生成新记录）
     */
    AiGenerationRecord generate(String userId, String stage);

    /**
     * 我的学习路径列表（新在前）
     */
    List<AiGenerationRecord> myList(String userId);

    /**
     * 删除学习路径记录
     */
    void delete(String userId, String recordId);
}