package com.nexora.admin.interceptor;

import com.nexora.component.RedisComponent;
import com.nexora.constants.Constants;
import com.nexora.entity.dto.TokenUserInfoDTO;
import com.nexora.entity.enums.ResponseCodeEnum;
import com.nexora.exception.BusinessException;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Component
public class AppInterceptor implements HandlerInterceptor {

    @Resource
    private RedisComponent redisComponent;

    // 不需要拦截的路径（登录/退出/错误页等公开接口）
    private static final Set<String> WHITE_LIST = new HashSet<>(Arrays.asList(
            "/account/login",
            "/account/logout",
            "/error"
    ));

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 获取请求路径
        String requestURI = request.getRequestURI();

        // 判断是否在白名单中
        for (String path : WHITE_LIST) {
            if (requestURI.contains(path)) {
                return true;
            }
        }

        // 白名单外所有请求（GET / POST / PUT / DELETE）统一校验 Token
        String token = request.getHeader(Constants.HEADER_ADMIN_TOKEN);

        // Token 为空时抛出异常
        if (token == null || token.trim().isEmpty()) {
            throw new BusinessException(ResponseCodeEnum.CODE_401);
        }

        // 从 Redis 中获取 Token 信息
        TokenUserInfoDTO userInfo = redisComponent.getTokenInfo(token);
        if (userInfo == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_401);
        }

        // 将用户信息存入请求属性，供后续业务使用
        request.setAttribute(Constants.ATTR_USER_INFO, userInfo);
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
    }
}