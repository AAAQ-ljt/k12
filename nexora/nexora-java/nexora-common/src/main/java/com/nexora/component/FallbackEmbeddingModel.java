package com.nexora.component;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingOptions;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;

/**
 * 向量模型兜底包装：主模型额度用完 / 被下线时自动切换到备用向量模型，业务代码无感知。
 *
 * <p>链路语义（经 2026-09-25 实测校准）：
 * <ul>
 *   <li>主模型：仅在「配额类错误」（429/402/403、欠费、限流）或「模型不可用」
 *       （model_not_found / model_not_supported）时才切换兜底；网络抖动等瞬时异常原样抛出，
 *       避免一次抖动就静默切换向量空间。</li>
 *   <li>兜底模型：任意异常都继续尝试下一个，全部失败时抛出<b>最后一个</b>异常（保留现场）。</li>
 *   <li>主模型是每次调用的起点：额度恢复（如次月重置）后自动回到主模型，无需重启。</li>
 * </ul>
 *
 * <p><b>注意向量空间问题</b>：不同向量模型的向量空间不互通。兜底生效后，
 * 新写入/新查询的向量由备用模型产生，与主模型历史向量的相似度会退化（功能可用、精度下降），
 * 主模型恢复后需要对历史知识重新向量化才能完全对齐。
 *
 * <p>兜底时显式带上与主模型相同的 dimensions，保证与 Elasticsearch 向量索引（1024 维）兼容：
 * qwen3.7-text-embedding 与 qwen3.7-text-embedding-flash 均实测支持 dimensions=1024。
 */
@Slf4j
public class FallbackEmbeddingModel implements EmbeddingModel {

    /** 配额类/模型不可用错误的关键词（响应体小写匹配） */
    private static final String[] FALLBACK_KEYWORDS = {
            "quota", "arrearage", "欠费", "余额", "throttl", "rate limit", "限流",
            "model_not_found", "model_not_supported", "unsupported model",
            "invalid model", "not found", "does not exist", "不存在"
    };

    private final EmbeddingModel delegate;
    private final List<String> fallbackModels;
    private final Integer dimensions;

    public FallbackEmbeddingModel(EmbeddingModel delegate, List<String> fallbackModels, Integer dimensions) {
        this.delegate = delegate;
        this.fallbackModels = List.copyOf(fallbackModels);
        this.dimensions = dimensions;
    }

    @Override
    public EmbeddingResponse call(EmbeddingRequest request) {
        try {
            return delegate.call(request);
        } catch (Exception primaryEx) {
            if (fallbackModels.isEmpty() || !shouldFallback(primaryEx)) {
                throw asRuntime(primaryEx);
            }
            log.warn("主向量模型 [{}] 调用失败，启动兜底链 {}: {}",
                    currentModel(request), fallbackModels, brief(primaryEx));
            Exception last = primaryEx;
            for (String model : fallbackModels) {
                try {
                    EmbeddingRequest retry = new EmbeddingRequest(
                            request.getInstructions(), rebuildOptions(model));
                    return delegate.call(retry);
                } catch (Exception ex) {
                    log.warn("兜底向量模型 [{}] 调用失败，尝试下一个: {}", model, brief(ex));
                    last = ex;
                }
            }
            throw asRuntime(last);
        }
    }

    /** call() 不声明受检异常，非运行时异常包一层保留原始堆栈再抛 */
    private RuntimeException asRuntime(Exception e) {
        return e instanceof RuntimeException re ? re : new IllegalStateException(e);
    }

    @Override
    public float[] embed(Document document) {
        return embed(document.getFormattedContent());
    }

    @Override
    public int dimensions() {
        return delegate.dimensions();
    }

    /**
     * 主模型：只有配额类/模型不可用错误才触发兜底。
     * 注意 Spring AI 会把 4xx 包成 NonTransientAiException（原始状态码与响应体在 message 里），
     * 不一定是 RestClientResponseException，所以整条异常链的 message 也要参与关键词匹配。
     */
    private boolean shouldFallback(Throwable error) {
        Throwable t = error;
        while (t != null) {
            if (t instanceof RestClientResponseException rc) {
                int status = rc.getStatusCode().value();
                if (status == 429 || status == 402 || status == 403 || status == 404) {
                    return true;
                }
                if (matchesKeywords(rc.getResponseBodyAsString())) {
                    return true;
                }
            }
            if (matchesKeywords(t.getMessage())) {
                return true;
            }
            t = t.getCause() == t ? null : t.getCause();
        }
        return false;
    }

    private boolean matchesKeywords(String text) {
        if (text == null || text.isBlank()) {
            return false;
        }
        String lower = text.toLowerCase();
        for (String kw : FALLBACK_KEYWORDS) {
            if (lower.contains(kw)) {
                return true;
            }
        }
        return false;
    }

    private EmbeddingOptions rebuildOptions(String model) {
        EmbeddingOptions.Builder builder = EmbeddingOptions.builder().model(model);
        if (dimensions != null) {
            builder.dimensions(dimensions);
        }
        return builder.build();
    }

    private String currentModel(EmbeddingRequest request) {
        if (request != null && request.getOptions() instanceof EmbeddingOptions opts && opts.getModel() != null) {
            return opts.getModel();
        }
        return "default";
    }

    private String brief(Throwable ex) {
        String msg = ex.getMessage();
        if (msg == null) {
            return ex.getClass().getSimpleName();
        }
        return msg.length() > 160 ? msg.substring(0, 160) + "..." : msg;
    }
}
