package com.nexora.web.controller;

import com.nexora.component.RedisComponent;
import com.nexora.constants.Constants;
import com.nexora.entity.dto.TokenUserInfoDTO;
import com.nexora.entity.po.UserInfo;
import com.nexora.entity.query.UserInfoQuery;
import com.nexora.entity.vo.ResponseVO;
import com.nexora.exception.BusinessException;
import com.nexora.service.UserInfoService;
import com.nexora.web.dto.StudentLoginRequest;
import com.nexora.web.vo.StudentLoginVO;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * 学生端账号 Controller
 * 提供登录、登出、获取用户信息接口
 */
@RestController
@RequestMapping("/studentInfo")
public class StudentAccountController {

    @Resource
    private UserInfoService userInfoService;

    @Resource
    private RedisComponent redisComponent;

    /**
     * 学生登录
     *
     * @param request 登录请求（email + password）
     * @return 登录成功返回 token 和用户信息
     */
    @PostMapping("/login")
    public ResponseVO<StudentLoginVO> login(@RequestBody StudentLoginRequest request) {
        String email = request.getEmail();
        String password = request.getPassword();
        if (email == null || email.isEmpty() || password == null || password.isEmpty()) {
            throw new BusinessException("邮箱和密码不能为空");
        }

        // MD5 加密密码
        String encodedPassword = DigestUtils.md5Hex(password);

        // 按 email + password + 角色为学生 查询
        UserInfoQuery query = new UserInfoQuery();
        query.setEmail(email);
        query.setPassword(encodedPassword);
        query.setRoleType(Constants.ROLE_STUDENT);
        List<UserInfo> userList = userInfoService.findListByParam(query);
        if (userList == null || userList.isEmpty()) {
            throw new BusinessException("邮箱或密码错误");
        }

        UserInfo userInfo = userList.get(0);
        if (!Constants.STATUS_ENABLE.equals(userInfo.getStatus())) {
            throw new BusinessException("账号已被禁用");
        }

        // 生成 token
        String token = UUID.randomUUID().toString().replace("-", "");

        // 构建 TokenUserInfoDTO
        TokenUserInfoDTO tokenUserInfoDTO = new TokenUserInfoDTO();
        tokenUserInfoDTO.setUserId(String.valueOf(userInfo.getUserId()));
        tokenUserInfoDTO.setUsername(userInfo.getUsername());
        tokenUserInfoDTO.setStage(userInfo.getStage());
        tokenUserInfoDTO.setRoleType(userInfo.getRoleType());

        // 保存到 Redis
        redisComponent.saveTokenInfo(token, tokenUserInfoDTO);

        // 构建响应
        StudentLoginVO loginVO = new StudentLoginVO();
        loginVO.setToken(token);
        loginVO.setUserInfo(tokenUserInfoDTO);
        return ResponseVO.success(loginVO);
    }

    /**
     * 学生登出
     *
     * @param request HTTP 请求（从 header 获取 token）
     * @return 操作结果
     */
    @PostMapping("/logout")
    public ResponseVO<Object> logout(HttpServletRequest request) {
        String token = request.getHeader(Constants.HEADER_STUDENT_TOKEN);
        if (token != null && !token.isEmpty()) {
            redisComponent.removeToken(token);
        }
        return ResponseVO.success();
    }

    /**
     * 获取当前登录用户信息
     *
     * @param request HTTP 请求（拦截器已注入 userInfo）
     * @return 用户信息
     */
    @GetMapping("/getUserInfo")
    public ResponseVO<TokenUserInfoDTO> getUserInfo(HttpServletRequest request) {
        TokenUserInfoDTO userInfo = (TokenUserInfoDTO) request.getAttribute(Constants.ATTR_USER_INFO);
        return ResponseVO.success(userInfo);
    }
}
