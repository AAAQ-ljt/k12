package com.nexora.component;

import com.alibaba.fastjson2.JSON;
import com.nexora.dto.AgentMessagePushDTO;
import com.nexora.entity.dto.TokenUserInfoDTO;
import com.nexora.entity.enums.StageEnum;
import com.nexora.entity.po.AgentMessage;
import com.nexora.entity.po.AgentSession;
import com.nexora.entity.query.AgentMessageQuery;
import com.nexora.entity.query.AgentSessionQuery;
import com.nexora.exception.BusinessException;
import com.nexora.service.AgentMessageService;
import com.nexora.service.AgentSessionService;
import com.nexora.utils.StringTools;
import com.nexora.websocket.ChannelContextUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * AI 对话核心组件：落库、组装上下文、DeepSeek 流式回复、WebSocket 推送、取消与错误处理
 */
@Component
@Slf4j
public class AgentChatComponent {

    private static final ExecutorService ASYNC_EXECUTOR = Executors.newFixedThreadPool(4);

    private static final int HISTORY_LIMIT = 10;

    @Resource
    private ChatClient chatClient;

    @Resource
    private AgentMessageService agentMessageService;

    @Resource
    private AgentSessionService agentSessionService;

    @Resource
    private ChannelContextUtils channelContextUtils;

    @Resource
    private RedisComponent redisComponent;

    @Value("${spring.ai.openai.chat.options.model:deepseek-v4-flash}")
    private String chatModel;

    @Value("${spring.ai.openai.chat.options.reasoning-effort:high}")
    private String reasoningEffort;

    public AgentMessage sendMessage(TokenUserInfoDTO user, String sessionId, String userMessage) {
        AgentSession session = resolveSession(user, sessionId);

        AgentMessage message = new AgentMessage();
        message.setMessageId(generateId());
        message.setSessionId(session.getSessionId());
        message.setUserId(user.getUserId());
        message.setStage(user.getStage());
        message.setUserMessage(userMessage);
        message.setStatus(0);
        message.setPromptTokens(0);
        message.setCompletionTokens(0);
        message.setCreateTime(new Date());
        message.setUpdateTime(new Date());
        agentMessageService.add(message);

        updateSession(session, userMessage);
        ASYNC_EXECUTOR.execute(() -> assistantAnswer(user, session, message));
        return message;
    }

    public void cancelMessage(String userId, String messageId) {
        AgentMessage dbMessage = agentMessageService.getAgentMessageByMessageId(messageId);
        if (dbMessage == null || !userId.equals(dbMessage.getUserId())) {
            throw new BusinessException("消息不存在");
        }
        if (dbMessage.getStatus() != null && dbMessage.getStatus() != 0) {
            return;
        }
        redisComponent.saveCancelMessage(userId, messageId);
        AgentMessage updateBean = new AgentMessage();
        updateBean.setStatus(2);
        updateBean.setErrorInfo("用户取消");
        updateBean.setUpdateTime(new Date());
        agentMessageService.updateAgentMessageByMessageId(updateBean, messageId);
    }

    public AgentSession createSession(TokenUserInfoDTO user) {
        AgentSession session = new AgentSession();
        session.setSessionId(generateId());
        session.setUserId(user.getUserId());
        session.setTitle("新对话");
        session.setStage(user.getStage());
        session.setScene(0);
        session.setMessageCount(0);
        session.setStatus(0);
        session.setCreateTime(new Date());
        session.setUpdateTime(new Date());
        agentSessionService.add(session);
        return session;
    }

    public List<AgentSession> sessionList(TokenUserInfoDTO user) {
        AgentSessionQuery query = new AgentSessionQuery();
        query.setUserId(user.getUserId());
        query.setOrderBy("last_message_time desc");
        return agentSessionService.findListByParam(query);
    }

    public List<AgentMessage> historyMessage(TokenUserInfoDTO user, String sessionId) {
        AgentSession session = agentSessionService.getAgentSessionBySessionId(sessionId);
        if (session == null || !user.getUserId().equals(session.getUserId())) {
            throw new BusinessException("会话不存在");
        }
        AgentMessageQuery query = new AgentMessageQuery();
        query.setUserId(user.getUserId());
        query.setSessionId(sessionId);
        query.setOrderBy("create_time asc");
        return agentMessageService.findListByParam(query);
    }

    public void deleteSession(TokenUserInfoDTO user, String sessionId) {
        AgentSession session = agentSessionService.getAgentSessionBySessionId(sessionId);
        if (session == null || !user.getUserId().equals(session.getUserId())) {
            throw new BusinessException("会话不存在");
        }
        agentSessionService.deleteAgentSessionBySessionId(sessionId);

        AgentMessageQuery messageQuery = new AgentMessageQuery();
        messageQuery.setUserId(user.getUserId());
        messageQuery.setSessionId(sessionId);
        agentMessageService.deleteByParam(messageQuery);
    }

    private void assistantAnswer(TokenUserInfoDTO user, AgentSession session, AgentMessage message) {
        AgentMessagePushDTO push = new AgentMessagePushDTO();
        push.setMessageId(message.getMessageId());
        push.setSessionId(session.getSessionId());
        StringBuilder answer = new StringBuilder();
        try {
            List<Message> historyMessages = buildHistory(user.getUserId(), session.getSessionId(), message.getMessageId());
            historyMessages.add(new UserMessage(message.getUserMessage()));

            OpenAiChatOptions options = OpenAiChatOptions.builder()
                    .model(chatModel)
                    .reasoningEffort(reasoningEffort)
                    .build();

            chatClient.prompt()
                    .system(buildSystemPrompt(user.getStage()))
                    .messages(historyMessages)
                    .options(options)
                    .stream()
                    .chatResponse()
                    .doOnNext(response -> {
                        if (redisComponent.hasCancelMessage(user.getUserId(), message.getMessageId())) {
                            throw new RuntimeException("用户取消");
                        }
                        if (response.getResults() == null || response.getResults().isEmpty()) {
                            return;
                        }
                        String content = response.getResults().get(0).getOutput().getText();
                        if (!StringTools.isEmpty(content)) {
                            answer.append(content);
                            push.setType("outputting");
                            push.setContent(content);
                            channelContextUtils.sendMessage(user.getUserId(), JSON.toJSONString(push));
                        }
                    })
                    .doOnComplete(() -> finishMessage(user, message, answer.toString(), true, null))
                    .doOnError(error -> finishMessage(user, message, answer.toString(), false, error))
                    .subscribe();
        } catch (Exception e) {
            log.error("AI 对话流式调用失败", e);
            finishMessage(user, message, answer.toString(), false, e);
        }
    }

    private void finishMessage(TokenUserInfoDTO user, AgentMessage message, String answer, boolean completed, Throwable error) {
        boolean cancelled = redisComponent.hasCancelMessage(user.getUserId(), message.getMessageId());
        AgentMessagePushDTO push = new AgentMessagePushDTO();
        push.setMessageId(message.getMessageId());
        push.setSessionId(message.getSessionId());

        AgentMessage updateBean = new AgentMessage();
        updateBean.setAssistantMessage(answer);
        updateBean.setUpdateTime(new Date());
        String errorInfo = extractError(error);

        if (cancelled) {
            push.setType("done");
            push.setContent(answer);
            channelContextUtils.sendMessage(user.getUserId(), JSON.toJSONString(push));
            updateBean.setStatus(2);
            updateBean.setErrorInfo("用户取消");
            redisComponent.removeCancelMessage(user.getUserId(), message.getMessageId());
        } else if (completed) {
            push.setType("done");
            push.setContent(answer);
            channelContextUtils.sendMessage(user.getUserId(), JSON.toJSONString(push));
            updateBean.setStatus(1);
        } else {
            push.setType("error");
            push.setContent(StringTools.isEmpty(answer) ? "AI 生成失败：" + errorInfo : answer);
            channelContextUtils.sendMessage(user.getUserId(), JSON.toJSONString(push));
            updateBean.setStatus(3);
            updateBean.setErrorInfo(errorInfo);
        }
        agentMessageService.updateAgentMessageByMessageId(updateBean, message.getMessageId());
    }

    private String extractError(Throwable error) {
        if (error == null) {
            return "AI 调用失败";
        }
        String detail = error.getMessage();
        if (StringTools.isEmpty(detail)) {
            return "AI 调用失败";
        }
        return detail.length() > 200 ? detail.substring(0, 200) : detail;
    }

    private List<Message> buildHistory(String userId, String sessionId, String currentMessageId) {
        AgentMessageQuery query = new AgentMessageQuery();
        query.setUserId(userId);
        query.setSessionId(sessionId);
        query.setOrderBy("create_time asc");
        List<AgentMessage> messageList = agentMessageService.findListByParam(query);

        List<Message> historyMessages = new ArrayList<>();
        for (AgentMessage item : messageList) {
            if (item.getMessageId().equals(currentMessageId)) {
                continue;
            }
            if (item.getStatus() != null && item.getStatus() == 1 && !StringTools.isEmpty(item.getAssistantMessage())) {
                historyMessages.add(new UserMessage(item.getUserMessage()));
                historyMessages.add(new AssistantMessage(item.getAssistantMessage()));
                if (historyMessages.size() >= HISTORY_LIMIT * 2) {
                    break;
                }
            }
        }
        return historyMessages;
    }

    private AgentSession resolveSession(TokenUserInfoDTO user, String sessionId) {
        if (StringTools.isEmpty(sessionId)) {
            return createSession(user);
        }
        AgentSession session = agentSessionService.getAgentSessionBySessionId(sessionId);
        if (session == null || !user.getUserId().equals(session.getUserId())) {
            throw new BusinessException("会话不存在或已删除");
        }
        return session;
    }

    private void updateSession(AgentSession session, String userMessage) {
        AgentSession updateBean = new AgentSession();
        updateBean.setMessageCount((session.getMessageCount() == null ? 0 : session.getMessageCount()) + 1);
        updateBean.setLastMessageTime(new Date());
        updateBean.setUpdateTime(new Date());
        if ((session.getMessageCount() == null || session.getMessageCount() == 0)
                && (StringTools.isEmpty(session.getTitle()) || "新对话".equals(session.getTitle()))) {
            updateBean.setTitle(userMessage.length() > 20 ? userMessage.substring(0, 20) : userMessage);
        }
        agentSessionService.updateAgentSessionBySessionId(updateBean, session.getSessionId());
    }

    private String buildSystemPrompt(String stage) {
        String stageDesc = "未知学段";
        for (StageEnum item : StageEnum.values()) {
            if (item.getCode().equals(stage)) {
                stageDesc = item.getDesc();
                break;
            }
        }
        return "你是 K12 人工智能通识课的 AI 助教。学生当前学段：" + stageDesc
                + "。请使用适合该学段的语言和深度回答，讲解清晰、鼓励式、不编造知识，始终使用中文回答。";
    }

    private String generateId() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
