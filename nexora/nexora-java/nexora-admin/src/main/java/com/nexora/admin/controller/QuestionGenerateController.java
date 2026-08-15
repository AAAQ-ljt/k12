package com.nexora.admin.controller;

import com.nexora.admin.biz.QuestionGenerateBiz;
import com.nexora.admin.dto.QuestionGenerateDTO;
import com.nexora.admin.dto.QuestionParseRequest;
import com.nexora.admin.vo.QuestionDraftVO;
import com.nexora.controller.ABaseController;
import com.nexora.entity.vo.ResponseVO;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * AI 出题 Controller
 */
@RestController
@RequestMapping("/questionGenerate")
public class QuestionGenerateController extends ABaseController {

    @Resource
    private QuestionGenerateBiz questionGenerateBiz;

    @PostMapping("/generate")
    public ResponseVO<String> generate(@RequestBody QuestionGenerateDTO dto) {
        return getSuccessResponseVO(questionGenerateBiz.generate(dto));
    }

    @PostMapping("/parseMd")
    public ResponseVO<List<QuestionDraftVO>> parseMd(@RequestBody QuestionParseRequest request) {
        return getSuccessResponseVO(questionGenerateBiz.parseMd(request == null ? null : request.getMarkdown()));
    }
}
