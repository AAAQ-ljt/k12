package com.nexora.admin.interceptor;

import com.alibaba.fastjson2.JSON;
import com.nexora.component.RedisComponent;
import com.nexora.constants.Constants;
import com.nexora.entity.dto.TokenUserInfoDTO;
import com.nexora.entity.vo.ResponseVO;
import com.nexora.utils.StringTools;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.PrintWriter;

/**
 * 管理端 Token 拦截器
 */
@Component
public class AdminInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(AdminInterceptor.class);

    @Resource
    private RedisComponent redisComponent;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (handler == null) {
            return false;
        }
        // 非控制器方法直接放行（静态资源等）
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        String token = request.getHeader(Constants.HEADER_ADMIN_TOKEN);
        if (StringTools.isEmpty(token)) {
            renderUnauthorized(response);
            return false;
        }

        TokenUserInfoDTO tokenUserInfo = redisComponent.getTokenInfo(token);
        if (tokenUserInfo == null) {
            renderUnauthorized(response);
            return false;
        }

        // 校验角色：管理端仅允许管理员访问
        if (tokenUserInfo.getRoleType() == null || !tokenUserInfo.getRoleType().equals(Constants.ROLE_ADMIN)) {
            renderUnauthorized(response);
            return false;
        }

        request.setAttribute(Constants.ATTR_USER_INFO, tokenUserInfo);
        return true;
    }

    /**
     * 返回未登录 JSON 响应，HTTP 状态码设为 200 由前端根据 body 判断
     */
    private void renderUnauthorized(HttpServletResponse response) {
        response.setStatus(200);
        response.setContentType("application/json;charset=UTF-8");
        ResponseVO<?> vo = ResponseVO.error(401, "未登录或登录已过期");
        try (PrintWriter writer = response.getWriter()) {
            writer.write(JSON.toJSONString(vo));
            writer.flush();
        } catch (Exception e) {
            logger.error("写入未登录响应失败", e);
        }
    }
}
