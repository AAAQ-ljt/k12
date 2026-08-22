package com.nexora.controller;

import com.nexora.annotation.GlobalInterceptor;
import com.nexora.entity.dto.TokenUserInfoDTO;
import com.nexora.entity.po.AiGenerationRecord;
import com.nexora.entity.vo.ResponseVO;
import com.nexora.exception.BusinessException;
import com.nexora.service.LearningPathService;
import com.nexora.utils.LoginUserContext;
import com.nexora.utils.StringTools;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 学生个性化学习路径 Controller：AI 生成 / 列表 / 删除
 */
@RestController
@RequestMapping("/learningPath")
@GlobalInterceptor(checkLogin = true)
public class LearningPathController extends ABaseController {

    @Resource
    private LearningPathService learningPathService;

    @PostMapping("/generate")
    public ResponseVO<AiGenerationRecord> generate() {
        TokenUserInfoDTO current = LoginUserContext.get();
        return getSuccessResponseVO(learningPathService.generate(current.getUserId(), current.getStage()));
    }

    @GetMapping("/myList")
    public ResponseVO<List<AiGenerationRecord>> myList() {
        return getSuccessResponseVO(learningPathService.myList(currentUserId()));
    }

    @DeleteMapping("/del")
    public ResponseVO<Void> del(@RequestParam String recordId) {
        learningPathService.delete(currentUserId(), recordId);
        return getSuccessResponseVO(null);
    }

    private String currentUserId() {
        TokenUserInfoDTO current = LoginUserContext.get();
        if (current == null || StringTools.isEmpty(current.getUserId())) {
            throw new BusinessException("登录状态异常");
        }
        return current.getUserId();
    }
}