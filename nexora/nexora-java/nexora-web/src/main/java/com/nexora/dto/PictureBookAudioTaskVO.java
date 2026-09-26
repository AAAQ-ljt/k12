package com.nexora.dto;

/**
 * 绘本旁白合成任务状态（Redis 持久化，前端 2s 轮询）
 */
public class PictureBookAudioTaskVO {

    /** 任务ID */
    private String taskId;

    /** 绘本资源ID */
    private String resourceId;

    /** 页码（0 开始）；null 表示整本任务 */
    private Integer page;

    /** 本次合成使用的音色 */
    private String voice;

    /** RUNNING / COMPLETED / FAILED */
    private String status;

    /** 当前已完成页数（整本任务递增） */
    private int current;

    /** 本次需合成的总页数 */
    private int total;

    /** 进度说明 / 失败原因 */
    private String message;

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public String getVoice() {
        return voice;
    }

    public void setVoice(String voice) {
        this.voice = voice;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getCurrent() {
        return current;
    }

    public void setCurrent(int current) {
        this.current = current;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
