package com.nexora.admin.dto;

/**
 * 文生图供应商切换入参（管理端「系统设置 → 环境配置」）
 */
public class ImageProviderSwitchDTO {

    /** 供应商编码：dashscope / ark / gpt-image-2 */
    private String provider;

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }
}