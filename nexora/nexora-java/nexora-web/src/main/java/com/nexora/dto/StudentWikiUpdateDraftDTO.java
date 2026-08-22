package com.nexora.dto;

/**
 * 学生知识页草稿更新入参
 */
public class StudentWikiUpdateDraftDTO {

    /**
     * 知识页ID
     */
    private String docId;

    /**
     * 编辑后的 Markdown 内容
     */
    private String content;

    public String getDocId() {
        return docId;
    }

    public void setDocId(String docId) {
        this.docId = docId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}