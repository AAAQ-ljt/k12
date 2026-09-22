package com.nexora.controller;

import com.nexora.annotation.GlobalInterceptor;
import com.nexora.entity.dto.TokenUserInfoDTO;
import com.nexora.entity.vo.ResponseVO;
import com.nexora.exception.BusinessException;
import com.nexora.service.KnowledgeMasteryBiz;
import com.nexora.utils.LoginUserContext;
import com.nexora.utils.StringTools;
import com.nexora.vo.KnowledgeMasteryOverviewVO;
import com.nexora.vo.LearningTrendVO;
import com.nexora.vo.ReviewLocateVO;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 学生端学习进度 Controller：我的知识点掌握度概览 + 学习趋势 + 待复习定位（真实进度数据）
 */
@RestController
@RequestMapping("/knowledgeMastery")
@GlobalInterceptor(checkLogin = true)
public class KnowledgeMasteryController extends ABaseController {

    @Resource
    private KnowledgeMasteryBiz knowledgeMasteryBiz;

    @GetMapping("/myOverview")
    public ResponseVO<KnowledgeMasteryOverviewVO> myOverview(
            @RequestParam(required = false) Integer limit) {
        TokenUserInfoDTO current = LoginUserContext.get();
        if (current == null || StringTools.isEmpty(current.getUserId())) {
            throw new BusinessException("登录状态异常");
        }
        return getSuccessResponseVO(knowledgeMasteryBiz.myOverview(current.getUserId(), limit));
    }

    /** 我的学习趋势（学习天数/连续打卡/近 7 天练习分布） */
    @GetMapping("/loadMyTrend")
    public ResponseVO<LearningTrendVO> loadMyTrend() {
        TokenUserInfoDTO current = LoginUserContext.get();
        if (current == null || StringTools.isEmpty(current.getUserId())) {
            throw new BusinessException("登录状态异常");
        }
        return getSuccessResponseVO(knowledgeMasteryBiz.loadMyTrend(current.getUserId()));
    }

    /** 待复习知识点定位：命中返回所在路径节点（跳路线做节点快测），未命中前端转 AI 助教 */
    @GetMapping("/locateReview")
    public ResponseVO<ReviewLocateVO> locateReview(@RequestParam String knowledgePointId) {
        TokenUserInfoDTO current = LoginUserContext.get();
        if (current == null || StringTools.isEmpty(current.getUserId())) {
            throw new BusinessException("登录状态异常");
        }
        return getSuccessResponseVO(knowledgeMasteryBiz.locateReview(current.getUserId(), knowledgePointId));
    }
}
