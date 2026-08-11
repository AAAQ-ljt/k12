package com.easymall.controller;

import com.easymall.annotation.GlobalInterceptor;
import com.easymall.component.RedisComponent;
import com.easymall.constants.Constants;
import com.easymall.entity.dto.TokenUserInfoDTO;
import com.easymall.entity.po.UserInfo;
import com.easymall.entity.vo.CheckCodeVO;
import com.easymall.entity.vo.ResponseVO;
import com.easymall.entity.vo.UserInfoVO;
import com.easymall.exception.BusinessException;
import com.easymall.service.UserInfoService;
import com.easymall.utils.CopyTools;
import com.wf.captcha.ArithmeticCaptcha;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@RestController("accountController")
@RequestMapping("/account")
@Validated
public class AccountController extends ABaseController {

    @Resource
    private UserInfoService userInfoService;

    @Resource
    private RedisComponent redisComponent;

    /**
     * 验证码
     */
    @RequestMapping(value = "/checkCode")
    @GlobalInterceptor
    public ResponseVO checkCode() {
        ArithmeticCaptcha captcha = new ArithmeticCaptcha(100, 42);
        String code = captcha.text();
        String checkCodeKey = redisComponent.saveCheckCode(code);
        String checkCodeBase64 = captcha.toBase64();
        CheckCodeVO checkCodeVO = new CheckCodeVO(checkCodeBase64, checkCodeKey);
        return getSuccessResponseVO(checkCodeVO);
    }

    @RequestMapping(value = "/register")
    @GlobalInterceptor
    public ResponseVO register(@NotEmpty @Email @Size(max = 150) String email,
                               @NotEmpty @Size(max = 20) String nickName,
                               @NotEmpty @Pattern(regexp = Constants.REGEX_PASSWORD) String registerPassword,
                               @NotEmpty String checkCodeKey, @NotEmpty String checkCode) {
        try {
            if (!checkCode.equalsIgnoreCase(redisComponent.getCheckCode(checkCodeKey))) {
                throw new BusinessException("图片验证码不正确");
            }
            userInfoService.register(email, nickName, registerPassword);
            return getSuccessResponseVO(null);
        } finally {
            redisComponent.cleanCheckCode(checkCodeKey);
        }
    }

    @RequestMapping(value = "/login")
    @GlobalInterceptor
    public ResponseVO login(@NotEmpty @Email String email, @NotEmpty String password, @NotEmpty String checkCodeKey, @NotEmpty String checkCode) {
        try {
            if (!checkCode.equalsIgnoreCase(redisComponent.getCheckCode(checkCodeKey))) {
                throw new BusinessException("图片验证码不正确");
            }
            String ip = getIpAddr();
            TokenUserInfoDTO tokenUserInfoDto = userInfoService.login(email, password, ip);
            return getSuccessResponseVO(tokenUserInfoDto);
        } finally {
            redisComponent.cleanCheckCode(checkCodeKey);
        }
    }

    @RequestMapping(value = "/autoLogin")
    @GlobalInterceptor
    public ResponseVO autoLogin() {
        TokenUserInfoDTO tokenUserInfoDto = getTokenUserInfo();
        if (tokenUserInfoDto == null) {
            return getSuccessResponseVO(null);
        }
        redisComponent.saveTokenInfo(tokenUserInfoDto);
        return getSuccessResponseVO(tokenUserInfoDto);
    }

    @RequestMapping(value = "/logout")
    @GlobalInterceptor
    public ResponseVO logout(HttpServletResponse response, @RequestHeader("token") String token) {
        redisComponent.cleanToken(token);
        return getSuccessResponseVO(null);
    }


    @RequestMapping(value = "/updatePassword")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO updatePassword(@NotEmpty String oldPassword, @NotEmpty String password) {
        TokenUserInfoDTO tokenUserInfoDto = getTokenUserInfo();
        userInfoService.updatePassword(tokenUserInfoDto.getUserId(), oldPassword, password);
        return getSuccessResponseVO(null);
    }

    @RequestMapping(value = "/getUserInfo")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO getUserInfo() {
        TokenUserInfoDTO tokenUserInfoDto = getTokenUserInfo();
        UserInfo userInfo = this.userInfoService.getUserInfoByUserId(tokenUserInfoDto.getUserId());
        return getSuccessResponseVO(CopyTools.copy(userInfo, UserInfoVO.class));
    }

    @RequestMapping(value = "/updateUserInfo")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO updateUserInfo(@NotEmpty String avatar, @NotEmpty @Size(max = 20) String nickName, @NotNull Integer sex) {
        TokenUserInfoDTO tokenUserInfoDto = getTokenUserInfo();
        UserInfo updateInfo = new UserInfo();
        updateInfo.setAvatar(avatar);
        updateInfo.setNickName(nickName);
        updateInfo.setSex(sex);
        this.userInfoService.updateUserInfoByUserId(updateInfo, tokenUserInfoDto.getUserId());

        tokenUserInfoDto.setNickName(nickName);
        tokenUserInfoDto.setAvatar(avatar);
        redisComponent.updateTokenInfo(tokenUserInfoDto);
        return getSuccessResponseVO(tokenUserInfoDto);
    }
}