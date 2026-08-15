package com.nexora.admin.vo;

import java.util.List;

/**
 * PDF 解析产物：抽取文本 + 页图列表
 */
public class QuestionPdfParseVO {

    private String text;

    private List<QuestionPdfPageVO> pages;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public List<QuestionPdfPageVO> getPages() {
        return pages;
    }

    public void setPages(List<QuestionPdfPageVO> pages) {
        this.pages = pages;
    }
}
