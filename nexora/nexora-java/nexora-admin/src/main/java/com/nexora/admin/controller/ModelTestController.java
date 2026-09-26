package com.nexora.admin.controller;

import com.nexora.admin.biz.ImageGenTaskBiz;
import com.nexora.admin.biz.ModelTestBiz;
import com.nexora.admin.dto.ImageGenTaskVO;
import com.nexora.admin.dto.TtsTestRequest;
import com.nexora.controller.ABaseController;
import com.nexora.entity.vo.ResponseVO;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 模型连通性验证 Controller（开发/排障用）
 */
@RestController
@RequestMapping("/modelTest")
public class ModelTestController extends ABaseController {

    @Resource
    private ModelTestBiz modelTestBiz;

    @Resource
    private ImageGenTaskBiz imageGenTaskBiz;

    @PostMapping("/chat")
    public ResponseVO<String> chat(@RequestBody Map<String, String> body) {
        return getSuccessResponseVO(modelTestBiz.testChat(body == null ? null : body.get("text")));
    }

    @PostMapping("/embedding")
    public ResponseVO<ModelTestBiz.EmbeddingTestVO> embedding(@RequestBody Map<String, String> body) {
        return getSuccessResponseVO(modelTestBiz.testEmbedding(body == null ? null : body.get("text")));
    }

    /** 提交文生图测试任务（异步，立即返回任务体；进行中重复提交返回原任务） */
    @PostMapping("/image")
    public ResponseVO<ImageGenTaskVO> image(@RequestBody Map<String, String> body) {
        return getSuccessResponseVO(imageGenTaskBiz.submit(body == null ? null : body.get("prompt")));
    }

    /** 轮询文生图测试任务状态（前端凭 taskId 恢复，切换页面不丢状态） */
    @GetMapping("/imageTask")
    public ResponseVO<ImageGenTaskVO> imageTask(@RequestParam String taskId) {
        return getSuccessResponseVO(imageGenTaskBiz.get(taskId));
    }

    /** 语音合成测试（同步返回 base64 音频，前端转 Blob 播放；1~10 秒） */
    @PostMapping("/tts")
    public ResponseVO<ModelTestBiz.TtsTestVO> tts(@RequestBody TtsTestRequest request) {
        return getSuccessResponseVO(modelTestBiz.testTts(request));
    }
}