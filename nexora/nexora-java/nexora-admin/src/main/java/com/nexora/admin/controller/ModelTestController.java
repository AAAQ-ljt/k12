package com.nexora.admin.controller;

import com.nexora.admin.biz.ModelTestBiz;
import com.nexora.controller.ABaseController;
import com.nexora.entity.vo.ResponseVO;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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

    @PostMapping("/chat")
    public ResponseVO<String> chat(@RequestBody Map<String, String> body) {
        return getSuccessResponseVO(modelTestBiz.testChat(body == null ? null : body.get("text")));
    }

    @PostMapping("/embedding")
    public ResponseVO<ModelTestBiz.EmbeddingTestVO> embedding(@RequestBody Map<String, String> body) {
        return getSuccessResponseVO(modelTestBiz.testEmbedding(body == null ? null : body.get("text")));
    }

    @PostMapping("/image")
    public ResponseVO<ModelTestBiz.ImageTestVO> image(@RequestBody Map<String, String> body) {
        return getSuccessResponseVO(modelTestBiz.testImage(body == null ? null : body.get("prompt")));
    }
}