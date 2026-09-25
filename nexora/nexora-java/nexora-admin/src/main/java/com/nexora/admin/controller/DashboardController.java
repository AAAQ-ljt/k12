package com.nexora.admin.controller;

import com.nexora.admin.biz.DashboardBiz;
import com.nexora.controller.ABaseController;
import com.nexora.entity.vo.DashboardOverviewVO;
import com.nexora.entity.vo.ResponseVO;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 控制面板 Controller：工作台总览数据
 */
@RestController
@RequestMapping("/dashboard")
public class DashboardController extends ABaseController {

    @Resource
    private DashboardBiz dashboardBiz;

    /** 控制面板总览（统计卡 + 趋势 + 用户分布 + 最近会话 + 待办） */
    @GetMapping("/overview")
    public ResponseVO<DashboardOverviewVO> overview() {
        return getSuccessResponseVO(dashboardBiz.overview());
    }
}