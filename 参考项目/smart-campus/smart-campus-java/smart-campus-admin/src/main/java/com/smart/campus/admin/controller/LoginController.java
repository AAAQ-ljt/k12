package com.smart.campus.admin.controller;

import com.smart.campus.admin.biz.AdminLoginBiz;
import com.smart.campus.controller.ABaseController;
import com.smart.campus.entity.dto.AdminLoginDTO;
import com.smart.campus.entity.vo.ResponseVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController("loginController")
@RequestMapping("/login")
public class LoginController extends ABaseController {

    private final AdminLoginBiz adminLoginBiz;

    public LoginController(AdminLoginBiz adminLoginBiz) {
        this.adminLoginBiz = adminLoginBiz;
    }

    @RequestMapping("/doLogin")
    public ResponseVO doLogin(@Valid AdminLoginDTO dto) {
        return getSuccessResponseVO(adminLoginBiz.doLogin(dto));
    }

    @RequestMapping("/getCaptcha")
    public ResponseVO getCaptcha() {
        return getSuccessResponseVO(adminLoginBiz.getCaptcha());
    }

    @RequestMapping("/getLoginInfo")
    public ResponseVO getLoginInfo() {
        return getSuccessResponseVO(adminLoginBiz.getLoginInfo());
    }

    @RequestMapping("/logout")
    public ResponseVO logout(HttpServletRequest request) {
        adminLoginBiz.logout(request.getHeader("adminToken"));
        return getSuccessResponseVO(null);
    }
}
