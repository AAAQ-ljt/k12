package com.easymall.aspect;

import com.easymall.annotation.GlobalInterceptor;
import com.easymall.entity.dto.TokenUserInfoDTO;
import com.easymall.entity.enums.ResponseCodeEnum;
import com.easymall.exception.BusinessException;
import com.easymall.component.RedisComponent;
import com.easymall.utils.StringTools;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;

@Component("operationAspect")
@Aspect
@Slf4j
public class GlobalOperationAspect {

    @Resource
    private RedisComponent redisComponent;

    @Before("@annotation(com.easymall.annotation.GlobalInterceptor)")
    public void interceptorDo(JoinPoint point) {
        Method method = ((MethodSignature) point.getSignature()).getMethod();
        GlobalInterceptor interceptor = method.getAnnotation(GlobalInterceptor.class);
        if (null == interceptor) {
            return;
        }
        /**
         * 校验登录
         */
        if (interceptor.checkLogin()) {
            checkLogin();
        }
    }

    //校验登录
    private void checkLogin() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String token = request.getHeader("token");
        if (StringTools.isEmpty(token)) {
            throw new BusinessException(ResponseCodeEnum.CODE_901);
        }
        TokenUserInfoDTO tokenUserInfoDto = redisComponent.getTokenInfo(token);
        if (System.getProperty("dev") != null) {
            tokenUserInfoDto = new TokenUserInfoDTO();
            tokenUserInfoDto.setUserId("vuzPteqk");
            tokenUserInfoDto.setNickName("程序员老罗");
            tokenUserInfoDto.setToken(token);
            redisComponent.saveTokenInfo(tokenUserInfoDto);
        }
        if (tokenUserInfoDto == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_901);
        }
    }
}