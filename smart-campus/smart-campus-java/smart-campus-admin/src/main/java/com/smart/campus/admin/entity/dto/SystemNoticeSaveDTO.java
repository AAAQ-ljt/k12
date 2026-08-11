package com.smart.campus.admin.entity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class SystemNoticeSaveDTO implements Serializable {

    public interface Create {}

    public interface Update extends Create {}

    @NotBlank(message = "公告ID不能为空", groups = Update.class)
    private String noticeId;

    @NotBlank(message = "公告标题不能为空", groups = {Create.class, Update.class})
    private String noticeTitle;

    @NotBlank(message = "公告内容不能为空", groups = {Create.class, Update.class})
    private String noticeContent;

    @NotNull(message = "发布范围不能为空", groups = {Create.class, Update.class})
    private Integer targetType;

    private Integer isTop;

    private String targetIds;

    private List<String> targetIdList = new ArrayList<>();

    public String getNoticeId() {
        return noticeId;
    }

    public void setNoticeId(String noticeId) {
        this.noticeId = noticeId;
    }

    public String getNoticeTitle() {
        return noticeTitle;
    }

    public void setNoticeTitle(String noticeTitle) {
        this.noticeTitle = noticeTitle;
    }

    public String getNoticeContent() {
        return noticeContent;
    }

    public void setNoticeContent(String noticeContent) {
        this.noticeContent = noticeContent;
    }

    public Integer getTargetType() {
        return targetType;
    }

    public void setTargetType(Integer targetType) {
        this.targetType = targetType;
    }

    public Integer getIsTop() {
        return isTop;
    }

    public void setIsTop(Integer isTop) {
        this.isTop = isTop;
    }

    public String getTargetIds() {
        return targetIds;
    }

    public void setTargetIds(String targetIds) {
        this.targetIds = targetIds;
    }

    public List<String> getTargetIdList() {
        return targetIdList;
    }

    public void setTargetIdList(List<String> targetIdList) {
        this.targetIdList = targetIdList;
    }
}
