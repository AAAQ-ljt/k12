package com.nexora.admin.biz;

import com.nexora.admin.dto.TtsTestRequest;
import com.nexora.component.AiUsageRecordComponent;
import com.nexora.component.TtsProvider;
import com.nexora.component.TtsSynthesizeRequest;
import com.nexora.component.TtsSynthesizeResult;
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

import java.util.Base64;
import java.util.List;

/**
 * 模型连通性验证（开发/排障用）：DeepSeek 对话 / 百炼向量 / MiMo 语音合成。
 * 文生图测试为异步任务，见 {@link ImageGenTaskBiz} 与 ImageGenTaskConsumer。
 */
@Slf4j
@Service
public class ModelTestBiz {

    @Resource
    private ChatClient chatClient;

    @Resource
    private EmbeddingModel embeddingModel;

    @Resource
    private TtsProvider ttsProvider;

    @Resource
    private AiUsageRecordComponent aiUsageRecordComponent;

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
     * 3) TTS 语音合成连通性（同步返回 base64 音频，前端转 Blob 播放；成功计一次用量）
     */
    public TtsTestVO testTts(TtsTestRequest request) {
        if (request == null || StringTools.isEmpty(request.getText())) {
            throw new BusinessException("请输入合成文本");
        }
        if (request.getVoice() != null && !request.getVoice().isBlank()
                && !TtsProvider.isValidVoice(request.getVoice().trim())) {
            throw new BusinessException("不支持的音色");
        }
        long start = System.currentTimeMillis();
        try {
            TtsSynthesizeResult result = ttsProvider.synthesize(TtsSynthesizeRequest.of(
                    request.getText().trim(),
                    StringTools.isEmpty(request.getVoice()) ? null : request.getVoice().trim(),
                    request.getTone()));
            if (!result.success()) {
                throw new BusinessException(result.errorMessage() == null ? "语音合成失败" : result.errorMessage());
            }
            long costMs = System.currentTimeMillis() - start;
            aiUsageRecordComponent.recordTtsUsage(ttsProvider.modelId());
            TtsTestVO vo = new TtsTestVO();
            vo.setAudioBase64(Base64.getEncoder().encodeToString(result.audioData()));
            vo.setFormat(ttsProvider.audioFormat());
            vo.setModel(ttsProvider.modelId());
            vo.setVoice(ttsProvider.resolveVoice(null,
                    StringTools.isEmpty(request.getVoice()) ? null : request.getVoice().trim()));
            vo.setCostMs(costMs);
            vo.setAudioBytes(result.audioData().length);
            return vo;
        } catch (BusinessException e) {
            log.error("模型验证-语音合成失败: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("模型验证-语音合成异常", e);
            throw new BusinessException("语音合成调用失败：" + e.getMessage());
        }
    }

    /**
     * 向量采样格式化（前 5 维展示）
     */
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

    /** 语音合成测试结果：base64 音频（前端转 Blob 播放）+ 元信息 */
    public static class TtsTestVO {
        private String audioBase64;
        private String format;
        private String model;
        private String voice;
        private long costMs;
        private int audioBytes;

        public String getAudioBase64() {
            return audioBase64;
        }

        public void setAudioBase64(String audioBase64) {
            this.audioBase64 = audioBase64;
        }

        public String getFormat() {
            return format;
        }

        public void setFormat(String format) {
            this.format = format;
        }

        public String getModel() {
            return model;
        }

        public void setModel(String model) {
            this.model = model;
        }

        public String getVoice() {
            return voice;
        }

        public void setVoice(String voice) {
            this.voice = voice;
        }

        public long getCostMs() {
            return costMs;
        }

        public void setCostMs(long costMs) {
            this.costMs = costMs;
        }

        public int getAudioBytes() {
            return audioBytes;
        }

        public void setAudioBytes(int audioBytes) {
            this.audioBytes = audioBytes;
        }
    }
}
