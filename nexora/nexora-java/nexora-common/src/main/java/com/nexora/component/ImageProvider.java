package com.nexora.component;

/**
 * 文生图供应商统一入口。
 * 由配置项 project.ai.image.provider 决定注入哪一个实现：dashscope / ark。
 */
public interface ImageProvider {

    /**
     * 生成一张图片，返回可直接下载的临时 URL；失败时返回 errorMessage。
     */
    ImageGenerateResult generate(String prompt);

    /**
     * 该供应商推荐的最大并发数：百炼限流严格，内部保持 1；豆包可 3 路并发。
     */
    default int maxConcurrency() {
        return 1;
    }
}
