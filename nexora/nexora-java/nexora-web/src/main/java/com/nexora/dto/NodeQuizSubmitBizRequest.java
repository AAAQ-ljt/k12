package com.nexora.dto;

/**
 * 节点快测出题任务提交请求
 */
public class NodeQuizSubmitBizRequest {

    /** 节点ID */
    private String itemId;

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }
}