package com.nexora.entity.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 《我的学习偏好》系统页（计划 C3）：只取渲染与保护需要的字段，避免改动生成式知识页 PO/XML。
 */
@Data
public class PreferencePageVO implements Serializable {

    /** 知识页 ID（会话锚点，写入 student_profile.preference_doc_id） */
    private String docId;

    /** 标题 */
    private String title;

    /** 正文（Markdown：顶部说明 + 规则段 + 自由段） */
    private String content;

    /** 向量状态：偏好页恒为 0（不入库） */
    private Integer vectorStatus;

    private Date createTime;

    private Date updateTime;
}
