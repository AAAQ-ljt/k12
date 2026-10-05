package com.nexora.entity.vo;

/**
 * 资源在线预览产物元信息（管理端与学生端共用）。
 *
 * 产物约定：与原文件同级目录 &lt;文件名去扩展名&gt;_preview/ 下放
 * preview.pdf（转换所得 PDF）、page_001.jpg…（逐页图）、meta.json（本对象序列化）。
 * 页面数据只读 meta.json，不需要数据库字段（可从 file_path 推导），因此不涉及 DDL 变更。
 */
public class ResourcePreviewMetaVO {

    /** 生成中 */
    public static final String STATUS_GENERATING = "GENERATING";

    /** 已就绪 */
    public static final String STATUS_READY = "READY";

    /** 生成失败 */
    public static final String STATUS_FAILED = "FAILED";

    /** 生成状态：GENERATING / READY / FAILED */
    private String status;

    /** 页数（READY 时有值） */
    private Integer pages;

    /** 已尝试转换次数（超过上限不再自动重试） */
    private Integer attempts;

    /** 源文件字节数（生成时记录，用于前端提示"文件较大"） */
    private Long sourceSize;

    /** 生成完成时间 yyyy-MM-dd HH:mm:ss */
    private String generatedAt;

    /** 失败原因（FAILED 时有值） */
    private String message;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getPages() {
        return pages;
    }

    public void setPages(Integer pages) {
        this.pages = pages;
    }

    public Integer getAttempts() {
        return attempts;
    }

    public void setAttempts(Integer attempts) {
        this.attempts = attempts;
    }

    public Long getSourceSize() {
        return sourceSize;
    }

    public void setSourceSize(Long sourceSize) {
        this.sourceSize = sourceSize;
    }

    public String getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(String generatedAt) {
        this.generatedAt = generatedAt;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
