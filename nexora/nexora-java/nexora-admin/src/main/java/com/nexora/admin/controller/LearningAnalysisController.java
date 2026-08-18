package com.nexora.admin.controller;

import com.nexora.admin.biz.LearningAnalysisBiz;
import com.nexora.controller.ABaseController;
import com.nexora.entity.query.LearningUserQuery;
import com.nexora.entity.vo.LearningUserDetailVO;
import com.nexora.entity.vo.LearningOverviewVO;
import com.nexora.entity.vo.LearningUserSummaryVO;
import com.nexora.entity.vo.PaginationResultVO;
import com.nexora.entity.vo.ResponseVO;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 学习分析：总览 / 用户个人学习情况
 */
@RestController
@RequestMapping("/learningAnalysis")
public class LearningAnalysisController extends ABaseController {

    @Resource
    private LearningAnalysisBiz learningAnalysisBiz;

    @GetMapping("/overview")
    public ResponseVO<LearningOverviewVO> overview() {
        return getSuccessResponseVO(learningAnalysisBiz.overview());
    }

    @GetMapping("/loadDataList")
    public ResponseVO<PaginationResultVO<LearningUserSummaryVO>> loadDataList(LearningUserQuery query) {
        return getSuccessResponseVO(learningAnalysisBiz.userPage(query));
    }

    @GetMapping("/getStudentDetail")
    public ResponseVO<LearningUserDetailVO> getStudentDetail(@RequestParam String userId) {
        return getSuccessResponseVO(learningAnalysisBiz.userDetail(userId));
    }
}
