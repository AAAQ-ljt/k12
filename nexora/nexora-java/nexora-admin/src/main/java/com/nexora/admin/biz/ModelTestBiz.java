package com.nexora.admin.biz;

import com.nexora.component.ImageGenerateResult;
import com.nexora.component.ImageProvider;
import com.nexora.exception.BusinessException;
import com.nexora.utils.StringTools;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.embedding.Embedding;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingOptions;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 模型连通性验证（开发/排障用）：DeepSeek 对话 / 百炼向量 / 文生图供应商
 */
@Slf4j
@Service
public class ModelTestBiz {

    @Resource
    private ChatClient chatClient;

    @Resource
    private EmbeddingModel embeddingModel;

    @Resource
    private ImageProvider imageProvider;

    /**
     * 1) 对话模型连通性
     */
    public String testChat(String text) {
        if (StringTools.isEmpty(text)) {
            throw new BusinessException("请输入测试文本");
        }
        try {
            String content = chatClient.prompt()
                    .system("你是连通性测试助手，请简短回复“模型调用成功”，并复述用户的话。")
                    .user(text)
                    .call()
                    .content();
            if (content == null || content.isBlank()) {
                throw new BusinessException("模型返回空内容（模型可连通，但未返回文本）");
            }
            return content.trim();
        } catch (BusinessException e) {
            log.error("模型验证-对话失败: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("模型验证-对话异常", e);
            throw new BusinessException("对话模型调用失败：" + e.getMessage());
        }
    }

    /**
     * 2) 向量模型连通性
     */
    public EmbeddingTestVO testEmbedding(String text) {
        if (StringTools.isEmpty(text)) {
            throw new BusinessException("请输入测试文本");
        }
        try {
            EmbeddingResponse response = embeddingModel.call(new EmbeddingRequest(
                    List.of(text), EmbeddingOptions.builder().build()));
            Embedding embedding = response.getResult();
            if (embedding == null) {
                throw new BusinessException("向量模型未返回结果");
            }
            float[] values = embedding.getOutput();
            EmbeddingTestVO vo = new EmbeddingTestVO();
            vo.setDimension(values == null ? 0 : values.length);
            vo.setSample(values == null || values.length == 0 ? "" : formatSample(values));
            return vo;
        } catch (BusinessException e) {
            log.error("模型验证-向量失败: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("模型验证-向量异常", e);
            throw new BusinessException("向量模型调用失败：" + e.getMessage());
        }
    }

    /**
     * 3) 文生图模型连通性（当前注入的 ImageProvider 供应商；成功返回临时 URL，24 小时有效）
     */
    public ImageTestVO testImage(String prompt) {
        if (StringTools.isEmpty(prompt)) {
            throw new BusinessException("请输入画面描述");
        }
        try {
            ImageGenerateResult result = imageProvider.generate(prompt.trim());
            if (!result.success()) {
                throw new BusinessException(result.errorMessage() == null ? "文生图调用失败" : result.errorMessage());
            }
            ImageTestVO vo = new ImageTestVO();
            vo.setSuccess(true);
            vo.setUrl(result.imageUrl());
            vo.setMessage("图片生成成功（临时链接 24 小时内有效）");
            log.info("模型验证-文生图成功 provider={}", imageProvider.getClass().getSimpleName());
            return vo;
        } catch (BusinessException e) {
            log.error("模型验证-文生图失败: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("模型验证-文生图异常", e);
            throw new BusinessException("文生图调用失败：" + e.getMessage());
        }
    }

    private String formatSample(float[] values) {
        StringBuilder sb = new StringBuilder("[");
        int count = Math.min(5, values.length);
        for (int i = 0; i < count; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(String.format("%.4f", values[i]));
        }
        sb.append(count < values.length ? ", ..." : "");
        sb.append("]");
        return sb.toString();
    }

    public static class EmbeddingTestVO {
        private int dimension;
        private String sample;

        public int getDimension() {
            return dimension;
        }

        public void setDimension(int dimension) {
            this.dimension = dimension;
        }

        public String getSample() {
            return sample;
        }

        public void setSample(String sample) {
            this.sample = sample;
        }
    }

    public static class ImageTestVO {
        private boolean success;
        private String url;
        private String message;

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}
