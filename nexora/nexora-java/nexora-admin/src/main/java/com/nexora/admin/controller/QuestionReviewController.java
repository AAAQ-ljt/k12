package com.nexora.admin.controller;

import com.nexora.admin.dto.PracticeReviewSubmitDTO;
import com.nexora.constants.Constants;
import com.nexora.entity.dto.TokenUserInfoDTO;
import com.nexora.entity.query.PracticeReviewQuery;
import com.nexora.entity.vo.PaginationResultVO;
import com.nexora.entity.vo.PracticeReviewStatsVO;
import com.nexora.admin.biz.QuestionReviewBiz;
import com.nexora.controller.ABaseController;
import com.nexora.entity.vo.ResponseVO;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 答题批阅 Controller：主观题作答列表、统计、人工批阅。
 */
@RestController
@RequestMapping("/questionReview")
public class QuestionReviewController extends ABaseController {

    @Resource
    private QuestionReviewBiz questionReviewBiz;

    @GetMapping("/loadDataList")
    public ResponseVO<PaginationResultVO> loadDataList(PracticeReviewQuery query) {
        return getSuccessResponseVO(questionReviewBiz.loadList(query));
    }

    @GetMapping("/stats")
    public ResponseVO<PracticeReviewStatsVO> stats() {
        return getSuccessResponseVO(questionReviewBiz.stats());
    }

    @PostMapping("/review")
    public ResponseVO<Void> review(@RequestBody PracticeReviewSubmitDTO dto, HttpServletRequest request) {
        TokenUserInfoDTO userInfo = (TokenUserInfoDTO) request.getAttribute(Constants.ATTR_USER_INFO);
        String reviewerId = userInfo == null ? null : userInfo.getUsername();
        questionReviewBiz.review(dto, reviewerId);
        return getSuccessResponseVO(null);
    }
}
