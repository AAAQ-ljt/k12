package com.smart.campus.admin.biz;

import com.smart.campus.redis.AdminLoginRedisComponent;
import com.smart.campus.entity.dto.AdminLoginDTO;
import com.smart.campus.entity.enums.ResponseCodeEnum;
import com.smart.campus.entity.enums.StatusEnum;
import com.smart.campus.entity.enums.UserRoleTypeEnum;
import com.smart.campus.entity.po.UserInfo;
import com.smart.campus.entity.vo.AdminLoginCaptchaVO;
import com.smart.campus.entity.vo.AdminLoginInfoVO;
import com.smart.campus.entity.vo.LoginUserVO;
import com.smart.campus.exception.BusinessException;
import com.smart.campus.service.UserInfoService;
import com.smart.campus.utils.LoginUserContextHolder;
import com.smart.campus.utils.StringTools;
import com.wf.captcha.SpecCaptcha;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

@Service
public class AdminLoginBiz {

    @Resource
    private UserInfoService userInfoService;

    @Resource
    private AdminLoginRedisComponent adminLoginRedisComponent;

    @Resource
    private AdminPermissionBiz adminPermissionBiz;

    public AdminLoginCaptchaVO getCaptcha() {
        SpecCaptcha captcha = new SpecCaptcha(130, 40, 4);
        String captchaCode = captcha.text();
        String captchaKey = UUID.randomUUID().toString().replace("-", "");
        adminLoginRedisComponent.saveCaptcha(captchaKey, captchaCode);

        AdminLoginCaptchaVO result = new AdminLoginCaptchaVO();
        result.setCaptchaKey(captchaKey);
        result.setCaptchaImage(captcha.toBase64());
        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    public AdminLoginInfoVO doLogin(AdminLoginDTO dto) {
        validateCaptcha(dto);
        String phone = StringTools.trim(dto.getPhone());
        String password = StringTools.trim(dto.getPassword());

        UserInfo userInfo = userInfoService.getUserInfoByPhone(phone);
        if (userInfo == null || !encodePassword(password).equals(userInfo.getPassword())) {
            throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "手机号或密码错误");
        }
        if (!StatusEnum.ENABLED.getCode().equals(userInfo.getStatus())) {
            throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "当前账号已停用");
        }
        if (UserRoleTypeEnum.STUDENT.getCode().equals(userInfo.getRoleType())) {
            throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "当前账号无管理后台登录权限");
        }

        UserInfo updateBean = new UserInfo();
        updateBean.setLastLoginTime(new Date());
        userInfoService.updateUserInfoByUserId(updateBean, userInfo.getUserId());
        userInfo.setLastLoginTime(updateBean.getLastLoginTime());

        LoginUserVO loginUser = buildLoginUser(userInfo);
        String token = UUID.randomUUID().toString().replace("-", "");
        adminLoginRedisComponent.save(token, loginUser);
        AdminLoginInfoVO result = buildLoginInfo(loginUser);
        result.setToken(token);
        return result;
    }

    public AdminLoginInfoVO getLoginInfo() {
        LoginUserVO loginUser = LoginUserContextHolder.get();
        if (loginUser == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_901);
        }
        AdminLoginInfoVO result = buildLoginInfo(loginUser);
        result.setToken(null);
        return result;
    }

    public void logout(String token) {
        if (StringTools.isEmpty(token)) {
            return;
        }
        adminLoginRedisComponent.delete(token);
    }

    private LoginUserVO buildLoginUser(UserInfo userInfo) {
        LoginUserVO loginUser = new LoginUserVO();
        loginUser.setUserId(userInfo.getUserId());
        loginUser.setUserNo(userInfo.getUserNo());
        loginUser.setPhone(userInfo.getPhone());
        loginUser.setRealName(userInfo.getRealName());
        loginUser.setRoleType(userInfo.getRoleType());
        return loginUser;
    }

    private AdminLoginInfoVO buildLoginInfo(LoginUserVO loginUser) {
        AdminLoginInfoVO result = new AdminLoginInfoVO();
        result.setUserInfo(loginUser);
        result.setMenuList(adminPermissionBiz.getMenuListByRole(loginUser.getRoleType()));
        result.setMenuCodes(adminPermissionBiz.getMenuCodesByRole(loginUser.getRoleType()));
        return result;
    }

    private void validateCaptcha(AdminLoginDTO dto) {
        String captchaKey = StringTools.trim(dto.getCaptchaKey());
        String captchaCode = StringTools.trim(dto.getCaptchaCode());
        String cachedCaptcha = adminLoginRedisComponent.getCaptcha(captchaKey);
        adminLoginRedisComponent.deleteCaptcha(captchaKey);
        if (StringTools.isEmpty(cachedCaptcha) || StringTools.isEmpty(captchaCode)) {
            throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "验证码已失效，请刷新后重试");
        }
        if (!cachedCaptcha.equalsIgnoreCase(captchaCode)) {
            throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "图片验证码错误");
        }
    }

    private String encodePassword(String password) {
        return DigestUtils.md5DigestAsHex(password.getBytes(StandardCharsets.UTF_8));
    }
}
