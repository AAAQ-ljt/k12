package com.smart.campus.admin.controller;

import com.smart.campus.admin.annotation.AdminPermission;
import com.smart.campus.admin.biz.DashboardAdminBiz;
import com.smart.campus.controller.ABaseController;
import com.smart.campus.entity.vo.ResponseVO;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@AdminPermission("dashboard")
@RestController("adminDashboardController")
@RequestMapping("/dashboard")
public class DashboardController extends ABaseController {

    private final DashboardAdminBiz dashboardAdminBiz;

    public DashboardController(DashboardAdminBiz dashboardAdminBiz) {
        this.dashboardAdminBiz = dashboardAdminBiz;
    }

    @RequestMapping("/loadDashboard")
    public ResponseVO loadDashboard() {
        return getSuccessResponseVO(dashboardAdminBiz.loadDashboard());
    }
}
