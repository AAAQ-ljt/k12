package com.easymall.controller;

import com.easymall.component.RedisComponent;
import com.easymall.constants.Constants;
import com.easymall.entity.config.AppConfig;
import com.easymall.entity.vo.CheckCodeVO;
import com.easymall.entity.vo.ResponseVO;
import com.easymall.exception.BusinessException;
import com.easymall.utils.StringTools;
import com.wf.captcha.ArithmeticCaptcha;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("accountController")
@RequestMapping("/account")
@Validated
public class AccountController extends ABaseController {

    @Resource
    private AppConfig appConfig;

    @Resource
    private RedisComponent redisComponent;

    /**
     * 验证码
     */
    @RequestMapping(value = "/checkCode")
    public ResponseVO checkCode() {
        ArithmeticCaptcha captcha = new ArithmeticCaptcha(100, 42);
        String code = captcha.text();
        String checkCodeKey = redisComponent.saveCheckCode(code);
        String checkCodeBase64 = captcha.toBase64();
        CheckCodeVO checkCodeVO = new CheckCodeVO(checkCodeBase64, checkCodeKey);
        return getSuccessResponseVO(checkCodeVO);
    }

    @RequestMapping(value = "/login")
    public ResponseVO login(
            @NotEmpty String account,
            @NotEmpty String password,
            @NotEmpty String checkCode,
            @NotEmpty String checkCodeKey) {
        try {
            if (!checkCode.equalsIgnoreCase((String) redisComponent.getCheckCode(checkCodeKey))) {
                throw new BusinessException("图片验证码不正确");
            }
            if (!account.equals(appConfig.getAdminAccount()) || !password.equals(StringTools.encodeByMD5(appConfig.getAdminPassword()))) {
                throw new BusinessException("账号或者密码错误");
            }
            String token = redisComponent.saveTokenInfo4Admin(account);
            return getSuccessResponseVO(token);
        } finally {
            redisComponent.cleanCheckCode(checkCodeKey);
        }
    }

    @RequestMapping(value = "/logout")
    public ResponseVO logout(@RequestHeader(Constants.TOKEN_ADMIN) String token) {
        redisComponent.cleanToken4Admin(token);
        return getSuccessResponseVO(null);
    }
}