package com.nexora.service;

import com.nexora.dto.StudentWikiProfileDTO;
import com.nexora.entity.po.KnowledgeDoc;
import com.nexora.entity.po.UserWikiProfile;

import java.util.List;

/**
 * 学生个人知识页（wiki 层）业务：生成草稿 → 用户编辑 → 确认向量化
 */
public interface StudentWikiService {

    /**
     * 基于原始资源 AI 生成知识页草稿（不向量化）；已存在则覆盖为草稿并清理旧向量
     */
    KnowledgeDoc generateDraft(String userId, String resourceId);

    /**
     * 同步 AI 对话到知识页草稿（L3）：取一次问答的用户问题 + AI 回答生成草稿，同一消息重复同步则覆盖
     */
    KnowledgeDoc syncFromMessage(String userId, String messageId);

    /**
     * 同步课程教材到知识页草稿（主线 6）：AI 将课程绑定资源（名称+简介）整理为知识页草稿，按课程去重
     */
    KnowledgeDoc syncFromCourse(String userId, String stage, String courseId, String courseTitle);

    /**
     * 用户编辑知识页内容：编辑后回到草稿态（若原已确认则清理旧向量）
     */
    KnowledgeDoc updateDraft(String userId, String docId, String content);

    /**
     * 确认知识页并进入向量化队列（异步处理后将可被 AI 检索）
     */
    KnowledgeDoc confirm(String userId, String docId);

    /**
     * 获取知识页详情
     */
    KnowledgeDoc getDraft(String userId, String docId);

    /**
     * 知识页列表：resourceId 为空返回该学生全部知识页
     */
    List<KnowledgeDoc> listDrafts(String userId, String resourceId);

    /**
     * 删除知识页（级联清理向量）
     */
    void deleteDraft(String userId, String docId);

    /**
     * 删除资源时级联清理其知识页与向量
     */
    void cleanupByResource(String userId, String resourceId);

    /**
     * 获取学生学习档案（不存在返回 null）
     */
    UserWikiProfile getProfile(String userId);

    /**
     * 保存学生学习档案（不存在则新建）
     */
    UserWikiProfile saveProfile(String userId, StudentWikiProfileDTO dto);
}