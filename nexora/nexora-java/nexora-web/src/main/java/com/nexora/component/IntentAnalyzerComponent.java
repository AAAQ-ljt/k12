package com.nexora.component;

import com.alibaba.fastjson2.JSON;
import com.nexora.dto.UserIntentDTO;
import com.nexora.entity.enums.UserIntentEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 意图分析：理科求解类先走关键词规则（省一次模型调用），其余走 LLM 结构化分类，失败兜底 CHAT
 */
@Component
@Slf4j
public class IntentAnalyzerComponent {

    private static final String INTENT_SYSTEM_PROMPT = """
            你是意图分类器。只输出 JSON，不要输出任何解释。
            从以下意图中选择一个：
            - EXPLAIN：讲解知识点或概念（"什么是X"、"讲讲X"）
            - RECOMMEND：推荐学习资料或课程
            - QUIZ：要求出题、练习、测验
            - PICTURE_BOOK：明确要求"创作/生成一本绘本或故事书"（产出物是一本新绘本）
            - DRAW：明确要求"画一幅画/生成一张图片"（产出物是一张新图片）
            - ANIMATION：要求学生把某个概念/过程**做成动画来讲解**（产出物是一段动画）：例如
              "生成细胞分裂的动画讲解"、"做个冒泡排序的动画演示"、"把光合作用做成动画"、"用动画讲讲X"、
              "X 的分步动画/动画讲解/动画片"。判定看产出物是不是"一段新做的动画"，**不要求出现"分步"二字**；
              如果只是"找/有没有现成的动画资源或视频"，选 RECOMMEND，不是本意图。
            - CODING：编程、代码、写程序相关
            - SCIENCE_SOLVE：数理化生题目求解
            - PLAN：制定学习计划
            - PROGRESS：查询学习进度
            - CHAT：普通聊天与提问。
            判定要点：用户提到"图片/这张图/图中"并询问其内容（是什么/讲了什么），这是看图提问，图片由视觉模型处理，选 CHAT；
            只有明确要求"创作绘本"才选 PICTURE_BOOK，只有明确要求"画一张新图"才选 DRAW；
            只有明确要求"把概念做成动画"才选 ANIMATION（普通"讲讲/解释"仍选 EXPLAIN）。
            无法确定用户意图时使用 CHAT。
            返回格式：{"intent":"EXPLAIN","data":{"knowledgePoint":"冒泡排序"}}。""";

    /** 概念类问句前缀：命中则不以关键词规则判为理科求解，交给 LLM/RAG 链路 */
    private static final List<String> CONCEPT_PREFIXES = List.of(
            "什么是", "啥是", "什么叫", "这是什么", "介绍一下", "介绍", "解释", "讲解",
            "概念", "原理是", "意思是", "举例", "举个例子", "举例子", "为什么");

    /** 理科求解关键词（数学/物理/化学/生物），含解题句式触发词 */
    private static final List<String> SCIENCE_KEYWORDS = List.of(
            // 数学
            "解方程", "方程", "求导", "导数", "微分", "积分", "求极限", "极限", "不等式",
            "证明", "求解", "三角函数", "对数", "指数", "概率", "排列", "组合数", "几何",
            "勾股", "配方", "函数值", "计算",
            // 物理
            "并联", "串联", "电阻", "电流", "电压", "电功率", "功率", "加速度", "牛顿",
            "浮力", "压强", "做功", "动能", "势能", "机械能", "折射", "反射", "透镜",
            "欧姆", "自由落体", "抛体", "电路",
            // 化学
            "配平", "化学方程式", "化学反应", "摩尔", "浓度", "化合价", "离子", "电解",
            "氧化还原", "中和反应", "沉淀", "溶解度", "化学式",
            // 生物
            "遗传", "基因", "染色体", "显性", "隐性", "杂交", "光合作用", "呼吸作用",
            "细胞分裂", "减数分裂",
            // 解题句式
            "帮我算", "帮我解", "怎么算", "怎么求", "怎么解", "计算一下", "算一下",
            "推导", "这道题", "解题", "做一下这道");

    private final ChatClient chatClient;

    @Value("${spring.ai.openai.chat.options.model:deepseek-v4-flash}")
    private String chatModel;

    public IntentAnalyzerComponent(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    public IntentResult analyze(String userMessage, String stage) {
        boolean primaryStage = "PRIMARY_LOW".equalsIgnoreCase(stage) || "PRIMARY_HIGH".equalsIgnoreCase(stage);
        if (matchScienceKeyword(userMessage)) {
            return new IntentResult("SCIENCE_SOLVE", null, 0, 0);
        }
        try {
            String systemPrompt = primaryStage
                    ? INTENT_SYSTEM_PROMPT
                            + " 注意：用户为小学阶段（小低/小高），画面型动画能力不可用：禁止选择 ANIMATION，动画类请求返回 CHAT。"
                    : INTENT_SYSTEM_PROMPT;
            ChatResponse response = chatClient.prompt()
                    .system(systemPrompt)
                    .user(userMessage)
                    .options(OpenAiChatOptions.builder().model(chatModel).build())
                    .call()
                    .chatResponse();
            int promptTokens = 0;
            int completionTokens = 0;
            if (response.getMetadata() != null && response.getMetadata().getUsage() != null) {
                Usage usage = response.getMetadata().getUsage();
                promptTokens = usage.getPromptTokens() == null ? 0 : usage.getPromptTokens();
                completionTokens = usage.getCompletionTokens() == null ? 0 : usage.getCompletionTokens();
            }
            String content = "";
            if (response.getResults() != null && !response.getResults().isEmpty()
                    && response.getResults().get(0).getOutput() != null) {
                content = response.getResults().get(0).getOutput().getText();
            }
            UserIntentDTO dto = parseJson(content);
            String intent = dto == null || dto.getIntent() == null ? "CHAT" : dto.getIntent().trim().toUpperCase();
            if (!UserIntentEnum.isValid(intent)) {
                intent = "CHAT";
            }
            // 硬拦截：小学阶段（小低/小高）不允许动画生成（无动画页面，防止误用绘本提示词）
            if (primaryStage && "ANIMATION".equals(intent)) {
                log.info("小学阶段动画意图降级为 CHAT: {}", userMessage);
                intent = "CHAT";
            }
            // 硬拦截（2026-10-08 补）：绘本生成面向小学（小低/小高），初高中学生要求绘本时不给生成任务，
            // 降级为 CHAT 由模型如实说明适用范围（此前只有动画侧有学段拦截，绘本侧漏了）
            if (!primaryStage && "PICTURE_BOOK".equals(intent)) {
                log.info("非小学阶段绘本意图降级为 CHAT: {}", userMessage);
                intent = "CHAT";
            }
            return new IntentResult(intent, dto == null ? null : dto.getData(), promptTokens, completionTokens);
        } catch (Exception e) {
            log.warn("意图分析失败，兜底 CHAT", e);
            return new IntentResult("CHAT", null, 0, 0);
        }
    }

    /**
     * 理科求解关键词规则：概念类问句（什么是/解释/为什么等开头）不进入该规则，避免误伤概念查询
     */
    private boolean matchScienceKeyword(String userMessage) {
        if (userMessage == null || userMessage.isBlank()) {
            return false;
        }
        String text = userMessage.trim();
        String lower = text.toLowerCase();
        for (String prefix : CONCEPT_PREFIXES) {
            if (text.startsWith(prefix)) {
                return false;
            }
        }
        return SCIENCE_KEYWORDS.stream().anyMatch(lower::contains);
    }

    private UserIntentDTO parseJson(String content) {
        if (content == null || content.isBlank()) {
            return null;
        }
        String text = content.trim();
        if (text.startsWith("```")) {
            text = text.replaceFirst("^```(?:json)?\\s*", "").replaceFirst("\\s*```$", "");
        }
        try {
            return JSON.parseObject(text, UserIntentDTO.class);
        } catch (Exception e) {
            log.warn("意图 JSON 解析失败，兜底 CHAT: {}", text);
            return null;
        }
    }

    public record IntentResult(String intent, Map<String, Object> data, int promptTokens, int completionTokens) {
    }
}