package com.nexora.controller;

import com.nexora.annotation.GlobalInterceptor;
import com.nexora.component.RedisComponent;
import com.nexora.component.TokenManager;
import com.nexora.constants.Constants;
import com.nexora.controller.ABaseController;
import com.nexora.dto.StudentLoginRequest;
import com.nexora.dto.StudentRegisterRequest;
import com.nexora.entity.dto.TokenUserInfoDTO;
import com.nexora.entity.enums.StageEnum;
import com.nexora.entity.po.UserInfo;
import com.nexora.entity.vo.ResponseVO;
import com.nexora.exception.BusinessException;
import com.nexora.service.UserInfoService;
import com.nexora.utils.LoginUserContext;
import com.nexora.utils.StringTools;
import com.nexora.vo.CheckCodeVO;
import com.nexora.vo.StudentLoginVO;
import com.wf.captcha.ArithmeticCaptcha;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

/**
 * 学生端账号 Controller（Codex 模式：公开浏览 + 登录/注册弹窗）
 * 类级默认需要登录，公开接口显式声明 checkLogin = false
 */
@RestController
@RequestMapping("/studentInfo")
@GlobalInterceptor(checkLogin = true)
public class StudentAccountController extends ABaseController {

    @Resource
    private UserInfoService userInfoService;

    @Resource
    private RedisComponent redisComponent;

    @Resource
    private TokenManager tokenManager;

    /**
     * 图形验证码（公开）
     */
    @GetMapping("/checkCode")
    @GlobalInterceptor(checkLogin = false)
    public ResponseVO<CheckCodeVO> checkCode(@RequestParam(required = false) String oldCheckCodeKey) {
        ArithmeticCaptcha captcha = new ArithmeticCaptcha(100, 42);
        String code = captcha.text();
        String checkCodeKey = redisComponent.saveCheckCode(code, oldCheckCodeKey);
        return getSuccessResponseVO(new CheckCodeVO(captcha.toBase64(), checkCodeKey));
    }

    /**
     * 学生注册（公开）：注册即登录，返回 { token, userInfo }（外层仍是统一 ResponseVO）
     * 注：验证码一次性，注册后直接返回登录态，避免用户二次输入验证码
     */
    @PostMapping("/register")
    @GlobalInterceptor(checkLogin = false)
    public ResponseVO<StudentLoginVO> register(@RequestBody StudentRegisterRequest request) {
        validCheckCode(request.getCheckCodeKey(), request.getCheckCode());
        try {
            if (StringTools.isEmpty(request.getUsername())) {
                throw new BusinessException("用户名不能为空");
            }
            if (StringTools.isEmpty(request.getEmail())) {
                throw new BusinessException("邮箱不能为空");
            }
            if (StringTools.isEmpty(request.getPassword())) {
                throw new BusinessException("密码不能为空");
            }
            if (userInfoService.getUserInfoByEmail(request.getEmail()) != null) {
                throw new BusinessException("该邮箱已注册");
            }

            UserInfo userInfo = new UserInfo();
            // 用户 ID 不依赖自增，统一使用随机数字字符串
            userInfo.setUserId(StringTools.getRandomNumber(Constants.LENGTH_10));
            userInfo.setUsername(request.getUsername());
            userInfo.setEmail(request.getEmail());
            userInfo.setPassword(StringTools.encodeByMD5(request.getPassword()));
            userInfo.setNickName(request.getUsername());
            userInfo.setRoleType(Constants.ROLE_STUDENT);
            // 注册默认小学低年级，可在「我的」页修改（方案 B）
            userInfo.setStage(StageEnum.PRIMARY_LOW.getCode());
            userInfo.setStatus(Constants.STATUS_ENABLE);
            userInfo.setCreateTime(new Date());
            userInfoService.add(userInfo);

            // 注册即登录：生成 token 并返回登录态
            return getSuccessResponseVO(buildLoginVO(userInfo));
        } finally {
            redisComponent.cleanCheckCode(request.getCheckCodeKey());
        }
    }

    /**
     * 学生登录（公开）：返回 { token, userInfo }
     */
    @PostMapping("/login")
    @GlobalInterceptor(checkLogin = false)
    public ResponseVO<StudentLoginVO> login(@RequestBody StudentLoginRequest request) {
        validCheckCode(request.getCheckCodeKey(), request.getCheckCode());
        try {
            if (StringTools.isEmpty(request.getEmail()) || StringTools.isEmpty(request.getPassword())) {
                throw new BusinessException("邮箱和密码不能为空");
            }

            UserInfo userInfo = userInfoService.findByEmailAndPassword(
                    request.getEmail(), StringTools.encodeByMD5(request.getPassword()));
            if (userInfo == null) {
                throw new BusinessException("邮箱或密码错误");
            }
            if (!Constants.STATUS_ENABLE.equals(userInfo.getStatus())) {
                throw new BusinessException("账号已被禁用");
            }

            // 更新最后登录时间
            UserInfo updateBean = new UserInfo();
            updateBean.setLastLoginTime(new Date());
            userInfoService.updateUserInfoByUserId(updateBean, userInfo.getUserId());

            return getSuccessResponseVO(buildLoginVO(userInfo));
        } finally {
            redisComponent.cleanCheckCode(request.getCheckCodeKey());
        }
    }

    /**
     * 学生退出登录
     */
    @PostMapping("/logout")
    public ResponseVO<Void> logout() {
        TokenUserInfoDTO current = LoginUserContext.get();
        if (current != null && current.getToken() != null) {
            redisComponent.removeToken(current.getToken());
        }
        return getSuccessResponseVO(null);
    }

    /**
     * 获取当前登录学生信息
     */
    @GetMapping("/getUserInfo")
    public ResponseVO<TokenUserInfoDTO> getUserInfo() {
        return getSuccessResponseVO(LoginUserContext.get());
    }

    /**
     * 修改学段（方案 B：默认小学低年级，可在「我的」页修改）
     */
    @PutMapping("/updateStage")
    public ResponseVO<Void> updateStage(@RequestParam String stage) {
        if (!StageEnum.isValid(stage)) {
            throw new BusinessException("非法的学段");
        }
        TokenUserInfoDTO current = LoginUserContext.get();
        if (current == null) {
            throw new BusinessException("登录已失效，请重新登录");
        }

        // 更新数据库
        UserInfo updateBean = new UserInfo();
        updateBean.setStage(stage);
        updateBean.setUpdateTime(new Date());
        userInfoService.updateUserInfoByUserId(updateBean, current.getUserId());

        // 同步 Redis 登录态
        current.setStage(stage);
        redisComponent.saveTokenInfo(current.getToken(), current);
        return getSuccessResponseVO(null);
    }

    /**
     * 构建登录返回：生成 token（userId 派生，Redis 单 key）并落库登录态
     */
    private StudentLoginVO buildLoginVO(UserInfo userInfo) {
        String token = tokenManager.generateToken(String.valueOf(userInfo.getUserId()));

        TokenUserInfoDTO tokenUserInfoDTO = new TokenUserInfoDTO();
        tokenUserInfoDTO.setUserId(userInfo.getUserId());
        tokenUserInfoDTO.setUsername(userInfo.getUsername());
        tokenUserInfoDTO.setEmail(userInfo.getEmail());
        tokenUserInfoDTO.setAvatar(userInfo.getAvatar());
        tokenUserInfoDTO.setStage(userInfo.getStage());
        tokenUserInfoDTO.setRoleType(userInfo.getRoleType());
        tokenUserInfoDTO.setToken(token);
        redisComponent.saveTokenInfo(token, tokenUserInfoDTO);

        StudentLoginVO loginVO = new StudentLoginVO();
        loginVO.setToken(token);
        loginVO.setUserInfo(tokenUserInfoDTO);
        return loginVO;
    }

    /**
     * 校验图形验证码（不匹配抛业务异常）
     */
    private void validCheckCode(String checkCodeKey, String checkCode) {
        String savedCode = redisComponent.getCheckCode(checkCodeKey);
        if (savedCode == null || checkCode == null || !savedCode.equalsIgnoreCase(checkCode)) {
            throw new BusinessException("验证码不正确");
        }
    }
}
