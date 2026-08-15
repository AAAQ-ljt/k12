package com.nexora.admin.controller;

import com.nexora.controller.ABaseController;
import com.nexora.entity.vo.LearningOverviewVO;
import com.nexora.entity.vo.ResponseVO;
import com.nexora.mappers.LearningAnalysisMapper;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 学习分析：课程进度 / 练习正确率 / 个人知识库活跃 / AI 对话量
 */
@RestController
@RequestMapping("/learningAnalysis")
public class LearningAnalysisController extends ABaseController {

    @Resource
    private LearningAnalysisMapper learningAnalysisMapper;

    @GetMapping("/overview")
    public ResponseVO<LearningOverviewVO> overview() {
        return getSuccessResponseVO(learningAnalysisMapper.selectOverview());
    }
}
