package com.nexora.component;

import com.nexora.utils.StringTools;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 绘本旁白组件：单页文本 → TtsProvider 合成音频字节 → 落盘（与插图同目录，.mp3）。
 * 音色解析由 TtsProvider 完成：用户显式指定（须命中白名单）> 学段默认（yml）> 全局默认。
 * 语音合成失败返回 null（调用方降级为无音频页），不阻断绘本主流程。
 */
@Slf4j
@Component
public class PictureBookAudioComponent {

    private final AtomicReference<String> lastFailure = new AtomicReference<>();

    @Resource
    private TtsProvider ttsProvider;

    @Resource
    private AiUsageRecordComponent aiUsageRecordComponent;

    @Value("${project.folder}")
    private String projectFolder;

    @Value("${resource.file-dir}")
    private String resourceFileDir;

    /** TTS 是否可用（未配置 Key 时调用方应整段跳过语音链路，不阻断出书） */
    public boolean isTtsAvailable() {
        return ttsProvider.isConfigured();
    }

    /** 供应商推荐最大并发数（分页旁白并发生成） */
    public int maxConcurrency() {
        return ttsProvider.maxConcurrency();
    }

    /**
     * 最终音色解析：显式指定优先，其次学段默认，最后全局默认。
     */
    public String resolveVoice(String stage, String voice) {
        return ttsProvider.resolveVoice(stage, voice);
    }

    /**
     * 生成一页旁白并落盘，返回相对文件路径；失败返回 null（调用方降级为无音频页）
     * 落盘目录：student/{邮箱目录}/picture-book/{月份}/（与插图同目录）
     */
    public String generatePageAudio(String email, String pageText, String voice, String bookTitle, int pageIndex) {
        lastFailure.set(null);
        try {
            TtsSynthesizeResult result = ttsProvider.synthesize(TtsSynthesizeRequest.of(pageText, voice, null));
            if (!result.success()) {
                lastFailure.set(result.errorMessage() == null ? "语音合成失败" : result.errorMessage());
                return null;
            }
            String saved = saveAudio(email, result.audioData());
            if (saved == null) {
                lastFailure.set("旁白保存失败：音频文件写入磁盘失败，请检查磁盘与目录权限");
            } else {
                // 语音按次计消耗（与生图同构：失败无产物不计数）
                aiUsageRecordComponent.recordTtsUsage(ttsProvider.modelId());
            }
            return saved;
        } catch (Exception e) {
            lastFailure.set("语音合成失败：" + (e.getMessage() == null ? "未知错误" : e.getMessage()));
            log.warn("绘本旁白合成失败 page={} title={}", pageIndex + 1, bookTitle, e);
            return null;
        }
    }

    /** 最近一次旁白失败原因（供外部记录到产物中，用户可见） */
    public String getLastFailureReason() {
        return lastFailure.get();
    }

    /** 删除一页旁白文件（重录覆盖/删书级联时调用；相对路径为空直接忽略） */
    public void deleteAudioFile(String audioFile) {
        if (StringTools.isEmpty(audioFile)) {
            return;
        }
        try {
            Files.deleteIfExists(Paths.get(projectFolder, audioFile));
        } catch (Exception e) {
            log.warn("删除绘本旁白文件失败 path={}", audioFile, e);
        }
    }

    private String saveAudio(String email, byte[] audioData) {
        String monthDir = LocalDate.now().toString().replace("-", "");
        Path targetDir = Paths.get(projectFolder, resourceFileDir,
                "student", emailDir(email), "picture-book", monthDir);
        String fileName = UUID.randomUUID().toString().replace("-", "") + ".mp3";
        try {
            Files.createDirectories(targetDir);
            Files.write(targetDir.resolve(fileName), audioData);
            return resourceFileDir + "/student/" + emailDir(email) + "/picture-book/" + monthDir + "/" + fileName;
        } catch (Exception e) {
            log.warn("绘本旁白写入失败 email={}", email, e);
            return null;
        }
    }

    /** 邮箱目录名：小写，@ 转 _at_ */
    private String emailDir(String email) {
        if (email == null || email.isBlank()) {
            return "unknown";
        }
        return email.trim().toLowerCase(java.util.Locale.ROOT).replace("@", "_at_");
    }
}
