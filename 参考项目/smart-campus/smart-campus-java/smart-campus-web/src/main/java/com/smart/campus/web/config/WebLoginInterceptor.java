package com.smart.campus.web.config;

import com.smart.campus.redis.WebLoginRedisComponent;
import com.smart.campus.entity.enums.ResponseCodeEnum;
import com.smart.campus.entity.vo.LoginUserVO;
import com.smart.campus.exception.BusinessException;
import com.smart.campus.utils.LoginUserContextHolder;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class WebLoginInterceptor implements HandlerInterceptor {

    @Resource
    private WebLoginRedisComponent webLoginRedisComponent;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String token = request.getHeader("studentToken");
        LoginUserVO loginUser = webLoginRedisComponent.get(token);
        if (loginUser == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_901);
        }
        LoginUserContextHolder.set(loginUser);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        LoginUserContextHolder.clear();
    }
}
