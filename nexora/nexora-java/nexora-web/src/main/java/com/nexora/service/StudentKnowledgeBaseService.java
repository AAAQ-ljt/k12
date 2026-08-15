package com.nexora.service;

import com.nexora.vo.StudentStorageVO;

/**
 * 学生个人知识库业务
 */
public interface StudentKnowledgeBaseService {

    /**
     * 初始化个人知识库（已初始化则跳过）
     */
    boolean initIfAbsent(String ownerId);

    /**
     * 获取个人知识库存储信息与初始化状态
     */
    StudentStorageVO getStorageInfo(String ownerId);
}
