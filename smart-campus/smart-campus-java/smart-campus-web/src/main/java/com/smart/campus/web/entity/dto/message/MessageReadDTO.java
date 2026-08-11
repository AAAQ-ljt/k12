package com.smart.campus.web.entity.dto.message;

import jakarta.validation.constraints.NotNull;

public class MessageReadDTO {

    @NotNull(message = "消息ID不能为空")
    private Long messageId;

    public Long getMessageId() {
        return messageId;
    }

    public void setMessageId(Long messageId) {
        this.messageId = messageId;
    }
}
