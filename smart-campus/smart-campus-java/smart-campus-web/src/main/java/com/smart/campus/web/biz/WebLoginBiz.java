package com.smart.campus.web.biz;

import com.smart.campus.redis.WebLoginRedisComponent;
import com.smart.campus.config.AppConfig;
import com.smart.campus.web.entity.dto.auth.UpdateProfileDTO;
import com.smart.campus.web.entity.dto.auth.UpdatePasswordDTO;
import com.smart.campus.entity.dto.StudentLoginDTO;
import com.smart.campus.entity.enums.ResponseCodeEnum;
import com.smart.campus.entity.enums.StatusEnum;
import com.smart.campus.entity.enums.UserRoleTypeEnum;
import com.smart.campus.entity.po.UserInfo;
import com.smart.campus.entity.vo.LoginUserVO;
import com.smart.campus.entity.vo.WebLoginCaptchaVO;
import com.smart.campus.entity.vo.WebLoginInfoVO;
import com.smart.campus.exception.BusinessException;
import com.smart.campus.service.UserInfoService;
import com.smart.campus.utils.LoginUserContextHolder;
import com.smart.campus.utils.StringTools;
import com.wf.captcha.SpecCaptcha;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.UUID;

@Service
public class WebLoginBiz {

    private static final long AVATAR_MAX_SIZE = 5L * 1024 * 1024;
    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyyMM");

    @Resource
    private UserInfoService userInfoService;

    @Resource
    private WebLoginRedisComponent webLoginRedisComponent;

    @Resource
    private AppConfig appConfig;

    public WebLoginCaptchaVO getCaptcha() {
        SpecCaptcha captcha = new SpecCaptcha(130, 40, 4);
        String captchaCode = captcha.text();
        String captchaKey = UUID.randomUUID().toString().replace("-", "");
        webLoginRedisComponent.saveCaptcha(captchaKey, captchaCode);

        WebLoginCaptchaVO result = new WebLoginCaptchaVO();
        result.setCaptchaKey(captchaKey);
        result.setCaptchaImage(captcha.toBase64());
        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    public WebLoginInfoVO doLogin(StudentLoginDTO dto) {
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
        if (!UserRoleTypeEnum.STUDENT.getCode().equals(userInfo.getRoleType())) {
            throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "当前账号不是学生账号");
        }

        UserInfo updateBean = new UserInfo();
        updateBean.setLastLoginTime(new Date());
        userInfoService.updateUserInfoByUserId(updateBean, userInfo.getUserId());
        userInfo.setLastLoginTime(updateBean.getLastLoginTime());

        LoginUserVO loginUser = buildLoginUser(userInfo);
        String token = UUID.randomUUID().toString().replace("-", "");
        webLoginRedisComponent.save(token, loginUser);

        WebLoginInfoVO result = new WebLoginInfoVO();
        result.setToken(token);
        result.setUserInfo(loginUser);
        return result;
    }

    public WebLoginInfoVO getLoginInfo() {
        LoginUserVO loginUser = LoginUserContextHolder.get();
        if (loginUser == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_901);
        }
        WebLoginInfoVO result = new WebLoginInfoVO();
        result.setUserInfo(loginUser);
        return result;
    }

    public void logout(String token) {
        if (StringTools.isEmpty(token)) {
            return;
        }
        webLoginRedisComponent.delete(token);
    }

    public String uploadAvatar(MultipartFile file) throws IOException {
        LoginUserVO loginUser = LoginUserContextHolder.get();
        if (loginUser == null || loginUser.getUserId() == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_901);
        }
        validateAvatarFile(file);
        String originalFileName = StringTools.trim(file.getOriginalFilename());
        String suffix = resolveAvatarSuffix(originalFileName);
        String relativePath = "avatar/" + MONTH_FORMATTER.format(LocalDate.now())
                + "/" + UUID.randomUUID().toString().replace("-", "") + "." + suffix;
        Path targetPath = Paths.get(appConfig.getProjectFolder(), relativePath);
        Files.createDirectories(targetPath.getParent());
        file.transferTo(targetPath);
        return relativePath.replace("\\", "/");
    }

    @Transactional(rollbackFor = Exception.class)
    public void updatePassword(UpdatePasswordDTO dto) {
        LoginUserVO loginUser = LoginUserContextHolder.get();
        if (loginUser == null || loginUser.getUserId() == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_901);
        }
        UserInfo userInfo = userInfoService.getUserInfoByUserId(loginUser.getUserId());
        if (userInfo == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_901);
        }
        String oldPassword = StringTools.trim(dto.getOldPassword());
        String newPassword = StringTools.trim(dto.getNewPassword());
        if (!encodePassword(oldPassword).equals(userInfo.getPassword())) {
            throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "原密码错误");
        }
        if (oldPassword.equals(newPassword)) {
            throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "新密码不能与原密码相同");
        }
        UserInfo updateBean = new UserInfo();
        updateBean.setPassword(encodePassword(newPassword));
        userInfoService.updateUserInfoByUserId(updateBean, userInfo.getUserId());
    }

    @Transactional(rollbackFor = Exception.class)
    public LoginUserVO updateProfile(UpdateProfileDTO dto, String token) {
        LoginUserVO loginUser = LoginUserContextHolder.get();
        if (loginUser == null || loginUser.getUserId() == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_901);
        }
        UserInfo userInfo = userInfoService.getUserInfoByUserId(loginUser.getUserId());
        if (userInfo == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_901);
        }

        UserInfo updateBean = new UserInfo();
        updateBean.setRealName(StringTools.trim(dto.getRealName()));
        updateBean.setEmail(StringTools.trim(dto.getEmail()));
        updateBean.setAvatar(StringTools.trim(dto.getAvatar()));
        userInfoService.updateUserInfoByUserId(updateBean, userInfo.getUserId());

        userInfo.setRealName(updateBean.getRealName());
        userInfo.setEmail(updateBean.getEmail());
        userInfo.setAvatar(updateBean.getAvatar());
        LoginUserVO refreshedLoginUser = buildLoginUser(userInfo);
        if (!StringTools.isEmpty(token)) {
            webLoginRedisComponent.save(token, refreshedLoginUser);
        }
        return refreshedLoginUser;
    }

    private LoginUserVO buildLoginUser(UserInfo userInfo) {
        LoginUserVO loginUser = new LoginUserVO();
        loginUser.setUserId(userInfo.getUserId());
        loginUser.setUserNo(userInfo.getUserNo());
        loginUser.setPhone(userInfo.getPhone());
        loginUser.setRealName(userInfo.getRealName());
        loginUser.setEmail(userInfo.getEmail());
        loginUser.setAvatar(userInfo.getAvatar());
        loginUser.setRoleType(userInfo.getRoleType());
        return loginUser;
    }

    private void validateAvatarFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "请选择头像图片");
        }
        if (file.getSize() > AVATAR_MAX_SIZE) {
            throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "头像图片不能超过5MB");
        }
        String suffix = resolveAvatarSuffix(file.getOriginalFilename());
        if (!"png".equals(suffix) && !"jpg".equals(suffix) && !"jpeg".equals(suffix) && !"webp".equals(suffix)) {
            throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "仅支持 png、jpg、jpeg、webp 格式头像");
        }
    }

    private String resolveAvatarSuffix(String fileName) {
        String normalizedName = StringTools.trim(fileName);
        int dotIndex = normalizedName == null ? -1 : normalizedName.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex >= normalizedName.length() - 1) {
            return "";
        }
        return normalizedName.substring(dotIndex + 1).toLowerCase();
    }

    private void validateCaptcha(StudentLoginDTO dto) {
        String captchaKey = StringTools.trim(dto.getCaptchaKey());
        String captchaCode = StringTools.trim(dto.getCaptchaCode());
        String cachedCaptcha = webLoginRedisComponent.getCaptcha(captchaKey);
        webLoginRedisComponent.deleteCaptcha(captchaKey);
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
