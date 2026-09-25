package com.nexora.component;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

/**
 * 文生图供应商路由：按 system_config 表 image_provider 运行时切换（管理端保存即生效），
 * 表里未配置时回落启动配置 project.ai.image.provider，仍然非法时兜底 dashscope。
 *
 * 三个具体供应商（dashscope / ark / gpt-image-2）全部常驻注册，
 * 本路由作为 ImageProvider 的主实现（@Primary）供业务注入，generate / maxConcurrency 转发到当前供应商。
 */
@Slf4j
@Component
@Primary
public class ImageProviderRouter implements ImageProvider {

    private final ArkImageProvider arkImageProvider;
    private final DashscopeImageProvider dashscopeImageProvider;
    private final GptImage2Provider gptImage2Provider;
    private final SystemConfigComponent systemConfigComponent;
    private final AiUsageRecordComponent aiUsageRecordComponent;

    /** 启动配置默认供应商（表里无覆盖时生效） */
    @Value("${project.ai.image.provider:dashscope}")
    private String defaultProvider;

    public ImageProviderRouter(ArkImageProvider arkImageProvider,
                               DashscopeImageProvider dashscopeImageProvider,
                               GptImage2Provider gptImage2Provider,
                               SystemConfigComponent systemConfigComponent,
                               AiUsageRecordComponent aiUsageRecordComponent) {
        this.arkImageProvider = arkImageProvider;
        this.dashscopeImageProvider = dashscopeImageProvider;
        this.gptImage2Provider = gptImage2Provider;
        this.systemConfigComponent = systemConfigComponent;
        this.aiUsageRecordComponent = aiUsageRecordComponent;
    }

    @Override
    public ImageGenerateResult generate(String prompt) {
        String providerCode = currentCode();
        ImageGenerateResult result = current(providerCode).generate(prompt);
        // 生图按张计次（绘本逐页插图、单页补画、管理端生图测试都走这里）：失败无图产出，不计消耗
        if (result != null && result.success()) {
            aiUsageRecordComponent.recordImageUsage(providerCode);
        }
        return result;
    }

    @Override
    public int maxConcurrency() {
        return current(currentCode()).maxConcurrency();
    }

    /**
     * 当前生效的供应商编码（供管理端运行时信息展示）
     */
    public String currentCode() {
        String code = systemConfigComponent.getImageProviderValue(defaultProvider);
        return SystemConfigComponent.normalizeImageProvider(code);
    }

    private ImageProvider current(String code) {
        switch (code) {
            case SystemConfigComponent.PROVIDER_ARK:
                return arkImageProvider;
            case SystemConfigComponent.PROVIDER_GPT_IMAGE_2:
                return gptImage2Provider;
            case SystemConfigComponent.PROVIDER_DASHSCOPE:
            default:
                return dashscopeImageProvider;
        }
    }
}