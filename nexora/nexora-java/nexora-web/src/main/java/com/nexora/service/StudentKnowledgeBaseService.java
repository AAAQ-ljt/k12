package com.nexora.service;

import com.nexora.entity.po.ResourceDirectory;
import com.nexora.vo.StudentStorageVO;

/**
 * 学生个人知识库业务
 */
public interface StudentKnowledgeBaseService {

    /** 系统目录类型：raw 原始资料 / wiki 知识页 / attachments 附件 */
    String DIR_TYPE_RAW = "raw";
    String DIR_TYPE_WIKI = "wiki";
    String DIR_TYPE_ATTACHMENTS = "attachments";

    /**
     * 初始化个人知识库（已初始化则跳过）
     */
    boolean initIfAbsent(String ownerId);

    /**
     * 获取个人知识库存储信息与初始化状态；顺带幂等补齐 raw/wiki/attachments 系统目录
     */
    StudentStorageVO getStorageInfo(String ownerId);

    /**
     * 幂等补齐三层系统目录（存量账号自动获得三层，已存在则跳过）
     */
    void ensureSystemDirectories(String ownerId);

    /**
     * 获取指定类型的系统目录；不存在返回 null
     */
    ResourceDirectory getSystemDirectory(String ownerId, String dirType);

    /**
     * 解析上传默认目录：md/txt 文档归 raw，图片/视频/其他文档归 attachments；返回目录ID或 null
     */
    String resolveDefaultDirectoryId(String ownerId, String resourceType, String extension);

    /**
     * 校验目标目录是否允许存放该资源：raw 系统目录仅允许 md/txt 文档
     */
    void validateDirectoryState(String ownerId, String directoryId, String resourceType, String extension);
}