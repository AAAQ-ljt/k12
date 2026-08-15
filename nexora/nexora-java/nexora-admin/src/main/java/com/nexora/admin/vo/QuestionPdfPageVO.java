package com.nexora.admin.vo;

/**
 * PDF 页图信息
 */
public class QuestionPdfPageVO {

    private Integer pageNo;

    private String imageUrl;

    public QuestionPdfPageVO() {
    }

    public QuestionPdfPageVO(Integer pageNo, String imageUrl) {
        this.pageNo = pageNo;
        this.imageUrl = imageUrl;
    }

    public Integer getPageNo() {
        return pageNo;
    }

    public void setPageNo(Integer pageNo) {
        this.pageNo = pageNo;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
