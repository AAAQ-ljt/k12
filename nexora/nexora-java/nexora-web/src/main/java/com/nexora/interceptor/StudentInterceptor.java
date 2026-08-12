package com.nexora.interceptor;

import com.nexora.annotation.GlobalInterceptor;
import com.nexora.component.RedisComponent;
import com.nexora.constants.Constants;
import com.nexora.entity.dto.TokenUserInfoDTO;
import com.nexora.entity.enums.ResponseCodeEnum;
import com.nexora.exception.BusinessException;
import com.nexora.utils.LoginUserContext;
import com.nexora.utils.StringTools;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 学生端登录拦截器（Web 层 AOP）
 * 通过 @GlobalInterceptor 注解声明接口是否需要登录，校验通过后写入登录上下文
 */
@Component
public class StudentInterceptor implements HandlerInterceptor {

    @Resource
    private RedisComponent redisComponent;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 非控制器方法（静态资源等）直接放行
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        // 取方法级注解，缺省回退到类级注解
        GlobalInterceptor interceptor = handlerMethod.getMethodAnnotation(GlobalInterceptor.class);
        if (interceptor == null) {
            interceptor = handlerMethod.getBeanType().getAnnotation(GlobalInterceptor.class);
        }

        // 未标注解默认需要登录（安全优先）；显式 checkLogin=false 的公开接口放行
        boolean checkLogin = interceptor == null || interceptor.checkLogin();
        if (!checkLogin) {
            return true;
        }

        // 校验登录态
        String token = request.getHeader(Constants.HEADER_STUDENT_TOKEN);
        if (StringTools.isEmpty(token)) {
            throw new BusinessException(ResponseCodeEnum.CODE_401);
        }
        TokenUserInfoDTO userInfo = redisComponent.getTokenInfo(token);
        if (userInfo == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_401);
        }

        // 写入登录上下文与请求属性
        LoginUserContext.set(userInfo);
        request.setAttribute(Constants.ATTR_USER_INFO, userInfo);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // 请求结束清理 ThreadLocal，防止线程复用串号
        LoginUserContext.remove();
    }
}
