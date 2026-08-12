package com.nexora.admin.controller;

import com.nexora.entity.vo.ResponseVO;
import com.nexora.constants.Constants;
import com.nexora.entity.dto.TokenUserInfoDTO;
import com.nexora.exception.BusinessException;
import com.nexora.admin.biz.AccountAdminBiz;
import com.nexora.controller.ABaseController;
import com.nexora.admin.dto.LoginRequestDTO;
import com.nexora.admin.vo.AdminLoginVO;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员账号管理控制器
 */
@RestController
@RequestMapping("/account")
public class AccountAdminController extends ABaseController {

    @Resource
    private AccountAdminBiz accountAdminBiz;

    /**
     * 管理员登录 - 支持 JSON 和 form-data
     * 
     * @param username 用户名
     * @param password 密码
     * @return 返回 adminToken
     */
    @PostMapping("/login")
    public ResponseVO<AdminLoginVO> login(
        @RequestBody(required = false) LoginRequestDTO request,
        @RequestParam(required = false) String username,
        @RequestParam(required = false) String password
    ) {
        // 优先从 JSON 请求体中获取
        if (request != null && request.getUsername() != null && request.getPassword() != null) {
            AdminLoginVO userInfo = accountAdminBiz.login(request.getUsername(), request.getPassword());
            return getSuccessResponseVO(userInfo);
        }
        
        // 其次从表单数据中获取
        if (username != null && password != null) {
            AdminLoginVO userInfo = accountAdminBiz.login(username, password);
            return getSuccessResponseVO(userInfo);
        }
        
        throw new BusinessException("用户名或密码为空");
    }

    /**
     * 退出登录
     * 
     * @param token 管理端 Token
     * @return 成功
     */
    @PostMapping("/logout")
    public ResponseVO<Void> logout(@RequestHeader("adminToken") String token) {
        accountAdminBiz.logout(token);
        return getSuccessResponseVO(null);
    }

    /**
     * 获取当前登录管理员信息（前端刷新后拉取）
     * 
     * @param token 管理端 Token
     * @return 管理员信息
     */
    @GetMapping("/getUserInfo")
    public ResponseVO<TokenUserInfoDTO> getUserInfo(jakarta.servlet.http.HttpServletRequest request) {
        // 拦截器已校验并把用户信息放入请求属性
        TokenUserInfoDTO userInfo = (TokenUserInfoDTO) request.getAttribute(Constants.ATTR_USER_INFO);
        return getSuccessResponseVO(userInfo);
    }
}
