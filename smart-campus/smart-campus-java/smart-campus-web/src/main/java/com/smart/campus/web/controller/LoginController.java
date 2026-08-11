package com.smart.campus.web.controller;

import com.smart.campus.controller.ABaseController;
import com.smart.campus.entity.dto.StudentLoginDTO;
import com.smart.campus.entity.vo.ResponseVO;
import com.smart.campus.web.biz.WebLoginBiz;
import com.smart.campus.web.entity.dto.auth.UpdatePasswordDTO;
import com.smart.campus.web.entity.dto.auth.UpdateProfileDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Validated
@RestController("webLoginController")
@RequestMapping("/login")
public class LoginController extends ABaseController {

    private final WebLoginBiz webLoginBiz;

    public LoginController(WebLoginBiz webLoginBiz) {
        this.webLoginBiz = webLoginBiz;
    }

    @RequestMapping("/doLogin")
    public ResponseVO doLogin(@Valid StudentLoginDTO dto) {
        return getSuccessResponseVO(webLoginBiz.doLogin(dto));
    }

    @RequestMapping("/getCaptcha")
    public ResponseVO getCaptcha() {
        return getSuccessResponseVO(webLoginBiz.getCaptcha());
    }

    @RequestMapping("/getLoginInfo")
    public ResponseVO getLoginInfo() {
        return getSuccessResponseVO(webLoginBiz.getLoginInfo());
    }

    @RequestMapping("/logout")
    public ResponseVO logout(HttpServletRequest request) {
        webLoginBiz.logout(request.getHeader("studentToken"));
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/updatePassword")
    public ResponseVO updatePassword(@Valid UpdatePasswordDTO dto) {
        webLoginBiz.updatePassword(dto);
        return getSuccessResponseVO(Boolean.TRUE);
    }

    @RequestMapping("/uploadAvatar")
    public ResponseVO uploadAvatar(@RequestParam("file") MultipartFile file) throws Exception {
        return getSuccessResponseVO(webLoginBiz.uploadAvatar(file));
    }

    @RequestMapping("/updateProfile")
    public ResponseVO updateProfile(@Valid UpdateProfileDTO dto, HttpServletRequest request) {
        return getSuccessResponseVO(webLoginBiz.updateProfile(dto, request.getHeader("studentToken")));
    }
}
