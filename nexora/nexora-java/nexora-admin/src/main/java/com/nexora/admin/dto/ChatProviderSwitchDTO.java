package com.nexora.admin.dto;

/**
 * 对话模型供应商切换入参（管理端「系统设置 → 环境配置」）
 */
public class ChatProviderSwitchDTO {

    /** 供应商编码：deepseek / opencode-go */
    private String provider;

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }
}
