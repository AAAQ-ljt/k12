package com.nexora.dto;

import java.util.Map;

/**
 * 用户意图结构化解析结果
 */
public class UserIntentDTO {

    /**
     * 意图编码，见 UserIntentEnum
     */
    private String intent;

    /**
     * 意图附带的结构化数据，如知识点、题目数量
     */
    private Map<String, Object> data;

    public String getIntent() {
        return intent;
    }

    public void setIntent(String intent) {
        this.intent = intent;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }
}
