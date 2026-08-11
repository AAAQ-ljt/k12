package com.smart.campus.admin.config;

import com.smart.campus.admin.annotation.AdminPermission;
import com.smart.campus.admin.biz.AdminPermissionBiz;
import com.smart.campus.entity.enums.ResponseCodeEnum;
import com.smart.campus.entity.vo.LoginUserVO;
import com.smart.campus.exception.BusinessException;
import com.smart.campus.redis.AdminLoginRedisComponent;
import com.smart.campus.utils.LoginUserContextHolder;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AdminLoginInterceptor implements HandlerInterceptor {

    @Resource
    private AdminLoginRedisComponent adminLoginRedisComponent;

    @Resource
    private AdminPermissionBiz adminPermissionBiz;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String token = request.getHeader("adminToken");
        LoginUserVO loginUser = adminLoginRedisComponent.get(token);
        if (loginUser == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_901);
        }
        LoginUserContextHolder.set(loginUser);
        checkPermission(handler, loginUser);
        return true;
    }

    private void checkPermission(Object handler, LoginUserVO loginUser) {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return;
        }
        AdminPermission permission = handlerMethod.getMethodAnnotation(AdminPermission.class);
        if (permission == null) {
            permission = handlerMethod.getBeanType().getAnnotation(AdminPermission.class);
        }
        if (permission == null) {
            return;
        }
        if (!adminPermissionBiz.hasPermission(loginUser, permission.value())) {
            throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "无操作权限");
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        LoginUserContextHolder.clear();
    }
}
