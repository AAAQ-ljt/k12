package com.nexora.admin.dto;

/**
 * RAG 配置保存入参（管理端「系统设置 → RAG 配置」）
 */
public class RagConfigSaveDTO {

    /** 配置项 key（白名单内） */
    private String configKey;

    /** 配置值（字符串，后端按定义类型校验） */
    private String configValue;

    public String getConfigKey() {
        return configKey;
    }

    public void setConfigKey(String configKey) {
        this.configKey = configKey;
    }

    public String getConfigValue() {
        return configValue;
    }

    public void setConfigValue(String configValue) {
        this.configValue = configValue;
    }
}
