package com.nexora.component;

import com.alibaba.fastjson2.JSON;
import com.nexora.dto.AgentMessagePushDTO;
import com.nexora.dto.PictureBookTaskVO;
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
import com.nexora.service.PictureBookTaskService;
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
import org.springframework.ai.tool.ToolCallback;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * AI 对话核心组件：落库、组装上下文、大模型流式回复（供应商可切换）、WebSocket 推送、取消与错误处理
 */
@Component
@Slf4j
public class AgentChatComponent {

    private static final ExecutorService ASYNC_EXECUTOR = Executors.newFixedThreadPool(4);

    private static final int HISTORY_LIMIT = 10;

    /**
     * 知识库引用规则：官方课程知识库（管理端维护）与学生个人知识库的口径必须分开，
     * 否则模型会把平台教材资料说成"你提供的知识库"。
     * 来源分组由 RagSearchComponent 组装参考内容时打在数据上，本段只负责约束说法。
     */
    private static final String RAG_CITATION_RULE = """
            ## 知识库引用规则（必须遵守）
            1. 上方参考内容已按来源分组标注，引用时必须如实区分，不得混为一谈：
               - 「平台课程知识库」：学校/平台在管理端后台统一维护的教材与课程资料，面向全体学生，不是学生本人上传的；
               - 「学生个人知识库」：学生本人上传或整理的知识页。
            2. 引用平台课程知识库的内容时，说成"根据平台课程知识库资料《标题》"这类表述；
               严禁说成"你提供的""你的知识库""你上传的""你自己的资料"。
            3. 只有内容确实来自学生个人知识库时，才可以用"你的个人知识库/你上传的资料"这类说法。
            4. 无法判断某条内容属于哪一类时，只说"根据知识库资料"，不要臆断归属。
            5. 优先基于上方参考内容回答，来源标题照抄不要改写；知识库没有相关内容时如实说明，不要编造。""";

    /** 无检索命中时的归属护栏：模型只靠自身知识回答，也不能声称内容来自学生提供的知识库 */
    private static final String RAG_ATTRIBUTION_GUARD = """
            补充约束：本轮没有提供知识库参考内容，不要声称内容来自学生的个人知识库或其上传的资料，
            也不要编造资料名称。""";

    /** 动画概念解析取的最近完成问答轮数 */
    private static final int CONCEPT_HISTORY_LIMIT = 6;

    /** 指代型动画指令的特征词：命中才做"最近对话 -> 具体概念"解析，明确点名概念的指令走原路径 */
    private static final List<String> CONCEPT_REFERENCE_WORDS = List.of(
            "刚才", "刚刚", "上面", "前面", "之前", "上次", "以上", "接着", "继续", "对话中");

    /**
     * emoji 占位符还原映射：模型偶发把 emoji 输出成 [right]/[star] 这类文本占位符
     * （2026-10-02 实测 DeepSeek 把"👉"输出成 "[right]" 落进回复），推送与落库前统一还原；
     * 不在映射内的方括号文本一律不动，避免误伤 Markdown 链接等语法。
     */
    private static final Map<String, String> EMOJI_PLACEHOLDERS = Map.ofEntries(
            Map.entry("right", "👉"), Map.entry("point_right", "👉"),
            Map.entry("left", "👈"), Map.entry("up", "👆"), Map.entry("down", "👇"),
            Map.entry("star", "⭐"), Map.entry("heart", "❤️"), Map.entry("fire", "🔥"),
            Map.entry("clap", "👏"), Map.entry("check", "✅"), Map.entry("checkmark", "✅"),
            Map.entry("cross", "❌"), Map.entry("wrong", "❌"), Map.entry("warn", "⚠️"),
            Map.entry("warning", "⚠️"), Map.entry("bulb", "💡"), Map.entry("idea", "💡"),
            Map.entry("think", "🤔"), Map.entry("thinking", "🤔"), Map.entry("ok", "👌"),
            Map.entry("sparkles", "✨"), Map.entry("tada", "🎉"), Map.entry("book", "📖"));

    /** emoji 占位符匹配：[xx] 后面紧跟 "(" 或 ":" 时可能是 Markdown 链接语法，不做替换 */
    private static final Pattern EMOJI_PLACEHOLDER_PATTERN = Pattern.compile("\\[([a-zA-Z_]{2,20})\\](?![(:])");

    @Resource
    private ChatProvider chatProvider;

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
    private PictureBookTaskService pictureBookTaskService;

    @Resource
    private AiGenerationRecordService aiGenerationRecordService;

    @Resource
    private ResourceInfoService resourceInfoService;

    @Resource
    private StudentKnowledgeBaseService studentKnowledgeBaseService;

    @Resource
    private KnowledgeAgentToolComponent knowledgeAgentToolComponent;

    // 对话模型 / 视觉模型 / reasoning-effort 已收敛到 ChatProvider（见 DeepSeekChatProvider、OpenCodeGoChatProvider）
    @Value("${project.folder}")
    private String projectFolder;

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
        List<String> persistedImageIds = imageResourceIds == null ? List.of() : imageResourceIds;
        ASYNC_EXECUTOR.execute(() -> assistantAnswer(user, session, message, finalImages, persistedImageIds));
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

    private void assistantAnswer(TokenUserInfoDTO user, AgentSession session, AgentMessage message, List<String> imageDataUrls, List<String> imageResourceIds) {
        boolean hasImages = imageResourceIds != null && !imageResourceIds.isEmpty();
        AgentMessagePushDTO push = new AgentMessagePushDTO();
        push.setMessageId(message.getMessageId());
        push.setSessionId(session.getSessionId());
        StringBuilder answer = new StringBuilder();
        try {
            if (redisComponent.hasCancelMessage(user.getUserId(), message.getMessageId())) {
                finishMessage(user, message, "", false, null, List.of(), 0, 0, false);
                return;
            }

            IntentAnalyzerComponent.IntentResult intentResult = intentAnalyzerComponent.analyze(
                    message.getUserMessage(), user.getStage());
            String intent = intentResult.intent();
            // 带图消息守卫：意图分类只看文本，"图片里是什么"这类看图提问会被误判成创作型意图，
            // 在到达下方视觉问答链路前就被绘本/动画任务拦截。带图时创作型意图一律降级 CHAT 走视觉回答；
            // 真要创作可去「绘本生成」页，或发不带图的明确指令
            if (hasImages && ("PICTURE_BOOK".equals(intent) || "ANIMATION".equals(intent))) {
                log.info("带图消息命中创作型意图 {}，降级 CHAT 走视觉问答: {}", intent, message.getUserMessage());
                intent = "CHAT";
            }
            String bizType = mapIntentToBizType(intent);
            String bizData = intentResult.data() == null ? null : JSON.toJSONString(intentResult.data());

            AgentMessage intentUpdate = new AgentMessage();
            intentUpdate.setIntent(intent);
            // 带图消息：保留 USER_IMAGE 的 bizType/bizData（图片资源ID 数组），供历史重放渲染缩略图；
            // 意图识别出的知识点信息仅在无图消息上落库
            if (!hasImages) {
                intentUpdate.setBizType(bizType);
                intentUpdate.setBizData(bizData);
            }
            intentUpdate.setPromptTokens(intentResult.promptTokens());
            intentUpdate.setCompletionTokens(intentResult.completionTokens());
            intentUpdate.setUpdateTime(new Date());
            agentMessageService.updateAgentMessageByMessageId(intentUpdate, message.getMessageId());

            if (redisComponent.hasCancelMessage(user.getUserId(), message.getMessageId())) {
                finishMessage(user, message, "", false, null, List.of(),
                        intentResult.promptTokens(), intentResult.completionTokens(), hasImages);
                return;
            }

            // 动画讲解：生成分步 SVG 脚本产物并推送卡片；生成失败降级为文字讲解
            if ("ANIMATION".equals(intent)) {
                if (handleAnimationAnswer(user, session, message, intentResult, push)) {
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

            // 对话内绘本：提交异步生成任务并推送进度卡片（与「绘本生成」页共用状态机，前端按 taskId 轮询）；提交失败降级为文字讲解
            if ("PICTURE_BOOK".equals(intent)) {
                if (handlePictureBookAnswer(user, message, intentResult, push)) {
                    return;
                }
                log.warn("绘本任务提交失败，降级为文字讲解");
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

            // 本轮供应商快照：客户端与模型名必须取自同一供应商，避免切换瞬间出现客户端与模型名错配
            ChatProvider provider = chatProvider.current();
            OpenAiChatOptions.Builder optionsBuilder = OpenAiChatOptions.builder()
                    .model(withImage ? provider.visionModel() : provider.textModel());
            // 是否传 reasoning-effort 由供应商决定（DeepSeek 视觉模型不接受该参数，GLM 带图也必须传 low）
            String effort = provider.reasoningEffort(withImage);
            if (!StringTools.isEmpty(effort)) {
                optionsBuilder.reasoningEffort(effort);
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

            // 知识页工具：MCP 未启用时返回空数组，对话照常降级（挂工具后模型可列表/读取/新建/覆盖/入库学生个人知识页）
            ToolCallback[] knowledgeTools = knowledgeAgentToolComponent.buildCallbacks();
            AtomicInteger wikiOps = new AtomicInteger();
            ChatClient.ChatClientRequestSpec requestSpec = provider.chatClient().prompt()
                    .system(systemPrompt)
                    .messages(historyMessages)
                    .options(options);
            if (knowledgeTools.length > 0) {
                Map<String, Object> toolContext = new HashMap<>();
                toolContext.put(KnowledgeAgentToolComponent.CTX_USER_ID, user.getUserId());
                toolContext.put(KnowledgeAgentToolComponent.CTX_STAGE, user.getStage());
                toolContext.put(KnowledgeAgentToolComponent.CTX_WIKI_OPS, wikiOps);
                KnowledgeAgentToolComponent.setFallbackContext(toolContext);
                requestSpec = requestSpec.toolCallbacks(knowledgeTools).toolContext(toolContext);
            }

            requestSpec
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
                    .doOnComplete(() -> {
                        KnowledgeAgentToolComponent.clearFallbackContext();
                        finishMessage(user, message, answer.toString(), true, null, recommends,
                                promptTokens.get(), completionTokens.get(), wikiOps.get(), hasImages);
                    })
                    .doOnError(error -> {
                        KnowledgeAgentToolComponent.clearFallbackContext();
                        finishMessage(user, message, answer.toString(), false, error, List.of(),
                                promptTokens.get(), completionTokens.get(), wikiOps.get(), hasImages);
                    })
                    .subscribe();
        } catch (Exception e) {
            log.error("AI 对话流式调用失败", e);
            KnowledgeAgentToolComponent.clearFallbackContext();
            finishMessage(user, message, answer.toString(), false, e, List.of(), 0, 0, hasImages);
        }
    }

    /**
     * 动画讲解产物链路：生成分步 SVG 脚本 → 落 ai_generation_record → 推送 ANIMATION 卡片完成消息。
     * "把刚才讲的做成动画"这类指代型指令本身没有主题，先生成前结合最近对话把概念解析出来，
     * 否则动画主题就会变成这句指令本身（2026-10-02 实测生成过"把讲解内容变成动画"的元动画）。
     * 返回 true 表示产物已推送；false 表示生成失败（调用方降级为文字讲解）
     */
    private boolean handleAnimationAnswer(TokenUserInfoDTO user, AgentSession session, AgentMessage message,
                                          IntentAnalyzerComponent.IntentResult intent, AgentMessagePushDTO push) {
        try {
            String concept = resolveAnimationConcept(user, session, message);
            AnimationScriptComponent.AnimationScript script =
                    animationScriptComponent.generate(user.getStage(), concept);
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
     * 动画概念解析入口：指令点名了具体概念（如"生成冒泡排序的动画讲解"）时原样返回、零额外开销；
     * 命中指代词（把刚才/刚刚/上面讲的内容做成动画）时结合最近对话解析成具体概念，解析失败兜底原始指令
     */
    private String resolveAnimationConcept(TokenUserInfoDTO user, AgentSession session, AgentMessage message) {
        String userMessage = message.getUserMessage();
        boolean referential = userMessage != null && CONCEPT_REFERENCE_WORDS.stream().anyMatch(userMessage::contains);
        if (!referential) {
            return userMessage;
        }
        String transcript = buildRecentTranscript(user.getUserId(), session.getSessionId(), message.getMessageId());
        if (StringTools.isEmpty(transcript)) {
            log.warn("动画概念解析缺少历史对话，回退原始指令: {}", userMessage);
            return userMessage;
        }
        return animationScriptComponent.resolveConcept(user.getStage(), userMessage, transcript);
    }

    /**
     * 最近完成问答的文本稿（正序、每侧截断），供指代型动画指令解析真实概念；取材口径与 buildHistory 一致
     */
    private String buildRecentTranscript(String userId, String sessionId, String currentMessageId) {
        AgentMessageQuery query = new AgentMessageQuery();
        query.setUserId(userId);
        query.setSessionId(sessionId);
        query.setOrderBy("create_time asc");
        List<AgentMessage> messageList = agentMessageService.findListByParam(query);
        List<AgentMessage> doneList = new ArrayList<>();
        for (AgentMessage item : messageList) {
            if (item.getMessageId().equals(currentMessageId)) {
                continue;
            }
            if (item.getStatus() != null && item.getStatus() == 1 && !StringTools.isEmpty(item.getAssistantMessage())) {
                doneList.add(item);
            }
        }
        StringBuilder transcript = new StringBuilder();
        int from = Math.max(0, doneList.size() - CONCEPT_HISTORY_LIMIT);
        for (int i = from; i < doneList.size(); i++) {
            AgentMessage item = doneList.get(i);
            transcript.append("学生：").append(truncateForTranscript(item.getUserMessage(), 200)).append('\n');
            transcript.append("AI：").append(truncateForTranscript(item.getAssistantMessage(), 600)).append('\n');
        }
        return transcript.toString();
    }

    /** 文本稿截断：压平空白防表格/公式撑爆提示词 */
    private String truncateForTranscript(String text, int max) {
        if (StringTools.isEmpty(text)) {
            return "";
        }
        String flat = text.replaceAll("\\s+", " ").trim();
        return flat.length() > max ? flat.substring(0, max) + "…" : flat;
    }

    /**
     * 把模型偶发输出的 [right]/[star] 等 emoji 文本占位符还原为真实 emoji；
     * 不在映射内的方括号文本保持原样（可能是 Markdown 链接或普通标注）
     */
    private static String restoreEmojiPlaceholders(String text) {
        if (StringTools.isEmpty(text) || text.indexOf('[') < 0) {
            return text;
        }
        Matcher matcher = EMOJI_PLACEHOLDER_PATTERN.matcher(text);
        StringBuilder result = new StringBuilder();
        while (matcher.find()) {
            String emoji = EMOJI_PLACEHOLDERS.get(matcher.group(1).toLowerCase());
            matcher.appendReplacement(result, Matcher.quoteReplacement(emoji == null ? matcher.group() : emoji));
        }
        matcher.appendTail(result);
        return result.toString();
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

    /**
     * 对话内绘本产物链路：提交绘本异步生成任务（与「绘本生成」页共用 Redis 状态机与队列，主题净化在消费端执行），
     * 推送 PICTURE_BOOK 卡片消息（前端按 taskId 轮询进度、完成展示「查看绘本」）。
     * 返回 true 表示任务已提交并推送；false 表示提交失败（调用方降级为文字讲解）
     */
    private boolean handlePictureBookAnswer(TokenUserInfoDTO user, AgentMessage message,
                                            IntentAnalyzerComponent.IntentResult intent, AgentMessagePushDTO push) {
        try {
            String topic = message.getUserMessage() == null ? "" : message.getUserMessage().trim();
            if (topic.isEmpty()) {
                topic = "我的AI小故事";
            }
            PictureBookTaskVO task = pictureBookTaskService.submit(user.getUserId(), user.getStage(), topic, null);

            Map<String, Object> bizData = new HashMap<>();
            bizData.put("taskId", task.getTaskId());
            bizData.put("topic", topic);
            bizData.put("status", task.getStatus());
            String bizJson = JSON.toJSONString(bizData);

            String text = "收到！已开始为你创作绘本，AI 正在编写故事并绘制插图（通常需要几分钟）。"
                    + "进度就在下方卡片里，切到其他页面也不会中断；完成后卡片会变成「查看绘本」～";

            Date now = new Date();
            AgentMessage update = new AgentMessage();
            update.setAssistantMessage(text);
            update.setStatus(1);
            update.setBizType("PICTURE_BOOK");
            update.setBizData(bizJson);
            update.setPromptTokens(intent.promptTokens());
            update.setCompletionTokens(intent.completionTokens());
            update.setUpdateTime(now);
            agentMessageService.updateAgentMessageByMessageId(update, message.getMessageId());

            push.setType("done");
            push.setContent(text);
            push.setBizType("PICTURE_BOOK");
            push.setBizData(bizJson);
            channelContextUtils.sendMessage(user.getUserId(), JSON.toJSONString(push));
            return true;
        } catch (Exception e) {
            log.error("绘本任务提交失败 messageId={}", message.getMessageId(), e);
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
                               List<ResourceRecommendVO> recommends, int promptTokens, int completionTokens, boolean hasImages) {
        finishMessage(user, message, answer, completed, error, recommends, promptTokens, completionTokens, 0, hasImages);
    }

    /**
     * @param wikiOps 本轮知识页工具被调用次数，&gt;0 时给消息打 WIKI 标记（前端据此刷新知识页抽屉）
     * @param hasImages 消息携带用户上传图片：持久化时保留 USER_IMAGE 的 bizType/bizData，避免被推荐/知识页标记覆写
     */
    private void finishMessage(TokenUserInfoDTO user, AgentMessage message, String answer, boolean completed, Throwable error,
                               List<ResourceRecommendVO> recommends, int promptTokens, int completionTokens, int wikiOps, boolean hasImages) {
        // 模型偶发输出 [right] 等 emoji 占位符，最终推送与落库前统一还原为真实 emoji
        answer = restoreEmojiPlaceholders(answer);
        boolean cancelled = redisComponent.hasCancelMessage(user.getUserId(), message.getMessageId());
        // 本轮动过知识页：完成/取消/失败都要标记，前端收到 WIKI 标记即刷新抽屉（数据已变化）
        boolean wikiChanged = wikiOps > 0;
        String wikiBizData = "{\"ops\":" + wikiOps + "}";
        AgentMessagePushDTO push = new AgentMessagePushDTO();
        push.setMessageId(message.getMessageId());
        push.setSessionId(message.getSessionId());
        if (wikiChanged) {
            push.setBizType("WIKI");
            push.setBizData(wikiBizData);
        }

        AgentMessage updateBean = new AgentMessage();
        updateBean.setAssistantMessage(answer);
        updateBean.setPromptTokens(promptTokens);
        updateBean.setCompletionTokens(completionTokens);
        updateBean.setUpdateTime(new Date());
        if (wikiChanged && !hasImages) {
            updateBean.setBizType("WIKI");
            updateBean.setBizData(wikiBizData);
        }
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
            // 同轮既有推荐卡片又有知识页操作时以 WIKI 标记为准（推荐卡片已由独立 recommend 事件推送）
            if (!wikiChanged && recommends != null && !recommends.isEmpty()) {
                push.setBizType("RESOURCE_RECOMMEND");
                push.setBizData(JSON.toJSONString(recommends));
            }
            channelContextUtils.sendMessage(user.getUserId(), JSON.toJSONString(push));
            updateBean.setStatus(1);
            // 带图消息：落库保留 USER_IMAGE 标记（推荐卡片仅实时推送，不入库覆写图片引用）
            if (!wikiChanged && !hasImages && recommends != null && !recommends.isEmpty()) {
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
        if (!shouldSearch(intent)) {
            return prompt;
        }
        if (ragData == null || ragData.isBlank()) {
            return prompt + "\n\n" + RAG_ATTRIBUTION_GUARD;
        }
        // 模板自带 {{ragData}} 占位符时只替换数据、规则照旧追加，避免启用占位符的模板绕过来源口径约束
        String promptWithRag = prompt.contains("{{ragData}}")
                ? prompt.replace("{{ragData}}", ragData)
                : prompt + "\n\n## 知识库参考内容（按来源分组，引用时请如实区分）\n" + ragData;
        return promptWithRag + "\n\n" + RAG_CITATION_RULE;
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
