package com.nexora.admin.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * 模型测试文生图异步任务体/状态（Redis 持久化，前端轮询；切页后凭 taskId 恢复）。
 *
 * 状态机：PENDING → GENERATING → COMPLETED / FAILED；
 * 全局单任务互斥：同一时刻只允许一个生图测试任务在跑，重复提交返回进行中任务。
 */
public class ImageGenTaskVO {

    /** 任务ID */
    private String taskId;

    /** 测试提示词 */
    private String prompt;

    /** 提交时生效的文生图供应商编码（dashscope / ark / gpt-image-2） */
    private String provider;

    /** 状态机：PENDING → GENERATING → COMPLETED / FAILED */
    private String status;

    /** 阶段说明 / 结果说明 / 失败信息 */
    private String message;

    /** 成功后返回的图片临时 URL（有效期较短，建议及时查看） */
    private String imageUrl;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}