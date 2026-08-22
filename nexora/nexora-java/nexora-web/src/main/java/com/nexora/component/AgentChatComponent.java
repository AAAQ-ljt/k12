package com.nexora.component;

import com.alibaba.fastjson2.JSON;
import com.nexora.dto.AgentMessagePushDTO;
import com.nexora.entity.dto.TokenUserInfoDTO;
import com.nexora.entity.po.AgentMessage;
import com.nexora.entity.po.AgentSession;
import com.nexora.entity.po.AiGenerationRecord;
import com.nexora.entity.po.ResourceDirectory;
import com.nexora.entity.po.ResourceInfo;
import com.nexora.entity.query.AgentMessageQuery;
import com.nexora.entity.query.AgentSessionQuery;
import com.nexora.exception.BusinessException;
import com.nexora.service.AgentMessageService;
import com.nexora.service.AgentSessionService;
import com.nexora.service.AiGenerationRecordService;
import com.nexora.service.ResourceInfoService;
import com.nexora.service.StudentKnowledgeBaseService;
import com.nexora.utils.StringTools;
import com.nexora.vo.ResourceRecommendVO;
import com.nexora.websocket.ChannelContextUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.content.Media;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeType;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
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

    @Resource
    private AnimationScriptComponent animationScriptComponent;

    @Resource
    private QuizGenerateComponent quizGenerateComponent;

    @Resource
    private AiGenerationRecordService aiGenerationRecordService;

    @Resource
    private ResourceInfoService resourceInfoService;

    @Resource
    private StudentKnowledgeBaseService studentKnowledgeBaseService;

    @Value("${spring.ai.openai.chat.options.model:deepseek-v4-flash}")
    private String chatModel;

    @Value("${project.ai.vision-model:deepseek-v4-flash-vision-exp}")
    private String visionModel;

    @Value("${project.folder}")
    private String projectFolder;

    @Value("${spring.ai.openai.chat.options.reasoning-effort:high}")
    private String reasoningEffort;

    public AgentMessage sendMessage(TokenUserInfoDTO user, String sessionId, String userMessage, List<String> imageResourceIds) {
        AgentSession session = resolveSession(user, sessionId);

        // 校验并解析随消息图片（个人库 IMAGE 资源），带图时走视觉模型
        List<String> imageDataUrls = new ArrayList<>();
        if (imageResourceIds != null && !imageResourceIds.isEmpty()) {
            for (String resourceId : imageResourceIds) {
                ResourceInfo resource = resourceInfoService.getResourceInfoByResourceId(resourceId);
                if (resource == null || !user.getUserId().equals(resource.getOwnerId())
                        || !"IMAGE".equalsIgnoreCase(resource.getResourceType())
                        || resource.getStatus() == null || resource.getStatus() != 1) {
                    throw new BusinessException("图片资源不存在或无权使用");
                }
                String dataUrl = imageToDataUrl(resource);
                if (dataUrl != null) {
                    imageDataUrls.add(dataUrl);
                }
            }
        }

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

        if (!imageDataUrls.isEmpty()) {
            // 用户消息携带图片引用（bizData=图片资源ID JSON），历史重放可展示缩略图
            AgentMessage imageUpdate = new AgentMessage();
            imageUpdate.setBizType("USER_IMAGE");
            imageUpdate.setBizData(JSON.toJSONString(imageResourceIds.stream().distinct().toList()));
            imageUpdate.setUpdateTime(new Date());
            agentMessageService.updateAgentMessageByMessageId(imageUpdate, message.getMessageId());
        }

        updateSession(session, userMessage);
        List<String> finalImages = imageDataUrls;
        ASYNC_EXECUTOR.execute(() -> assistantAnswer(user, session, message, finalImages));
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

    private void assistantAnswer(TokenUserInfoDTO user, AgentSession session, AgentMessage message, List<String> imageDataUrls) {
        AgentMessagePushDTO push = new AgentMessagePushDTO();
        push.setMessageId(message.getMessageId());
        push.setSessionId(session.getSessionId());
        StringBuilder answer = new StringBuilder();
        try {
            if (redisComponent.hasCancelMessage(user.getUserId(), message.getMessageId())) {
                finishMessage(user, message, "", false, null, List.of(), 0, 0);
                return;
            }

            IntentAnalyzerComponent.IntentResult intentResult = intentAnalyzerComponent.analyze(
                    message.getUserMessage(), user.getStage());
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
                finishMessage(user, message, "", false, null, List.of(),
                        intentResult.promptTokens(), intentResult.completionTokens());
                return;
            }

            // 动画讲解：生成分步 SVG 脚本产物并推送卡片；生成失败降级为文字讲解
            if ("ANIMATION".equals(intent)) {
                if (handleAnimationAnswer(user, message, intentResult, push)) {
                    return;
                }
                log.warn("动画生成失败，降级为文字讲解");
                degradeToChat(message);
                intent = "CHAT";
            }

            // 对话内出题：生成选择测验卡片；生成失败降级为文字出题
            if ("QUIZ".equals(intent)) {
                if (handleQuizAnswer(user, message, intentResult, push)) {
                    return;
                }
                log.warn("出题生成失败，降级为文字出题");
                degradeToChat(message);
                intent = "CHAT";
            }

            List<Message> historyMessages = buildHistory(user.getUserId(), session.getSessionId(), message.getMessageId());
            boolean withImage = imageDataUrls != null && !imageDataUrls.isEmpty();
            if (withImage) {
                UserMessage.Builder userMessageBuilder = UserMessage.builder().text(message.getUserMessage());
                for (String dataUrl : imageDataUrls) {
                    userMessageBuilder.media(new Media(MimeType.valueOf("image/png"), URI.create(dataUrl)));
                }
                historyMessages.add(userMessageBuilder.build());
            } else {
                historyMessages.add(new UserMessage(message.getUserMessage()));
            }

            OpenAiChatOptions.Builder optionsBuilder = OpenAiChatOptions.builder()
                    .model(withImage ? visionModel : chatModel);
            // 视觉模型不传 reasoning-effort（避免不支持的参数导致 400）
            if (!withImage) {
                optionsBuilder.reasoningEffort(reasoningEffort);
            }
            OpenAiChatOptions options = optionsBuilder.build();

            RagSearchComponent.RagSearchResult ragResult =
                    shouldSearch(intent) ? ragSearchComponent.buildRagResult(user.getUserId(), user.getStage(), message.getUserMessage())
                            : new RagSearchComponent.RagSearchResult("", List.of());
            List<ResourceRecommendVO> recommends = ragResult.recommendations();
            String systemPrompt = resolvePromptWithRag(user, intent, ragResult.ragData());
            sendRecommendPush(user, message, recommends);
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
                    .doOnComplete(() -> finishMessage(user, message, answer.toString(), true, null, recommends,
                            promptTokens.get(), completionTokens.get()))
                    .doOnError(error -> finishMessage(user, message, answer.toString(), false, error, List.of(),
                            promptTokens.get(), completionTokens.get()))
                    .subscribe();
        } catch (Exception e) {
            log.error("AI 对话流式调用失败", e);
            finishMessage(user, message, answer.toString(), false, e, List.of(), 0, 0);
        }
    }

    /**
     * 动画讲解产物链路：生成分步 SVG 脚本 → 落 ai_generation_record → 推送 ANIMATION 卡片完成消息。
     * 返回 true 表示产物已推送；false 表示生成失败（调用方降级为文字讲解）
     */
    private boolean handleAnimationAnswer(TokenUserInfoDTO user, AgentMessage message,
                                          IntentAnalyzerComponent.IntentResult intent, AgentMessagePushDTO push) {
        try {
            AnimationScriptComponent.AnimationScript script =
                    animationScriptComponent.generate(user.getStage(), message.getUserMessage());
            String scriptJson = script.toJson();
            Date now = new Date();

            AiGenerationRecord record = new AiGenerationRecord();
            record.setRecordId(UUID.randomUUID().toString().replace("-", ""));
            record.setUserId(user.getUserId());
            record.setStage(user.getStage());
            record.setType("ANIMATION");
            record.setTitle(script.title());
            record.setContent(scriptJson);
            record.setSource(0);
            record.setStatus(1);
            record.setSaved(0);
            record.setAuditStatus(0);
            record.setCreateTime(now);
            record.setUpdateTime(now);
            aiGenerationRecordService.add(record);

            // 动画产物同步落个人知识库（resource_info，ANIMATION 类型，附件目录），独立页可从资源中心回看
            saveAnimationResource(user, script.title(), scriptJson, now);

            String text = "已为你生成动画讲解《" + script.title() + "》，共 " + script.steps().size()
                    + " 步，点击查看动画讲解页 👇";

            AgentMessage update = new AgentMessage();
            update.setAssistantMessage(text);
            update.setStatus(1);
            update.setBizType("ANIMATION");
            update.setBizData(scriptJson);
            update.setPromptTokens(intent.promptTokens());
            update.setCompletionTokens(intent.completionTokens());
            update.setUpdateTime(now);
            agentMessageService.updateAgentMessageByMessageId(update, message.getMessageId());

            push.setType("done");
            push.setContent(text);
            push.setBizType("ANIMATION");
            push.setBizData(scriptJson);
            channelContextUtils.sendMessage(user.getUserId(), JSON.toJSONString(push));
            return true;
        } catch (Exception e) {
            log.error("动画生成失败 messageId={}", message.getMessageId(), e);
            return false;
        }
    }

    /**
     * 对话内出题产物链路：生成选择测验 JSON → 推送 QUIZ 卡片完成消息（答题与判分在前端即时完成）。
     * 返回 true 表示产物已推送；false 表示生成失败（调用方降级为文字出题）
     */
    private boolean handleQuizAnswer(TokenUserInfoDTO user, AgentMessage message,
                                     IntentAnalyzerComponent.IntentResult intent, AgentMessagePushDTO push) {
        try {
            QuizGenerateComponent.QuizScript quiz =
                    quizGenerateComponent.generate(user.getStage(), message.getUserMessage());
            String quizJson = quiz.toJson();
            String text = "我出好了《" + quiz.title() + "》，共 " + quiz.questions().size()
                    + " 道题，点击下方卡片开始作答吧 ✍️";

            Date now = new Date();
            AgentMessage update = new AgentMessage();
            update.setAssistantMessage(text);
            update.setStatus(1);
            update.setBizType("QUIZ");
            update.setBizData(quizJson);
            update.setPromptTokens(intent.promptTokens());
            update.setCompletionTokens(intent.completionTokens());
            update.setUpdateTime(now);
            agentMessageService.updateAgentMessageByMessageId(update, message.getMessageId());

            push.setType("done");
            push.setContent(text);
            push.setBizType("QUIZ");
            push.setBizData(quizJson);
            channelContextUtils.sendMessage(user.getUserId(), JSON.toJSONString(push));
            return true;
        } catch (Exception e) {
            log.error("出题生成失败 messageId={}", message.getMessageId(), e);
            return false;
        }
    }

    private void degradeToChat(AgentMessage message) {
        AgentMessage degradeUpdate = new AgentMessage();
        degradeUpdate.setIntent("CHAT");
        degradeUpdate.setBizType(null);
        degradeUpdate.setBizData(null);
        degradeUpdate.setUpdateTime(new Date());
        agentMessageService.updateAgentMessageByMessageId(degradeUpdate, message.getMessageId());
    }

    /**
     * 动画产物落个人知识库：resource_info(ANIMATION, ext_json=动画脚本, 附件目录)
     */
    private void saveAnimationResource(TokenUserInfoDTO user, String title, String scriptJson, Date now) {
        try {
            ResourceDirectory dir = studentKnowledgeBaseService.getSystemDirectory(
                    user.getUserId(), StudentKnowledgeBaseService.DIR_TYPE_ATTACHMENTS);
            ResourceInfo resource = new ResourceInfo();
            resource.setResourceId(UUID.randomUUID().toString().replace("-", ""));
            resource.setResourceName("动画讲解-" + title);
            resource.setResourceType("ANIMATION");
            resource.setExtJson(scriptJson);
            resource.setDirectoryId(dir == null ? null : dir.getDirId());
            resource.setStage(user.getStage());
            resource.setOwnerId(user.getUserId());
            resource.setSource(1);
            resource.setStatus(1);
            resource.setCreateTime(now);
            resource.setUpdateTime(now);
            resourceInfoService.add(resource);
            log.info("动画产物已存入个人知识库 resourceId={}", resource.getResourceId());
        } catch (Exception e) {
            log.warn("动画产物存入个人知识库失败 userId={}", user.getUserId(), e);
        }
    }

    /**
     * 个人库图片资源 → Base64 Data URL（单图 ≤8MB，供视觉模型 image_url 使用）
     */
    private String imageToDataUrl(ResourceInfo resource) {
        try {
            if (resource == null || StringTools.isEmpty(resource.getFilePath())) {
                return null;
            }
            Path path = Paths.get(projectFolder, resource.getFilePath());
            if (!Files.exists(path)) {
                return null;
            }
            byte[] bytes = Files.readAllBytes(path);
            if (bytes.length > 8 * 1024 * 1024) {
                return null;
            }
            boolean png = resource.getResourceName() != null
                    && resource.getResourceName().toLowerCase().endsWith(".png");
            return "data:" + (png ? "image/png" : "image/jpeg") + ";base64,"
                    + Base64.getEncoder().encodeToString(bytes);
        } catch (Exception e) {
            log.warn("图片转 DataURL 失败 resourceId={}", resource == null ? null : resource.getResourceId(), e);
            return null;
        }
    }

    private void finishMessage(TokenUserInfoDTO user, AgentMessage message, String answer, boolean completed, Throwable error,
                               List<ResourceRecommendVO> recommends, int promptTokens, int completionTokens) {
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
            if (recommends != null && !recommends.isEmpty()) {
                push.setBizType("RESOURCE_RECOMMEND");
                push.setBizData(JSON.toJSONString(recommends));
            }
            channelContextUtils.sendMessage(user.getUserId(), JSON.toJSONString(push));
            updateBean.setStatus(1);
            if (recommends != null && !recommends.isEmpty()) {
                updateBean.setBizType("RESOURCE_RECOMMEND");
                updateBean.setBizData(JSON.toJSONString(recommends));
            }
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

    private String resolvePromptWithRag(TokenUserInfoDTO user, String intent, String ragData) {
        String prompt = promptTemplateComponent.resolvePrompt(user.getStage(), intent);
        if (!shouldSearch(intent) || ragData == null || ragData.isBlank()) {
            return prompt;
        }
        if (prompt.contains("{{ragData}}")) {
            return prompt.replace("{{ragData}}", ragData);
        }
        return prompt + "\n\n## 知识库参考内容\n" + ragData
                + "\n\n回答时优先基于以上内容，并标注来源；如果知识库没有相关内容，明确告知用户，不要编造。";
    }

    private void sendRecommendPush(TokenUserInfoDTO user, AgentMessage message,
                                   List<ResourceRecommendVO> recommends) {
        if (recommends == null || recommends.isEmpty()) {
            return;
        }
        AgentMessagePushDTO push = new AgentMessagePushDTO();
        push.setMessageId(message.getMessageId());
        push.setSessionId(message.getSessionId());
        push.setType("recommend");
        push.setBizType("RESOURCE_RECOMMEND");
        push.setBizData(JSON.toJSONString(recommends));
        channelContextUtils.sendMessage(user.getUserId(), JSON.toJSONString(push));
    }

    private boolean shouldSearch(String intent) {
        if (intent == null) {
            return true;
        }
        // 理科求解直接走模型能力，不检索本地知识库；生成类意图同样跳过检索
        return switch (intent) {
            case "PICTURE_BOOK", "DRAW", "ANIMATION", "CODING", "SCIENCE_SOLVE" -> false;
            default -> true;
        };
    }

    private String generateId() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
