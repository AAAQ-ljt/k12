package com.nexora.service;

import com.nexora.entity.po.AiGenerationRecord;
import com.nexora.vo.LearningPathSummaryVO;
import com.nexora.vo.LearningPathVO;

import java.util.List;

/**
 * 学生个性化学习路径业务：
 * - 结构层（节点三态/进度）落 learning_path / learning_path_item，由 common 的 LearningPathComponent 维护；
 * - 叙事层（目标/产出物/节奏/阶段/每节点任务与用时）写 ai_generation_record.content（带 pathId 索引，方案 A：不改表结构），读取时与节点按知识点名对齐；
 * - 生成前要求填写学习目标（空档案生成的路线过于空泛，直接拦下并引导补档案）。
 */
public interface StudentLearningPathService {

    /**
     * 生成学习路径（叙事层 + 结构层），返回完整详情
     */
    LearningPathVO generate(String userId, String stage);

    /**
     * 我的路线库（卡片列表：不含节点明细，新在前）
     */
    List<LearningPathSummaryVO> myList(String userId);

    /**
     * 单条路线详情（阶段分组节点 + 叙事层）
     */
    LearningPathVO detail(String userId, String pathId);

    /**
     * 删除路线（级联节点，并清理其叙事层快照）
     */
    void delete(String userId, String pathId);

    /**
     * 历史计划（旧版无叙事层的生成记录）
     */
    List<AiGenerationRecord> historyList(String userId);

    /**
     * 删除历史计划记录
     */
    void deleteHistory(String userId, String recordId);
}
