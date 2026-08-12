package com.nexora.web.interceptor;

import com.alibaba.fastjson2.JSON;
import com.nexora.component.RedisComponent;
import com.nexora.constants.Constants;
import com.nexora.entity.dto.TokenUserInfoDTO;
import com.nexora.entity.vo.ResponseVO;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 学生端鉴权拦截器
 * 校验请求头中的 studentToken，无效则返回 401 JSON
 */
@Component
public class StudentInterceptor implements HandlerInterceptor {

    @Resource
    private RedisComponent redisComponent;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = request.getHeader(Constants.HEADER_STUDENT_TOKEN);
        if (token == null || token.isEmpty()) {
            sendUnauthorized(response);
            return false;
        }
        TokenUserInfoDTO tokenUserInfoDTO = redisComponent.getTokenInfo(token);
        if (tokenUserInfoDTO == null) {
            sendUnauthorized(response);
            return false;
        }
        request.setAttribute(Constants.ATTR_USER_INFO, tokenUserInfoDTO);
        return true;
    }

    /**
     * 返回未登录 JSON 响应（HTTP 状态码 200，业务码 401）
     */
    private void sendUnauthorized(HttpServletResponse response) throws Exception {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json;charset=UTF-8");
        ResponseVO<Object> vo = ResponseVO.error(401, "未登录或登录已过期");
        response.getWriter().write(JSON.toJSONString(vo));
    }
}
