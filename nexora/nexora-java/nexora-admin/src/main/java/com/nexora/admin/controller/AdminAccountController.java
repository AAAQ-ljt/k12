package com.nexora.admin.controller;

import com.nexora.component.RedisComponent;
import com.nexora.constants.Constants;
import com.nexora.entity.dto.TokenUserInfoDTO;
import com.nexora.entity.po.UserInfo;
import com.nexora.entity.vo.ResponseVO;
import com.nexora.exception.BusinessException;
import com.nexora.service.UserInfoService;
import com.nexora.utils.StringTools;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 管理端账号控制器 — 登录 / 登出 / 获取当前用户信息
 */
@RestController
@RequestMapping("/adminInfo")
public class AdminAccountController {

    private static final Logger logger = LoggerFactory.getLogger(AdminAccountController.class);

    @Resource
    private UserInfoService userInfoService;

    @Resource
    private RedisComponent redisComponent;

    /**
     * 管理员登录
     */
    @PostMapping("/login")
    public ResponseVO<?> login(@RequestBody Map<String, String> params) {
        String email = params.get("email");
        String password = params.get("password");
        if (StringTools.isEmpty(email) || StringTools.isEmpty(password)) {
            throw new BusinessException("邮箱或密码不能为空");
        }

        // MD5 加密密码后查询
        String md5Password = DigestUtils.md5Hex(password);
        UserInfo userInfo = userInfoService.findByEmailAndPassword(email, md5Password);
        if (userInfo == null || !Constants.ROLE_ADMIN.equals(userInfo.getRoleType())) {
            throw new BusinessException("邮箱或密码错误");
        }

        // 校验账号状态
        if (!Constants.STATUS_ENABLE.equals(userInfo.getStatus())) {
            throw new BusinessException("账号已被禁用");
        }

        // 生成 token 并保存到 Redis
        String token = UUID.randomUUID().toString().replace("-", "");
        TokenUserInfoDTO tokenUserInfo = new TokenUserInfoDTO();
        tokenUserInfo.setUserId(String.valueOf(userInfo.getUserId()));
        tokenUserInfo.setUsername(userInfo.getUsername());
        tokenUserInfo.setStage(userInfo.getStage());
        tokenUserInfo.setRoleType(userInfo.getRoleType());
        redisComponent.saveTokenInfo(token, tokenUserInfo);

        // 更新最后登录时间
        UserInfo updateBean = new UserInfo();
        updateBean.setLastLoginTime(new java.util.Date());
        userInfoService.updateUserInfoByUserId(updateBean, userInfo.getUserId());

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("userInfo", tokenUserInfo);
        return ResponseVO.success(result);
    }

    /**
     * 管理员登出
     */
    @PostMapping("/logout")
    public ResponseVO<?> logout(HttpServletRequest request) {
        String token = request.getHeader(Constants.HEADER_ADMIN_TOKEN);
        if (!StringTools.isEmpty(token)) {
            redisComponent.removeToken(token);
        }
        return ResponseVO.success();
    }

    /**
     * 获取当前登录用户信息
     */
    @GetMapping("/getUserInfo")
    public ResponseVO<TokenUserInfoDTO> getUserInfo(HttpServletRequest request) {
        TokenUserInfoDTO userInfo = (TokenUserInfoDTO) request.getAttribute(Constants.ATTR_USER_INFO);
        return ResponseVO.success(userInfo);
    }
}
