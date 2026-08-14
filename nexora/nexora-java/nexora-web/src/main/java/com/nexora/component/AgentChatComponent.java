package com.nexora.component;

import com.alibaba.fastjson2.JSON;
import com.nexora.dto.AgentMessagePushDTO;
import com.nexora.entity.dto.TokenUserInfoDTO;
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
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

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

    @Resource
    private IntentAnalyzerComponent intentAnalyzerComponent;

    @Resource
    private PromptTemplateComponent promptTemplateComponent;

    @Resource
    private RagSearchComponent ragSearchComponent;

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
            if (redisComponent.hasCancelMessage(user.getUserId(), message.getMessageId())) {
                finishMessage(user, message, "", false, null, 0, 0);
                return;
            }

            IntentAnalyzerComponent.IntentResult intentResult = intentAnalyzerComponent.analyze(message.getUserMessage());
            String intent = intentResult.intent();
            String bizType = mapIntentToBizType(intent);
            String bizData = intentResult.data() == null ? null : JSON.toJSONString(intentResult.data());

            AgentMessage intentUpdate = new AgentMessage();
            intentUpdate.setIntent(intent);
            intentUpdate.setBizType(bizType);
            intentUpdate.setBizData(bizData);
            intentUpdate.setPromptTokens(intentResult.promptTokens());
            intentUpdate.setCompletionTokens(intentResult.completionTokens());
            intentUpdate.setUpdateTime(new Date());
            agentMessageService.updateAgentMessageByMessageId(intentUpdate, message.getMessageId());

            if (redisComponent.hasCancelMessage(user.getUserId(), message.getMessageId())) {
                finishMessage(user, message, "", false, null,
                        intentResult.promptTokens(), intentResult.completionTokens());
                return;
            }

            List<Message> historyMessages = buildHistory(user.getUserId(), session.getSessionId(), message.getMessageId());
            historyMessages.add(new UserMessage(message.getUserMessage()));

            OpenAiChatOptions options = OpenAiChatOptions.builder()
                    .model(chatModel)
                    .reasoningEffort(reasoningEffort)
                    .build();

            String systemPrompt = resolvePromptWithRag(user, intent, message.getUserMessage());
            AtomicInteger promptTokens = new AtomicInteger(intentResult.promptTokens());
            AtomicInteger completionTokens = new AtomicInteger(intentResult.completionTokens());

            chatClient.prompt()
                    .system(systemPrompt)
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
                        if (response.getMetadata() != null && response.getMetadata().getUsage() != null) {
                            Usage usage = response.getMetadata().getUsage();
                            if (usage.getPromptTokens() != null) {
                                promptTokens.set(usage.getPromptTokens());
                            }
                            if (usage.getCompletionTokens() != null) {
                                completionTokens.set(usage.getCompletionTokens());
                            }
                        }
                        String content = response.getResults().get(0).getOutput().getText();
                        if (!StringTools.isEmpty(content)) {
                            answer.append(content);
                            push.setType("outputting");
                            push.setContent(content);
                            channelContextUtils.sendMessage(user.getUserId(), JSON.toJSONString(push));
                        }
                    })
                    .doOnComplete(() -> finishMessage(user, message, answer.toString(), true, null,
                            promptTokens.get(), completionTokens.get()))
                    .doOnError(error -> finishMessage(user, message, answer.toString(), false, error,
                            promptTokens.get(), completionTokens.get()))
                    .subscribe();
        } catch (Exception e) {
            log.error("AI 对话流式调用失败", e);
            finishMessage(user, message, answer.toString(), false, e, 0, 0);
        }
    }

    private void finishMessage(TokenUserInfoDTO user, AgentMessage message, String answer, boolean completed, Throwable error,
                               int promptTokens, int completionTokens) {
        boolean cancelled = redisComponent.hasCancelMessage(user.getUserId(), message.getMessageId());
        AgentMessagePushDTO push = new AgentMessagePushDTO();
        push.setMessageId(message.getMessageId());
        push.setSessionId(message.getSessionId());

        AgentMessage updateBean = new AgentMessage();
        updateBean.setAssistantMessage(answer);
        updateBean.setPromptTokens(promptTokens);
        updateBean.setCompletionTokens(completionTokens);
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

    private String mapIntentToBizType(String intent) {
        if (intent == null) {
            return null;
        }
        return switch (intent) {
            case "RECOMMEND" -> "RESOURCE_LIST";
            case "QUIZ" -> "QUIZ";
            case "PICTURE_BOOK" -> "PICTURE_BOOK";
            case "ANIMATION" -> "ANIMATION";
            case "CODING" -> "CODE";
            default -> null;
        };
    }

    private String resolvePromptWithRag(TokenUserInfoDTO user, String intent, String question) {
        String prompt = promptTemplateComponent.resolvePrompt(user.getStage(), intent);
        if (!shouldSearch(intent)) {
            return prompt;
        }
        String ragData = ragSearchComponent.buildRagData(user.getStage(), question);
        if (ragData == null || ragData.isBlank()) {
            return prompt;
        }
        if (prompt.contains("{{ragData}}")) {
            return prompt.replace("{{ragData}}", ragData);
        }
        return prompt + "\n\n## 知识库参考内容\n" + ragData
                + "\n\n回答时优先基于以上内容，并标注来源；如果知识库没有相关内容，明确告知用户，不要编造。";
    }

    private boolean shouldSearch(String intent) {
        if (intent == null) {
            return true;
        }
        return switch (intent) {
            case "PICTURE_BOOK", "DRAW", "ANIMATION", "CODING" -> false;
            default -> true;
        };
    }

    private String generateId() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
