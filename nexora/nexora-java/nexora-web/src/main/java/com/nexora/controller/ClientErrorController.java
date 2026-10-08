package com.nexora.controller;

import com.nexora.dto.ClientErrorReportRequest;
import com.nexora.entity.dto.TokenUserInfoDTO;
import com.nexora.utils.LoginUserContext;
import com.nexora.utils.StringTools;
import com.nexora.entity.vo.ResponseVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 前端错误兜底页上报接口（2026-10-08）。
 *
 * 背景：学生端渲染崩了会显示「页面出了点小问题」，但错误原文只在用户截图的「技术细节」里，
 * 服务端完全看不到，排查线上问题只能靠猜。这个接口把原文收进服务端日志（WARN），
 * 之后 `journalctl -u nexora-web | grep 错误兜底页上报` 就能定位。
 *
 * 故意不加 {@code @GlobalInterceptor(checkLogin = true)}：会话失效本身就可能触发兜底页，
 * 上报不该再被 401 挡掉（拿得到登录态就带上 userId，拿不到记「未登录」）。
 */
@Slf4j
@RestController
@RequestMapping("/clientError")
public class ClientErrorController extends ABaseController {

    @PostMapping("/report")
    public ResponseVO<Void> report(@RequestBody ClientErrorReportRequest request) {
        if (request == null || StringTools.isEmpty(request.getMessage())) {
            return getSuccessResponseVO(null);
        }
        TokenUserInfoDTO current = LoginUserContext.get();
        String userId = current == null ? "(未登录)" : current.getUserId();
        // 截断：错误原文可能很长（含组件栈），日志里留前 1200 字足够定位
        String message = request.getMessage().trim();
        if (message.length() > 1200) {
            message = message.substring(0, 1200) + "...";
        }
        log.warn("学生端错误兜底页上报 userId={} stage={} url={} message={} ua={}",
                userId, request.getStage(), request.getUrl(), message, request.getUserAgent());
        return getSuccessResponseVO(null);
    }
}
