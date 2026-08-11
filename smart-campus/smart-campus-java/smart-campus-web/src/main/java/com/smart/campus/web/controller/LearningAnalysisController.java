package com.smart.campus.web.controller;

import com.smart.campus.controller.ABaseController;
import com.smart.campus.web.biz.LearningAnalysisWebBiz;
import com.smart.campus.web.entity.dto.analysis.LearningAnalysisQueryDTO;
import com.smart.campus.entity.vo.ResponseVO;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("webLearningAnalysisController")
@RequestMapping("/learningAnalysis")
public class LearningAnalysisController extends ABaseController {

    private final LearningAnalysisWebBiz learningAnalysisWebBiz;

    public LearningAnalysisController(LearningAnalysisWebBiz learningAnalysisWebBiz) {
        this.learningAnalysisWebBiz = learningAnalysisWebBiz;
    }

    @RequestMapping("/loadDashboard")
    public ResponseVO loadDashboard(LearningAnalysisQueryDTO dto) {
        return getSuccessResponseVO(learningAnalysisWebBiz.loadDashboard(dto));
    }
}
