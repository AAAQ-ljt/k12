package com.nexora.component;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.nexora.entity.po.KnowledgeDoc;
import com.nexora.utils.StringTools;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.definition.DefaultToolDefinition;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * AI 助教「个人知识页」工具集（对话侧）：
 *
 * 1）数据工具：包装 nexora-mcp 的知识页 MCP 工具（列表/读取/新建/覆盖/入库），
 *    模型看到的入参不含 userId——真实 userId 与学段由 web 端从登录上下文经 ToolContext 注入，
 *    模型既看不到也无法伪造，杜绝越权访问他人知识页；
 * 2）AI 处理工具：摘要 / 改写 / 归档整合，由服务端提示词体系（PromptTemplateComponent，Redis → 表 → 枚举默认值）
 *    驱动大模型完成文本处理，结果再经 MCP 工具落库为草稿。
 *
 * 统一口径：AI 改动一律落草稿（vectorStatus=0，旧向量清理），只有用户明确要求入库时才调用 ingest 工具。
 * MCP 未启用 / 未启动时 {@link #buildCallbacks()} 返回空数组，对话主链路不受影响。
 */
@Slf4j
@Component
public class KnowledgeAgentToolComponent {

    /** ToolContext key：登录用户 ID（web 端注入） */
    public static final String CTX_USER_ID = "userId";

    /** ToolContext key：学生学段（web 端注入） */
    public static final String CTX_STAGE = "stage";

    /** ToolContext key：知识页操作计数器（任一知识页工具被调用 +1，用于给消息打 WIKI 标记） */
    public static final String CTX_WIKI_OPS = "wikiOps";

    /** AI 处理单次输入上限，超出则分段处理后拼接 */
    private static final int MAX_SEGMENT_CHARS = 6000;

    /** 归档整合单次最多合并的知识页数 */
    private static final int MAX_ORGANIZE_DOCS = 8;

    /** AI 摘要页的来源标识前缀（同源重复总结覆盖同一页） */
    private static final String SOURCE_PREFIX_SUMMARY = "ai-summary:";

    /** AI 归档整合页的来源标识前缀 */
    private static final String SOURCE_PREFIX_ORGANIZE = "ai-organize:";

    /** 知识页工具上下文兜底（ToolContext 未透传时由 AgentChatComponent 设置） */
    private static final ThreadLocal<Map<String, Object>> FALLBACK_CONTEXT = new ThreadLocal<>();

    private record McpToolSpec(String name, String description, String inputSchema, boolean injectStage) {
    }

    /** 包装的 MCP 数据工具（入参 schema 全部去掉 userId，需要时由 web 端注入 userId / 学段） */
    private static final List<McpToolSpec> MCP_TOOL_SPECS = List.of(
            new McpToolSpec("listKnowledgePages",
                    "查询学生个人知识库里的知识页清单（标题、状态、来源、更新时间）。"
                            + "当用户问「我有哪些知识页 / 我整理过哪些资料 / 上次那篇在哪」时先调用本工具。",
                    """
                            {"type":"object","properties":{
                              "keyword":{"type":"string","description":"标题关键词，可空"},
                              "vectorStatus":{"type":"integer","description":"状态过滤：0草稿 1向量化中 2已入库 3失败，可空表示不限"}
                            }}""",
                    false),
            new McpToolSpec("readKnowledgePage",
                    "读取指定知识页的完整正文（Markdown）。修改、总结、归档整合之前先用本工具读取原文。",
                    """
                            {"type":"object","properties":{
                              "docId":{"type":"string","description":"知识页ID"}
                            },"required":["docId"]}""",
                    false),
            new McpToolSpec("createKnowledgePage",
                    "新建一篇知识页草稿（不会自动入库，用户确认或明确要求后才入库）。"
                            + "适合把整理好的内容写成新的知识页；传入 sourceUrl 时同来源会覆盖已有页。",
                    """
                            {"type":"object","properties":{
                              "title":{"type":"string","description":"知识页标题"},
                              "content":{"type":"string","description":"Markdown 正文"},
                              "sourceUrl":{"type":"string","description":"来源标识，可空；同来源重复写入会覆盖已有页"}
                            },"required":["title","content"]}""",
                    true),
            new McpToolSpec("updateKnowledgePage",
                    "覆盖指定知识页的标题与正文，保存后为草稿状态（已入库的页会清除旧向量，需重新入库才可检索）。",
                    """
                            {"type":"object","properties":{
                              "docId":{"type":"string","description":"知识页ID"},
                              "title":{"type":"string","description":"新标题，可空表示不改标题"},
                              "content":{"type":"string","description":"新的 Markdown 正文"}
                            },"required":["docId","content"]}""",
                    false),
            new McpToolSpec("ingestKnowledgePage",
                    "把知识页入库（向量化），入库后 AI 助教才能检索到该内容。"
                            + "只在用户明确要求「入库 / 上架 / 让 AI 能搜到」时调用，不要擅自入库。",
                    """
                            {"type":"object","properties":{
                              "docId":{"type":"string","description":"知识页ID"}
                            },"required":["docId"]}""",
                    false));

    @Resource
    private ObjectProvider<SyncMcpToolCallbackProvider> mcpToolCallbackProvider;

    @Resource
    private WikiKnowledgeComponent wikiKnowledgeComponent;

    @Resource
    private PromptTemplateComponent promptTemplateComponent;

    @Resource
    private ChatClient chatClient;

    /**
     * 构建本轮对话可用的知识页工具；MCP 未启用 / 未就绪时返回空数组（对话不挂工具，正常降级）
     */
    public ToolCallback[] buildCallbacks() {
        SyncMcpToolCallbackProvider provider = mcpToolCallbackProvider.getIfAvailable();
        if (provider == null) {
            return new ToolCallback[0];
        }
        ToolCallback[] mcpCallbacks;
        try {
            mcpCallbacks = provider.getToolCallbacks();
        } catch (Exception e) {
            log.warn("MCP 工具列表获取失败，本轮对话不启用知识页工具：{}", e.getMessage());
            return new ToolCallback[0];
        }
        Map<String, ToolCallback> byName = new LinkedHashMap<>();
        for (ToolCallback callback : mcpCallbacks) {
            byName.put(callback.getToolDefinition().name(), callback);
        }
        List<ToolCallback> callbacks = new ArrayList<>();
        for (McpToolSpec spec : MCP_TOOL_SPECS) {
            ToolCallback delegate = byName.get(spec.name());
            if (delegate == null) {
                log.warn("MCP 未注册知识页工具 {}，本轮跳过", spec.name());
                continue;
            }
            callbacks.add(new McpDataToolCallback(spec, delegate));
        }
        callbacks.add(new AiProcessToolCallback(AiOperation.SUMMARY, byName));
        callbacks.add(new AiProcessToolCallback(AiOperation.REWRITE, byName));
        callbacks.add(new AiProcessToolCallback(AiOperation.ORGANIZE, byName));
        return callbacks.toArray(new ToolCallback[0]);
    }

    /** ToolContext 未透传时的兜底上下文（由 AgentChatComponent 在流式调用期间设置） */
    public static void setFallbackContext(Map<String, Object> context) {
        FALLBACK_CONTEXT.set(context);
    }

    public static void clearFallbackContext() {
        FALLBACK_CONTEXT.remove();
    }

    private enum AiOperation {
        SUMMARY("aiSummarizeKnowledgePage",
                "为指定知识页生成结构化摘要，并新建一篇「XX（摘要）」草稿页（原文不改动）。"
                        + "用户要求「总结 / 摘要 / 提炼要点」时使用。",
                """
                        {"type":"object","properties":{
                          "docId":{"type":"string","description":"要总结的知识页ID"}
                        },"required":["docId"]}""", 1),
        REWRITE("aiRewriteKnowledgePage",
                "按用户要求改写指定知识页内容（润色、调整难度、扩写、精简），结果覆盖为该页草稿。"
                        + "用户要求「改写 / 换个说法 / 面向初中改写 / 更简洁」时使用。",
                """
                        {"type":"object","properties":{
                          "docId":{"type":"string","description":"要改写的知识页ID"},
                          "instruction":{"type":"string","description":"改写要求，可空"}
                        },"required":["docId"]}""", 1),
        ORGANIZE("aiOrganizeKnowledgePages",
                "把多篇知识页归档整合成一篇新的结构化知识页草稿（原文都不改动）。"
                        + "用户要求「归档 / 整理到一起 / 合并成一页 / 汇总」时使用。",
                """
                        {"type":"object","properties":{
                          "docIds":{"type":"string","description":"知识页ID列表，用英文逗号分隔"},
                          "title":{"type":"string","description":"整合后新知识页的标题"},
                          "instruction":{"type":"string","description":"整合要求，可空"}
                        },"required":["docIds","title"]}""", 2);

        private final String name;
        private final String description;
        private final String inputSchema;
        private final int minArgs;

        AiOperation(String name, String description, String inputSchema, int minArgs) {
            this.name = name;
            this.description = description;
            this.inputSchema = inputSchema;
            this.minArgs = minArgs;
        }
    }

    /**
     * MCP 数据工具包装：入参补齐 userId（与学段）后转发给 MCP 工具，异常兜底为友好文案
     */
    private class McpDataToolCallback implements ToolCallback {

        private final McpToolSpec spec;
        private final ToolCallback delegate;
        private final ToolDefinition definition;

        private McpDataToolCallback(McpToolSpec spec, ToolCallback delegate) {
            this.spec = spec;
            this.delegate = delegate;
            this.definition = DefaultToolDefinition.builder()
                    .name(spec.name())
                    .description(spec.description())
                    .inputSchema(spec.inputSchema())
                    .build();
        }

        @Override
        public ToolDefinition getToolDefinition() {
            return definition;
        }

        @Override
        public String call(String toolInput) {
            return call(toolInput, null);
        }

        @Override
        public String call(String toolInput, ToolContext toolContext) {
            String userId = contextUserId(toolContext);
            if (StringTools.isEmpty(userId)) {
                return "知识页工具暂不可用：未获取到登录用户身份";
            }
            JSONObject args = parseArgs(toolInput);
            args.put("userId", userId);
            if (spec.injectStage() && StringTools.isEmpty(args.getString("stage"))) {
                String stage = contextStage(toolContext);
                if (!StringTools.isEmpty(stage)) {
                    args.put("stage", stage);
                }
            }
            markWikiOp(toolContext);
            try {
                return delegate.call(args.toJSONString());
            } catch (Exception e) {
                log.warn("知识页 MCP 工具调用失败 tool={}", spec.name(), e);
                return "知识页服务暂不可用（MCP 服务 " + spec.name() + " 调用失败）："
                        + e.getMessage() + "。请稍后重试，或告知用户稍后再试。";
            }
        }
    }

    /**
     * AI 处理工具：读原页（in-process）→ 服务端提示词驱动大模型 → 经 MCP 工具落草稿
     */
    private class AiProcessToolCallback implements ToolCallback {

        private final AiOperation operation;
        private final Map<String, ToolCallback> mcpCallbacks;
        private final ToolDefinition definition;

        private AiProcessToolCallback(AiOperation operation, Map<String, ToolCallback> mcpCallbacks) {
            this.operation = operation;
            this.mcpCallbacks = mcpCallbacks;
            this.definition = DefaultToolDefinition.builder()
                    .name(operation.name)
                    .description(operation.description)
                    .inputSchema(operation.inputSchema)
                    .build();
        }

        @Override
        public ToolDefinition getToolDefinition() {
            return definition;
        }

        @Override
        public String call(String toolInput) {
            return call(toolInput, null);
        }

        @Override
        public String call(String toolInput, ToolContext toolContext) {
            String userId = contextUserId(toolContext);
            if (StringTools.isEmpty(userId)) {
                return "知识页工具暂不可用：未获取到登录用户身份";
            }
            String stage = contextStage(toolContext);
            JSONObject args = parseArgs(toolInput);
            markWikiOp(toolContext);
            try {
                return switch (operation) {
                    case SUMMARY -> summarize(userId, stage, args);
                    case REWRITE -> rewrite(userId, stage, args);
                    case ORGANIZE -> organize(userId, stage, args);
                };
            } catch (Exception e) {
                log.warn("AI 知识页处理失败 op={}", operation.name, e);
                return "AI 知识页处理失败：" + e.getMessage();
            }
        }

        /** 生成摘要并落为「XX（摘要）」草稿页（原文不改动） */
        private String summarize(String userId, String stage, JSONObject args) {
            String docId = args.getString("docId");
            if (StringTools.isEmpty(docId)) {
                return "参数错误：缺少知识页ID";
            }
            KnowledgeDoc source = wikiKnowledgeComponent.requireOwnedDoc(userId, docId.trim());
            String content = source.getContent() == null ? "" : source.getContent();
            if (StringTools.isEmpty(content)) {
                return "知识页《" + source.getTitle() + "》内容为空，无法总结";
            }
            String summary = processByModel("WIKI_SUMMARY", stage, source.getTitle(), content, null);
            if (StringTools.isEmpty(summary)) {
                return "AI 摘要生成失败，请稍后重试";
            }
            JSONObject createArgs = new JSONObject();
            createArgs.put("userId", userId);
            createArgs.put("stage", stage);
            createArgs.put("title", source.getTitle() + "（摘要）");
            createArgs.put("content", summary);
            createArgs.put("sourceUrl", SOURCE_PREFIX_SUMMARY + source.getDocId());
            return "摘要已生成（原文未改动）。" + callMcp("createKnowledgePage", createArgs);
        }

        /** 按用户要求改写并覆盖为该页草稿 */
        private String rewrite(String userId, String stage, JSONObject args) {
            String docId = args.getString("docId");
            if (StringTools.isEmpty(docId)) {
                return "参数错误：缺少知识页ID";
            }
            String instruction = args.getString("instruction");
            KnowledgeDoc source = wikiKnowledgeComponent.requireOwnedDoc(userId, docId.trim());
            String content = source.getContent() == null ? "" : source.getContent();
            if (StringTools.isEmpty(content)) {
                return "知识页《" + source.getTitle() + "》内容为空，无法改写";
            }
            String rewritten = processByModel("WIKI_REWRITE", stage, source.getTitle(), content, instruction);
            if (StringTools.isEmpty(rewritten)) {
                return "AI 改写失败，请稍后重试";
            }
            JSONObject updateArgs = new JSONObject();
            updateArgs.put("userId", userId);
            updateArgs.put("docId", source.getDocId());
            updateArgs.put("content", rewritten);
            return "改写完成。" + callMcp("updateKnowledgePage", updateArgs);
        }

        /** 多篇归档整合为一篇新草稿页 */
        private String organize(String userId, String stage, JSONObject args) {
            String docIds = args.getString("docIds");
            String title = args.getString("title");
            if (StringTools.isEmpty(docIds) || StringTools.isEmpty(title)) {
                return "参数错误：缺少待整合的知识页ID或新页标题";
            }
            String instruction = args.getString("instruction");
            List<String> ids = new ArrayList<>();
            StringBuilder material = new StringBuilder();
            int index = 1;
            for (String raw : docIds.split(",")) {
                if (StringTools.isEmpty(raw)) {
                    continue;
                }
                if (ids.size() >= MAX_ORGANIZE_DOCS) {
                    break;
                }
                KnowledgeDoc doc = wikiKnowledgeComponent.requireOwnedDoc(userId, raw.trim());
                if (StringTools.isEmpty(doc.getContent())) {
                    continue;
                }
                ids.add(doc.getDocId());
                material.append("## 资料 ").append(index++).append("：").append(doc.getTitle()).append("\n\n")
                        .append(doc.getContent()).append("\n\n");
            }
            if (ids.isEmpty()) {
                return "没有可用于整合的知识页内容（请先用 listKnowledgePages 确认知识页ID）";
            }
            String merged = processByModel("WIKI_ORGANIZE", stage, title, material.toString(), instruction);
            if (StringTools.isEmpty(merged)) {
                return "AI 归档整合失败，请稍后重试";
            }
            JSONObject createArgs = new JSONObject();
            createArgs.put("userId", userId);
            createArgs.put("stage", stage);
            createArgs.put("title", title.trim());
            createArgs.put("content", merged);
            createArgs.put("sourceUrl", SOURCE_PREFIX_ORGANIZE + String.join(",", ids));
            return "归档整合完成，已合并 " + ids.size() + " 篇（原文未改动）。"
                    + callMcp("createKnowledgePage", createArgs);
        }

        private String callMcp(String toolName, JSONObject args) {
            ToolCallback callback = mcpCallbacks.get(toolName);
            if (callback == null) {
                throw new IllegalStateException("MCP 工具未注册：" + toolName);
            }
            return callback.call(args.toJSONString());
        }
    }

    /**
     * 服务端模型处理：按场景取提示词（Redis → 表 → 枚举默认值），长文本分段处理后拼接
     */
    private String processByModel(String scene, String stage, String title, String content, String instruction) {
        String systemPrompt = promptTemplateComponent.resolvePrompt(stage, scene);
        List<String> segments = splitSegments(content);
        List<String> results = new ArrayList<>();
        for (int i = 0; i < segments.size(); i++) {
            StringBuilder userPrompt = new StringBuilder();
            userPrompt.append("知识页标题：").append(title == null ? "" : title).append("\n");
            if (!StringTools.isEmpty(instruction)) {
                userPrompt.append("用户要求：").append(instruction.trim()).append("\n");
            }
            if (segments.size() > 1) {
                userPrompt.append("以下内容较长，这是第 ").append(i + 1).append("/").append(segments.size())
                        .append(" 部分，请只处理这一部分：\n\n");
            } else {
                userPrompt.append("\n以下是需要处理的正文：\n\n");
            }
            userPrompt.append(segments.get(i));
            String result = chatClient.prompt()
                    .system(systemPrompt)
                    .user(userPrompt.toString())
                    .call()
                    .content();
            if (!StringTools.isEmpty(result)) {
                results.add(result.trim());
            }
        }
        return String.join("\n\n---\n\n", results).trim();
    }

    private List<String> splitSegments(String text) {
        List<String> segments = new ArrayList<>();
        if (StringTools.isEmpty(text)) {
            return segments;
        }
        if (text.length() <= MAX_SEGMENT_CHARS) {
            segments.add(text);
            return segments;
        }
        int start = 0;
        while (start < text.length()) {
            int end = Math.min(start + MAX_SEGMENT_CHARS, text.length());
            if (end < text.length()) {
                int newline = text.lastIndexOf('\n', end);
                if (newline > start + MAX_SEGMENT_CHARS / 2) {
                    end = newline;
                }
            }
            segments.add(text.substring(start, end));
            start = end;
        }
        return segments;
    }

    private JSONObject parseArgs(String toolInput) {
        if (StringTools.isEmpty(toolInput)) {
            return new JSONObject();
        }
        try {
            JSONObject parsed = JSON.parseObject(toolInput);
            return parsed == null ? new JSONObject() : parsed;
        } catch (Exception e) {
            log.warn("知识页工具入参解析失败，按空参数处理：{}", toolInput);
            return new JSONObject();
        }
    }

    private String contextUserId(ToolContext toolContext) {
        Object value = contextValue(toolContext, CTX_USER_ID);
        return value == null ? null : value.toString();
    }

    private String contextStage(ToolContext toolContext) {
        Object value = contextValue(toolContext, CTX_STAGE);
        return value == null ? null : value.toString();
    }

    private void markWikiOp(ToolContext toolContext) {
        Object value = contextValue(toolContext, CTX_WIKI_OPS);
        if (value instanceof AtomicInteger counter) {
            counter.incrementAndGet();
        }
    }

    private Object contextValue(ToolContext toolContext, String key) {
        if (toolContext != null && toolContext.getContext() != null) {
            Object value = toolContext.getContext().get(key);
            if (value != null) {
                return value;
            }
        }
        Map<String, Object> fallback = FALLBACK_CONTEXT.get();
        return fallback == null ? null : fallback.get(key);
    }
}
