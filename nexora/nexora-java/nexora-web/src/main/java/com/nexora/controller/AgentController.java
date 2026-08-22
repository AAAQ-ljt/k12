package com.nexora.controller;

import com.nexora.annotation.GlobalInterceptor;
import com.nexora.component.AgentChatComponent;
import com.nexora.dto.AgentCancelRequest;
import com.nexora.dto.AgentDeleteSessionRequest;
import com.nexora.dto.AgentSendMessageRequest;
import com.nexora.entity.dto.TokenUserInfoDTO;
import com.nexora.entity.po.AgentMessage;
import com.nexora.entity.po.AgentSession;
import com.nexora.entity.vo.ResponseVO;
import com.nexora.exception.BusinessException;
import com.nexora.utils.LoginUserContext;
import com.nexora.utils.StringTools;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * AI 助教 Controller：只做参数校验与登录态获取，模型调用统一收敛到 AgentChatComponent
 */
@RestController
@RequestMapping("/agent")
@GlobalInterceptor(checkLogin = true)
public class AgentController extends ABaseController {

    @Resource
    private AgentChatComponent agentChatComponent;

    @PostMapping("/sendMessage")
    public ResponseVO<AgentMessage> sendMessage(@RequestBody AgentSendMessageRequest request) {
        TokenUserInfoDTO current = LoginUserContext.get();
        if (current == null) {
            throw new BusinessException("登录已失效，请重新登录");
        }
        if (request == null || StringTools.isEmpty(request.getMessage())) {
            throw new BusinessException("消息不能为空");
        }
        AgentMessage message = agentChatComponent.sendMessage(current, request.getSessionId(),
                request.getMessage(), request.getImageResourceIds());
        return getSuccessResponseVO(message);
    }

    @PostMapping("/cancelMessage")
    public ResponseVO<Void> cancelMessage(@RequestBody AgentCancelRequest request) {
        TokenUserInfoDTO current = LoginUserContext.get();
        if (current == null || request == null || StringTools.isEmpty(request.getMessageId())) {
            throw new BusinessException("参数错误");
        }
        agentChatComponent.cancelMessage(current.getUserId(), request.getMessageId());
        return getSuccessResponseVO(null);
    }

    @PostMapping("/createSession")
    public ResponseVO<AgentSession> createSession() {
        return getSuccessResponseVO(agentChatComponent.createSession(LoginUserContext.get()));
    }

    @GetMapping("/sessionList")
    public ResponseVO<List<AgentSession>> sessionList() {
        return getSuccessResponseVO(agentChatComponent.sessionList(LoginUserContext.get()));
    }

    @GetMapping("/loadHistoryMessage")
    public ResponseVO<List<AgentMessage>> loadHistoryMessage(@RequestParam String sessionId) {
        return getSuccessResponseVO(agentChatComponent.historyMessage(LoginUserContext.get(), sessionId));
    }

    @PostMapping("/delSession")
    public ResponseVO<Void> delSession(@RequestBody AgentDeleteSessionRequest request) {
        if (request == null || StringTools.isEmpty(request.getSessionId())) {
            throw new BusinessException("参数错误");
        }
        agentChatComponent.deleteSession(LoginUserContext.get(), request.getSessionId());
        return getSuccessResponseVO(null);
    }
}
