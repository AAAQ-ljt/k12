package com.nexora.component;

import com.nexora.component.PreferencePageComponent;
import com.nexora.constants.Constants;
import java.util.concurrent.TimeUnit;

import com.alibaba.fastjson2.JSON;
import com.nexora.dto.AgentMessagePushDTO;
import com.nexora.dto.AnimationTaskVO;
import com.nexora.dto.PictureBookTaskVO;
import com.nexora.entity.dto.TokenUserInfoDTO;
import com.nexora.entity.enums.StageEnum;
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
import com.nexora.service.AnimationTaskService;
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
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

    /**
     * 平台事实约束（常驻注入）：课程 / 课时 / 学习人数 / 难度 / 掌握度 / 知识页清单这类**平台事实**
     * 只能来自本轮工具返回；知识库参考内容只是学习资料，不等于平台的课程清单。
     *
     * 背景（2026-10-07）：学生问「我的三年级学段的课程有哪些」，模型把个人知识页里「介绍语文课程」的内容
     * 当成了平台课程，编造出不存在的《三年级语文》并虚构「188 人在学 / 2 星难度 / 简介」——见开发流程 7.56。
     */
    private static final String PLATFORM_FACT_RULE = """
            ## 平台事实规则（必须遵守，违反即视为答错）
            1. 课程、课时、学习人数、难度、知识点掌握度、知识页清单这类**平台事实**，只能来自本轮工具（MCP）返回的结果：
               - 工具没有返回的课程，一律不得提及，也不得"顺便补充"、不得凭印象或记忆列举；
               - 严禁编造课程名、学习人数、难度星级、课程简介等字段（数字必须与工具返回完全一致）；
               - 用户问「有没有某门课 / 某学段的课程有哪些 / 我加入了哪些课」时，一律以工具返回为准；
                 工具没返回这门课，就如实说明"没有查到这门课"。
            2. 知识库参考内容（含学生个人知识页、平台课程资料、教材原文）**只是学习资料，不是平台的课程清单**：
               不得因为参考资料里出现「语文」「三年级」等字眼，就把它当成平台上存在的一门课程。
            3. 不确定的事实宁可说"我先帮你查一下"，也不要给出看似具体、实则虚构的信息。
            4. **学习数据来源规则**（二期 7.60）：涉及学习路径、节点进度、待复习、知识点掌握度时，
               必须先调工具（queryLearningPath / queryPathNode / planNextStep / queryMastery）拿真实数据再回答；
               - 工具确实不可用（未开启 MCP）时，如实说明"暂时拿不到你的学习数据，可以先按知识点本身讲"，不得编造掌握度/进度/复习时间；
               - 若本轮消息里学生已经说明了自己的学习情况（例如"我正在学这条路径的某个节点、掌握度多少、练习几次"），
                 必须直接引用这些信息作答，**禁止**回答"没有你的数据""我无法访问你的学习档案"；
               - 给"下一步学什么/复习什么"建议时，必须基于工具返回或学生提供的真实清单，不得只讲通用方法。""";

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

    /** 学生画像（计划 C1：对话注入画像速览） */
    /** 用户端提示词规则（计划 C2：学生自定义偏好，第四层注入） */
    @Resource
    private UserPromptRuleComponent userPromptRuleComponent;

    @Resource
    private PreferencePageComponent preferencePageComponent;

    @Resource
    private StudentProfileComponent studentProfileComponent;

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
    private AnimationTaskService animationTaskService;

    @Resource
    private PictureBookGenerateComponent pictureBookGenerateComponent;

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
        return sendMessage(user, sessionId, userMessage, imageResourceIds, null, null);
    }

    /**
     * 发送消息（带场景标记）
     *
     * @param scene        新会话场景（0 自由对话 / 3 编程练习），仅新建会话时生效
     * @param sessionTitle 新会话标题（仅新建会话时生效）
     */
    public AgentMessage sendMessage(TokenUserInfoDTO user, String sessionId, String userMessage,
                                    List<String> imageResourceIds, Integer scene, String sessionTitle) {
        return sendMessage(user, sessionId, userMessage, imageResourceIds, scene, sessionTitle, null);
    }

    /**
     * 发送消息（带学生学习上下文，计划 7.60 收敛项）。
     *
     * learningContext 由学习路径节点等入口带来：服务端把它暂存到 Redis（按消息 ID，TTL 30 分钟），
     * 生成提示词时作为独立段落拼进系统提示词，**不写进消息正文**——气泡里只显示学生的提问本身。
     */
    public AgentMessage sendMessage(TokenUserInfoDTO user, String sessionId, String userMessage,
                                    List<String> imageResourceIds, Integer scene, String sessionTitle,
                                    String learningContext) {
        return sendMessage(user, sessionId, userMessage, imageResourceIds, scene, sessionTitle, learningContext, null);
    }

    /**
     * 发送消息（带学生显式选择的意图，2026-10-08）。
     *
     * preferIntent 由前端「动画讲解」模式 / 动作卡片带入：学生的显式选择必须是权威的，
     * 服务端只做白名单与学段校验后直接采用，不再交给意图分类去猜——
     * 猜错会退化成文字讲解，学生看到的是「我没有生成动画的功能」。
     */
    public AgentMessage sendMessage(TokenUserInfoDTO user, String sessionId, String userMessage,
                                    List<String> imageResourceIds, Integer scene, String sessionTitle,
                                    String learningContext, String preferIntent) {
        AgentSession session = resolveSession(user, sessionId, scene, sessionTitle);

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
        // 学习上下文暂存（按消息 ID）：生成阶段读出来拼提示词用，不进消息正文
        if (!StringTools.isEmpty(learningContext) && !StringTools.isEmpty(message.getMessageId())) {
            try {
                redisComponent.setString(Constants.REDIS_KEY_AGENT_LEARNING_CONTEXT + message.getMessageId(),
                        learningContext.trim(), 30, TimeUnit.MINUTES);
            } catch (Exception e) {
                log.warn("暂存学习上下文失败（本轮不注入上下文）messageId={}", message.getMessageId(), e);
            }
        }
        // 学生显式选择的意图暂存（按消息 ID）：只接受白名单，且小学段不允许动画
        if (!StringTools.isEmpty(preferIntent)) {
            String preferred = preferIntent.trim().toUpperCase();
            boolean allowed = PREFER_INTENT_WHITELIST.contains(preferred);
            // 学段匹配才采纳：动画讲解仅初高中、绘本仅小学（2026-10-08）
            boolean stageBlocked = ("ANIMATION".equals(preferred) && isPrimaryStage(user.getStage()))
                    || ("PICTURE_BOOK".equals(preferred) && !isPrimaryStage(user.getStage()));
            if (allowed && !stageBlocked) {
                try {
                    redisComponent.setString(Constants.REDIS_KEY_AGENT_PREFER_INTENT + message.getMessageId(),
                            preferred, 30, TimeUnit.MINUTES);
                } catch (Exception e) {
                    log.warn("暂存显式意图失败（本轮回落到意图分类）messageId={}", message.getMessageId(), e);
                }
            } else {
                log.info("显式意图被忽略 intent={} allowed={} stageBlocked={} stage={}",
                        preferred, allowed, stageBlocked, user.getStage());
            }
        }
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

    /** 场景：自由对话 */
    public static final int SCENE_CHAT = 0;

    /** 场景：编程练习（学生端会话列表按此筛选，避免与普通对话混在一起） */
    public static final int SCENE_CODING = 3;

    public AgentSession createSession(TokenUserInfoDTO user) {
        return createSession(user, SCENE_CHAT, null);
    }

    /**
     * 创建会话
     *
     * @param scene 场景：0 自由对话 / 1 课程引导 / 2 路径引导 / **3 编程练习**（学生端会话列表可按场景筛选）
     * @param title 指定标题（如「编程练习 · 星星塔」）；为空时由首条用户消息截断生成
     */
    public AgentSession createSession(TokenUserInfoDTO user, Integer scene, String title) {
        AgentSession session = new AgentSession();
        session.setSessionId(generateId());
        session.setUserId(user.getUserId());
        session.setTitle(StringTools.isEmpty(title) ? "新对话" : title);
        session.setStage(user.getStage());
        session.setScene(scene == null ? SCENE_CHAT : scene);
        session.setTop(0);
        session.setMessageCount(0);
        session.setStatus(0);
        session.setCreateTime(new Date());
        session.setUpdateTime(new Date());
        agentSessionService.add(session);
        return session;
    }

    /** 学生端会话列表：置顶优先，其余按最后消息时间倒序 */
    public List<AgentSession> sessionList(TokenUserInfoDTO user, Integer scene, Integer sceneNot) {
        AgentSessionQuery query = new AgentSessionQuery();
        query.setUserId(user.getUserId());
        query.setScene(scene);
        query.setSceneNot(sceneNot);
        query.setOrderBy("a.top desc, a.last_message_time desc");
        return agentSessionService.findListByParam(query);
    }

    /** 重命名会话 */
    public void renameSession(TokenUserInfoDTO user, String sessionId, String title) {
        AgentSession session = requireOwnSession(user, sessionId);
        if (StringTools.isEmpty(title)) {
            throw new BusinessException("会话名称不能为空");
        }
        AgentSession updateBean = new AgentSession();
        updateBean.setTitle(title.length() > 50 ? title.substring(0, 50) : title);
        updateBean.setUpdateTime(new Date());
        agentSessionService.updateAgentSessionBySessionId(updateBean, session.getSessionId());
    }

    /** 置顶 / 取消置顶 */
    public void topSession(TokenUserInfoDTO user, String sessionId, Integer top) {
        AgentSession session = requireOwnSession(user, sessionId);
        AgentSession updateBean = new AgentSession();
        updateBean.setTop(top != null && top == 1 ? 1 : 0);
        updateBean.setUpdateTime(new Date());
        agentSessionService.updateAgentSessionBySessionId(updateBean, session.getSessionId());
    }

    private AgentSession requireOwnSession(TokenUserInfoDTO user, String sessionId) {
        AgentSession session = agentSessionService.getAgentSessionBySessionId(sessionId);
        if (session == null || !user.getUserId().equals(session.getUserId())) {
            throw new BusinessException("会话不存在");
        }
        return session;
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

            // 学生在界面上显式选过意图（「动画讲解」模式/动作卡片）时以它为准：显式选择不该被分类器猜错，
            // 猜错会退化成文字讲解，学生看到的是"我没有生成动画的功能"（2026-10-08 修）
            String preferredIntent = takePreferredIntent(message.getMessageId());
            IntentAnalyzerComponent.IntentResult intentResult = preferredIntent == null
                    ? intentAnalyzerComponent.analyze(message.getUserMessage(), user.getStage())
                    : new IntentAnalyzerComponent.IntentResult(preferredIntent, null, 0, 0);
            String intent = intentResult.intent();
            if (preferredIntent != null) {
                log.info("采用学生显式选择的意图 intent={} messageId={}", preferredIntent, message.getMessageId());
            }
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

            // 动画讲解：**优先提交异步任务**（与「动画讲解」页共用同一套状态机），
            // 前端按 taskId 轮询进度卡片——学生能立刻看到"收到 + 正在生成"，切页也不中断（2026-10-08 学生要求，对齐绘本体验）；
            // 任务提交失败再退回本进程内同步生成，最后才降级为文字讲解
            if ("ANIMATION".equals(intent)) {
                if (handleAnimationTaskAnswer(user, session, message, intentResult, push)) {
                    return;
                }
                if (handleAnimationAnswer(user, session, message, intentResult, push)) {
                    return;
                }
                log.warn("动画生成失败，降级为文字讲解");
                degradeToChat(message);
                intent = "CHAT";
            }

            // 对话内出题：生成选择测验卡片；生成失败降级为文字出题
            if ("QUIZ".equals(intent)) {
                if (handleQuizAnswer(user, session, message, intentResult, push)) {
                    return;
                }
                log.warn("出题生成失败，降级为文字出题");
                degradeToChat(message);
                intent = "CHAT";
            }

            // 对话内绘本：**仅小学（小低/小高）可用**（2026-10-08 补学段拦截，与动画侧对称）。
            // 非小学学段即使被分类成 PICTURE_BOOK（或前端误传 preferIntent）也不提交任务，
            // 降级为 CHAT 由模型如实说明适用范围；小学段提交失败同样降级为文字讲解
            if ("PICTURE_BOOK".equals(intent)) {
                if (isPrimaryStage(user.getStage())
                        && handlePictureBookAnswer(user, session, message, intentResult, push)) {
                    return;
                }
                if (!isPrimaryStage(user.getStage())) {
                    log.warn("非小学学段要求绘本，按不支持处理（学段={}）", user.getStage());
                } else {
                    log.warn("绘本任务提交失败，降级为文字讲解");
                }
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
            // 是否传 parallel_tool_calls 也由供应商决定（GLM 必须禁用以规避 Spring AI 流式的单工具调用限制）
            Boolean parallelToolCalls = provider.parallelToolCalls();
            if (parallelToolCalls != null) {
                optionsBuilder.parallelToolCalls(parallelToolCalls);
            }
            OpenAiChatOptions options = optionsBuilder.build();

            RagSearchComponent.RagSearchResult ragResult =
                    shouldSearch(intent) ? ragSearchComponent.buildRagResult(user.getUserId(), user.getStage(), message.getUserMessage())
                            : new RagSearchComponent.RagSearchResult("", List.of());
            List<ResourceRecommendVO> recommends = ragResult.recommendations();
            // MCP 工具：未启用时返回空数组；构建提前到 prompt 组装之前，
            // 能力说明块按真实挂载的工具动态追加（MCP 关闭时模型不会声称具备这些能力）
            ToolCallback[] knowledgeTools = knowledgeAgentToolComponent.buildCallbacks();
            String systemPrompt = resolvePromptWithRag(user, intent, ragResult.ragData(), knowledgeTools,
                    takeLearningContext(message.getMessageId()));
            sendRecommendPush(user, message, recommends);
            AtomicInteger promptTokens = new AtomicInteger(intentResult.promptTokens());
            AtomicInteger completionTokens = new AtomicInteger(intentResult.completionTokens());

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
     * 动画讲解「任务版」链路（2026-10-08）：提交异步任务 + 立刻回"收到 + 进度卡"，
     * 与绘本同一套体验（对话内可见、切页保持、完成后卡片直接变成播放器）。
     * 返回 true 表示任务已提交并推送；false 表示提交失败（调用方退回同步生成）。
     */
    private boolean handleAnimationTaskAnswer(TokenUserInfoDTO user, AgentSession session, AgentMessage message,
                                             IntentAnalyzerComponent.IntentResult intent, AgentMessagePushDTO push) {
        try {
            String topic = resolveAnimationConcept(user, session, message);
            if (StringTools.isEmpty(topic)) {
                return false;
            }
            // 主题过长时截断（任务侧与动画页同口径 50 字）
            String trimmed = topic.trim();
            if (trimmed.length() > 50) {
                trimmed = trimmed.substring(0, 50);
            }
            AnimationTaskVO task = animationTaskService.submit(user.getUserId(), user.getStage(), trimmed);

            Map<String, Object> bizData = new HashMap<>();
            bizData.put("taskId", task.getTaskId());
            bizData.put("topic", trimmed);
            bizData.put("status", task.getStatus());
            String bizJson = JSON.toJSONString(bizData);

            String text = "收到！正在为你生成动画讲解《" + trimmed + "》，AI 正在把概念拆成分步 SVG 画面"
                    + "（通常十几秒到一分钟）。进度就在下方卡片里，切到其他页面也不会中断；"
                    + "完成后卡片会直接变成动画讲解，也可以去「动画讲解」页查看～";

            Date now = new Date();
            AgentMessage update = new AgentMessage();
            update.setAssistantMessage(text);
            update.setStatus(1);
            update.setBizType("ANIMATION_TASK");
            update.setBizData(bizJson);
            update.setPromptTokens(intent.promptTokens());
            update.setCompletionTokens(intent.completionTokens());
            update.setUpdateTime(now);
            agentMessageService.updateAgentMessageByMessageId(update, message.getMessageId());

            push.setType("done");
            push.setContent(text);
            push.setBizType("ANIMATION_TASK");
            push.setBizData(bizJson);
            channelContextUtils.sendMessage(user.getUserId(), JSON.toJSONString(push));
            return true;
        } catch (Exception e) {
            log.warn("动画任务提交失败，退回同步生成 messageId={}", message.getMessageId(), e);
            return false;
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
        return buildRecentTranscript(userId, sessionId, currentMessageId, 600);
    }

    /**
     * 最近完成问答的文本稿（正序、每侧截断），供指代型产物指令（动画/出题/绘本）还原「刚才讲的内容」。
     * 取材口径与 buildHistory 一致，但排除产物型消息（出题/绘本/动画的助手侧是卡片提示短句，不是讲解，
     * 混入会让概念/主题解析跑偏）。lastAssistantCap 控制最近一条 AI 讲解的截断额度
     * （出题需覆盖完整讲解，给更大额度；概念/主题解析取大意，600 足够）。
     */
    private String buildRecentTranscript(String userId, String sessionId, String currentMessageId, int lastAssistantCap) {
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
            if (isProductStub(item)) {
                continue;
            }
            if (item.getStatus() != null && item.getStatus() == 1 && !StringTools.isEmpty(item.getAssistantMessage())) {
                doneList.add(item);
            }
        }
        if (doneList.isEmpty()) {
            return "";
        }
        StringBuilder transcript = new StringBuilder();
        int from = Math.max(0, doneList.size() - CONCEPT_HISTORY_LIMIT);
        for (int i = from; i < doneList.size(); i++) {
            AgentMessage item = doneList.get(i);
            boolean last = i == doneList.size() - 1;
            transcript.append("学生：").append(truncateForTranscript(item.getUserMessage(), 200)).append('\n');
            transcript.append("AI：").append(truncateForTranscript(item.getAssistantMessage(), last ? lastAssistantCap : 600)).append('\n');
        }
        return transcript.toString();
    }

    /**
     * 产物型消息：助手侧只有卡片提示短句（出题/绘本/动画），不是讲解内容，不进指代解析文本稿
     */
    private boolean isProductStub(AgentMessage item) {
        String bizType = item.getBizType();
        return "QUIZ".equals(bizType) || "PICTURE_BOOK".equals(bizType) || "ANIMATION".equals(bizType);
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
     * 指代型需求（"针对刚才讲解的内容出题"）结合最近对话文本稿出题，排除产物型消息污染；
     * 返回 true 表示产物已推送；false 表示生成失败（调用方降级为文字出题）
     */
    private boolean handleQuizAnswer(TokenUserInfoDTO user, AgentSession session, AgentMessage message,
                                     IntentAnalyzerComponent.IntentResult intent, AgentMessagePushDTO push) {
        try {
            String userMessage = message.getUserMessage();
            String contextDigest = null;
            if (userMessage != null && CONCEPT_REFERENCE_WORDS.stream().anyMatch(userMessage::contains)) {
                // 出题要覆盖完整讲解，最近一条 AI 消息给 3000 字符额度；无历史时摘录为空，出题组件按需求原句兜底
                contextDigest = buildRecentTranscript(user.getUserId(), session.getSessionId(), message.getMessageId(), 3000);
            }
            QuizGenerateComponent.QuizScript quiz =
                    quizGenerateComponent.generate(user.getStage(), userMessage, contextDigest);
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
    private boolean handlePictureBookAnswer(TokenUserInfoDTO user, AgentSession session, AgentMessage message,
                                            IntentAnalyzerComponent.IntentResult intent, AgentMessagePushDTO push) {
        try {
            String topic = message.getUserMessage() == null ? "" : message.getUserMessage().trim();
            // 指代型主题（"把刚才讲的内容做成绘本"）结合最近对话解析成具体主题，解析失败兜底原句；
            // 版权净化在任务消费端照常执行，不受影响
            if (!topic.isEmpty() && CONCEPT_REFERENCE_WORDS.stream().anyMatch(topic::contains)) {
                String transcript = buildRecentTranscript(user.getUserId(), session.getSessionId(), message.getMessageId());
                if (!StringTools.isEmpty(transcript)) {
                    topic = pictureBookGenerateComponent.resolveTopic(user.getStage(), message.getUserMessage(), transcript);
                }
            }
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
        // 网关类错误优先带上响应体（Cloudflare/opencode 的 4xx 会在 body 里写明原因，如 error code:1010、
        // InvalidRequestError 等；此前只记 "400 Bad Request" 拿不到依据，排障全靠猜）
        Throwable cursor = error;
        for (int guard = 0; cursor != null && guard < 10; guard++) {
            if (cursor instanceof WebClientResponseException responseException) {
                String body = responseException.getResponseBodyAsString();
                if (!StringTools.isEmpty(body)) {
                    String detail = responseException.getMessage() + " | body: " + body.trim();
                    // error_info 列宽 500，留余量截断
                    return detail.length() > 460 ? detail.substring(0, 460) : detail;
                }
                break;
            }
            cursor = cursor.getCause();
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

    private AgentSession resolveSession(TokenUserInfoDTO user, String sessionId, Integer scene, String sessionTitle) {
        if (StringTools.isEmpty(sessionId)) {
            return createSession(user, scene, sessionTitle);
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

    /**
     * 取出并清理暂存的学习上下文（读一次即删，避免长期占用；取不到返回 null）。
     */
    /** 学生显式选择的意图：取一次即删（同学习上下文口径），没有则返回 null 走意图分类 */
    private String takePreferredIntent(String messageId) {
        if (StringTools.isEmpty(messageId)) {
            return null;
        }
        try {
            String key = Constants.REDIS_KEY_AGENT_PREFER_INTENT + messageId;
            String intent = redisComponent.getString(key);
            if (!StringTools.isEmpty(intent)) {
                redisComponent.removeKey(key);
                return intent;
            }
            return null;
        } catch (Exception e) {
            log.warn("读取显式意图失败 messageId={}", messageId, e);
            return null;
        }
    }

    private String takeLearningContext(String messageId) {
        if (StringTools.isEmpty(messageId)) {
            return null;
        }
        try {
            String key = Constants.REDIS_KEY_AGENT_LEARNING_CONTEXT + messageId;
            String context = redisComponent.getString(key);
            if (!StringTools.isEmpty(context)) {
                redisComponent.removeKey(key);
            }
            return context;
        } catch (Exception e) {
            log.warn("读取学习上下文失败 messageId={}", messageId, e);
            return null;
        }
    }

    private String resolvePromptWithRag(TokenUserInfoDTO user, String intent, String ragData, ToolCallback[] tools,
                                       String learningContext) {
        String prompt = promptTemplateComponent.resolvePrompt(user.getStage(), intent);
        String promptWithRag;
        if (!shouldSearch(intent)) {
            promptWithRag = prompt;
        } else if (ragData == null || ragData.isBlank()) {
            promptWithRag = prompt + "\n\n" + RAG_ATTRIBUTION_GUARD;
        } else {
            // 模板自带 {{ragData}} 占位符时只替换数据、规则照旧追加，避免启用占位符的模板绕过来源口径约束
            promptWithRag = (prompt.contains("{{ragData}}")
                    ? prompt.replace("{{ragData}}", ragData)
                    : prompt + "\n\n## 知识库参考内容（按来源分组，引用时请如实区分）\n" + ragData)
                    + "\n\n" + RAG_CITATION_RULE;
        }
        // 平台事实约束（常驻）：课程/人数/难度等只能来自工具返回，参考资料不等于课程清单
        String withFactRule = promptWithRag + "\n\n" + PLATFORM_FACT_RULE;
        // 学生画像速览（计划 C1）：让模型不调工具也知道学生概况；取不到就不注入（降级不影响对话）
        String profileLine = studentProfileComponent.profileLine(
                user.getUserId(), user.getStage(), user.getGrade());
        String withProfile = StringTools.isEmpty(profileLine) ? withFactRule
                : withFactRule + "\n\n## 学生画像速览（可直接参考；更细的掌握度/复习时间请用工具查）\n" + profileLine;
        // 产品功能自述块：不依赖 MCP 始终注入（按学段生成，防止介绍能力时漏掉绘本/编程等内建功能）
        // 学生自定义偏好（计划 C2）：系统提示词第四层，优先级低于上面的平台安全与事实规则；
        // 总开关关闭、或学生没设规则时不注入（块内也写明"平台规则优先"，不只靠拼接顺序）
        String ruleBlock = userPromptRuleComponent.promptBlock(user.getUserId());
        String withUserRules = StringTools.isEmpty(ruleBlock) ? withProfile : withProfile + "\n\n" + ruleBlock;
        // 《我的学习偏好》自由段（计划 C3）：页面写着"AI 每次回答你时都会参考"，就必须真的注入。
        // 优先级最低（放在规则之后）；学生没写过（仍是示例）时为空、不注入
        String freePreference = preferencePageComponent.freeSectionForPrompt(user.getUserId());
        String withFreePreference = StringTools.isEmpty(freePreference) ? withUserRules
                : withUserRules + "\n\n## 学生自己写的学习偏好（自由段，优先级最低，与平台规则冲突时以平台规则为准）\n"
                        + freePreference;
        // 学生当前学习上下文（来自学习路径节点等入口）：独立段落，优先参考；取不到则跳过
        String withLearning = StringTools.isEmpty(learningContext) ? withFreePreference
                : withFreePreference + "\n\n## 学生当前学习上下文（本轮消息带入，请直接引用）\n" + learningContext;
        // 当前时间（2026-10-07 修复）：模型自身没有可靠的"今天几号"，
        // 判断复习是否到期/逾期多久必须以这里注入的时间为准，否则会把已过期说成"还没到时间"。
        String withNow = withLearning + "\n\n## 当前时间\n今天是 "
                + java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                + "（" + "周" + "一二三四五六日".charAt(java.time.LocalDate.now().getDayOfWeek().getValue() - 1)
                + "）" + java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"))
                + "。凡涉及\"是否到期 / 逾期多久 / 今天该学什么\"，一律以这个时间为准；"
                + "工具返回的时间若早于它，就是**已经逾期**，不要说\"还没到时间\"。";
        String withProduct = appendProductCapabilities(withNow, user.getStage());
        // 输出格式规范：与前端渲染层规范化双保险（模型偶发输出无空格标题/列表导致前端原样显示源码）
        String withFormat = withProduct + "\n\n" + MARKDOWN_FORMAT_RULE;
        // MCP 能力块：仅工具真实挂载时追加，MCP 关闭时模型不会声称具备这些能力
        return appendMcpCapabilities(withFormat, tools);
    }

    /**
     * 输出格式规范（2026-10-04 新增，与前端 MathMarkdown 的 normalizeMarkdown 双保险）：
     * 实测模型复述教材时用"中文习惯"输出无空格 Markdown（##一、/###1.原始社会/-基本单位），
     * CommonMark 不识别导致前端整行原样显示源码；提示词侧先约束，前端渲染层再兜底。
     */
    private static final String MARKDOWN_FORMAT_RULE = """
            ## 输出格式规范（必须遵守）
            1. Markdown 标题的 # 后必须留一个空格：写 `## 一、小节名`，不要写 `##一、小节名`；
            2. 列表项符号后必须留一个空格：写 `- 内容`，不要写 `-内容`；有序列表同理：写 `1. 内容`；
            3. 加粗、公式保持 `**加粗**`、`$公式$` 的规范写法；标题与列表独占一行，不要和其它文字挤在同一行。""";

    /** 小学低 / 高年级编码（产品功能块的学段分支） */
    private static final String STAGE_PRIMARY_LOW = "PRIMARY_LOW";
    private static final String STAGE_PRIMARY_HIGH = "PRIMARY_HIGH";

    /** 学生端可显式指定的意图白名单（其余一律走意图分类，防止前端被改造成任意意图注入） */
    private static final java.util.Set<String> PREFER_INTENT_WHITELIST =
            java.util.Set.of("ANIMATION", "QUIZ", "PICTURE_BOOK");

    /**
     * 产品功能自述块（不依赖 MCP 开关，始终注入）：
     * 按学生学段列出真实可用的产品功能（绘本生成仅小学、动画讲解/学习路径仅初高中），
     * 避免模型介绍能力时漏掉绘本生成、趣味编程等内建功能，或把页面功能说成"不是我的功能"。
     * 用户问"你能做什么"时以本节 + MCP 工具块（若挂载）为准如实回答。
     */
    private String appendProductCapabilities(String prompt, String stage) {
        String code = stage == null ? "" : stage.trim().toUpperCase();
        StringBuilder block = new StringBuilder("\n\n## 你的产品功能（学生当前学段：")
                .append(stageDescOf(stage)).append("；介绍能力时以本节为准，不遗漏、不夸大）\n")
                .append("- AI 讲课答疑与解题：各科知识讲解、公式推导、作文指导等，随时提问；\n")
                .append("- 出题练习：在对话里直接出选择题，即时判分并逐题解析；\n")
                .append("- 课程教材：按学段浏览课程与课时、观看教学视频；\n")
                .append("- 资源中心：动画、绘本、编程作品、知识页等学习产物都汇总在这里；\n")
                .append("- 个人知识页：AI 讲解可一键同步为知识页，在「知识页」里查看、编辑、确认入库；\n");
        if (STAGE_PRIMARY_LOW.equals(code)) {
            block.append("- 绘本生成：在「绘本生成」页输入主题，AI 编故事、配图并朗读（小学专属）；\n");
        } else if (STAGE_PRIMARY_HIGH.equals(code)) {
            block.append("- 绘本生成：在「绘本生成」页输入主题，AI 编故事、配图并朗读（小学专属）；\n")
                    .append("- 趣味编程：在「趣味编程」页写 Python 并直接运行，有分学段的题库与编程比赛；\n");
        } else {
            block.append("- 动画讲解：把抽象概念做成 SVG 分步动画（初高中专属）。**学生说「生成/做个X的动画讲解、动画演示」时，"
                    + "你就在这个对话里直接生成，不要说「我没有这个功能」，也不要只让他自己去页面找**（对话里输「生成X的动画讲解」即可，"
                    + "「动画讲解」页是另一个入口）；\n")
                    .append("- 学习路径：按你的学习档案生成个性化学习路线，掌握度驱动解锁；\n")
                    .append("- 绘本生成：**面向小学（小低/小高），本学段不可用**；学生要求绘本时如实说明适用范围，"
                            + "并建议用动画讲解或知识页代替，不要假装能生成、也不要说成系统坏了；\n")
                    .append("- 趣味编程：在「趣味编程」页写 Python 并直接运行，有分学段的题库与编程比赛；\n");
        }
        block.append("介绍要求：\n")
                .append("1. 用户问「你能做什么/有哪些功能」时，按本节如实介绍，不遗漏、不夸大；\n")
                .append("2. 本节未列出的能力不要声称具备；涉及页面操作的功能要说明入口页面名称；\n")
                .append("3. 与当前学段不匹配的能力如实说明适用范围（动画讲解面向初高中、绘本面向小学等）；\n")
                .append("4. 本节列出的能力要正面回答「可以」：学生要动画讲解就在对话里直接生成（不要否认、不要只让他去页面找）；\n");
        return prompt + block;
    }

    /** 是否小学段（小低/小高）：绘本生成仅小学可用，动画讲解仅初高中可用 */
    private boolean isPrimaryStage(String stage) {
        return STAGE_PRIMARY_LOW.equalsIgnoreCase(stage == null ? "" : stage.trim())
                || STAGE_PRIMARY_HIGH.equalsIgnoreCase(stage == null ? "" : stage.trim());
    }

    /** 学段编码 → 中文描述（产品功能块用） */    private String stageDescOf(String stage) {
        if (stage == null) {
            return "未知学段";
        }
        for (StageEnum item : StageEnum.values()) {
            if (item.getCode().equalsIgnoreCase(stage.trim())) {
                return item.getDesc();
            }
        }
        return "未知学段";
    }

    /** MCP 工具名 → 能力说明（能力块按真实挂载的工具名取交集生成，杜绝"说了没有"） */
    private static final Map<String, String> MCP_TOOL_CAPABILITIES = Map.ofEntries(
            Map.entry("listKnowledgePages", "查个人知识页清单"),
            Map.entry("readKnowledgePage", "读某页知识页全文"),
            Map.entry("createKnowledgePage", "把整理内容新建为知识页草稿"),
            Map.entry("updateKnowledgePage", "覆盖修改知识页草稿"),
            Map.entry("ingestKnowledgePage", "把知识页入库向量化"),
            Map.entry("aiSummarizeKnowledgePage", "AI 总结生成摘要页"),
            Map.entry("aiRewriteKnowledgePage", "AI 按要求改写知识页"),
            Map.entry("aiOrganizeKnowledgePages", "AI 归档整合多篇知识页"),
            Map.entry("createWikiFolder", "新建知识页子文件夹"),
            Map.entry("moveKnowledgePage", "移动知识页到子文件夹"),
            Map.entry("searchTextbooks", "查官方教材书目"),
            Map.entry("getTextbookToc", "读教材章节目录"),
            Map.entry("readTextbookSection", "读教材指定章节正文"),
            Map.entry("queryCourse", "查课程清单（传 mine=true 查「我加入的课程」，跨学段）"),
            Map.entry("queryLesson", "查课程课时列表与详情"),
            Map.entry("recommendResource", "推荐官方学习资源"),
            Map.entry("queryMastery", "查知识点掌握度概览"),
            Map.entry("saveLearningRecord", "记录学生学习行为（仅用户明确要求时调用）"));

    /** 能力块分组展示顺序：组名 → 该组工具名 */
    private static final Map<String, List<String>> MCP_TOOL_GROUPS = new LinkedHashMap<>();

    static {
        MCP_TOOL_GROUPS.put("知识页工具", List.of(
                "listKnowledgePages", "readKnowledgePage", "createKnowledgePage",
                "updateKnowledgePage", "ingestKnowledgePage",
                "aiSummarizeKnowledgePage", "aiRewriteKnowledgePage", "aiOrganizeKnowledgePages",
                "createWikiFolder", "moveKnowledgePage"));
        MCP_TOOL_GROUPS.put("教材检索工具（按学生学段自动过滤）", List.of(
                "searchTextbooks", "getTextbookToc", "readTextbookSection"));
        MCP_TOOL_GROUPS.put("教学查询工具", List.of(
                "queryCourse", "queryLesson", "recommendResource", "queryMastery", "saveLearningRecord"));
    }

    /**
     * MCP 能力块：仅在工具真实挂载时追加到 system prompt，内容按挂载工具名与说明映射取交集生成；
     * 用户问"你能做什么"时模型依据本节如实回答，未挂载的能力不会被提及
     */
    private String appendMcpCapabilities(String prompt, ToolCallback[] tools) {
        if (tools == null || tools.length == 0) {
            return prompt;
        }
        Set<String> attached = new HashSet<>();
        for (ToolCallback tool : tools) {
            attached.add(tool.getToolDefinition().name());
        }
        StringBuilder block = new StringBuilder("\n\n## 当前可用的 MCP 工具（本轮对话已真实挂载）\n");
        for (Map.Entry<String, List<String>> group : MCP_TOOL_GROUPS.entrySet()) {
            List<String> lines = new ArrayList<>();
            for (String name : group.getValue()) {
                if (attached.contains(name) && MCP_TOOL_CAPABILITIES.containsKey(name)) {
                    lines.add(name + " " + MCP_TOOL_CAPABILITIES.get(name));
                }
            }
            if (!lines.isEmpty()) {
                block.append("- ").append(group.getKey()).append("：").append(String.join(" / ", lines)).append("\n");
            }
        }
        if (block.indexOf("- ") < 0) {
            // 挂载的工具均不在说明映射内（异常情况），不追加空块
            return prompt;
        }
        block.append("使用要求：\n")
                .append("1. 用户问「你能做什么」时，MCP 工具能力部分只依据本节如实回答，不得声称本节未列出的工具能力；\n")
                .append("2. 用户指名教材/章节而上方参考内容未覆盖时，先 searchTextbooks 找书、getTextbookToc 看目录、")
                .append("readTextbookSection 读该节，引用时注明《书名》章节；\n")
                .append("3. 涉及学生个人知识页的改动一律先落草稿，用户明确要求才入库（ingestKnowledgePage）。");
        return prompt + block;
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
