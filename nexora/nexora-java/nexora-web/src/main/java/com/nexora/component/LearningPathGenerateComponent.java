package com.nexora.component;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.nexora.entity.enums.StageEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 个性化学习路径生成组件：结合学习档案（目标/兴趣/关键问题）与已学知识，LLM 生成分阶段学习计划 JSON。
 * 产物结构：{ "title": "...", "steps": [ { "title": "...", "desc": "...", "kind": "learn|practice|review" } ] }
 */
@Slf4j
@Component
public class LearningPathGenerateComponent {

    private static final int MAX_STEPS = 8;

    private static final String SYSTEM_PROMPT = """
            你是 K12 人工智能通识课的「个性化学习路径规划师」。学生学段：%s。
            根据学生的学习目标、兴趣与已学知识，规划一份可执行的学习路径，只输出一个 JSON 对象，不要输出任何解释或 Markdown 标记。
            JSON 结构：
            {
              "title": "路径标题（简短，如：Python 入门三步走）",
              "steps": [
                { "title": "阶段标题", "desc": "这个阶段学什么、怎么学、用什么方式（讲解/动画/编程/练习）", "kind": "learn" }
              ]
            }
            要求：
            1. steps 为 4-8 步，循序渐进，覆盖「学 → 练 → 复习」节奏（kind 取 learn 学习 / practice 练习 / review 复习）；
            2. 结合学生提供的学习目标、兴趣学科与关键问题，优先回应其兴趣；
            3. 考虑学生学段难度，语言具体可执行；
            4. title 10 字以内。""";

    private final ChatClient chatClient;

    public LearningPathGenerateComponent(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    /**
     * 生成学习路径计划；LLM 输出不合法时抛出异常（调用方提示重试）
     */
    public LearningPathPlan generate(String stage, String profileText, List<String> learnedTitles) {
        String stageDesc = stageDesc(stage);
        StringBuilder userPrompt = new StringBuilder();
        if (profileText != null && !profileText.isBlank()) {
            userPrompt.append("我的学习档案：\n").append(profileText).append("\n\n");
        }
        if (learnedTitles != null && !learnedTitles.isEmpty()) {
            userPrompt.append("我已经学习的知识页：\n");
            for (String title : learnedTitles) {
                userPrompt.append("- ").append(title).append("\n");
            }
            userPrompt.append("\n请在此基础上规划下一步学习路径（学段：").append(stageDesc).append("）。只输出 JSON。");
        } else {
            userPrompt.append("我刚开始学习，请从基础开始规划学习路径（学段：").append(stageDesc).append("）。只输出 JSON。");
        }
        String raw;
        try {
            raw = chatClient.prompt()
                    .system(String.format(SYSTEM_PROMPT, stageDesc))
                    .user(userPrompt.toString())
                    .call()
                    .content();
        } catch (Exception e) {
            log.error("学习路径生成调用失败", e);
            throw new RuntimeException("学习路径生成失败");
        }
        return parse(raw);
    }

    private LearningPathPlan parse(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new RuntimeException("学习路径为空");
        }
        String text = raw.trim();
        if (text.startsWith("```")) {
            text = text.replaceFirst("^```(?:json)?\\s*", "").replaceFirst("\\s*```$", "");
        }
        JSONObject root = JSON.parseObject(text);
        if (root == null) {
            throw new RuntimeException("学习路径解析失败");
        }
        JSONArray steps = root.getJSONArray("steps");
        if (steps == null || steps.isEmpty()) {
            throw new RuntimeException("学习路径缺少步骤");
        }
        String title = root.getString("title");
        List<PlanStep> list = new ArrayList<>();
        for (int i = 0; i < steps.size() && i < MAX_STEPS; i++) {
            JSONObject step = steps.getJSONObject(i);
            if (step == null) {
                continue;
            }
            String stepTitle = step.getString("title");
            String desc = step.getString("desc");
            String kind = step.getString("kind");
            if ((stepTitle == null || stepTitle.isBlank()) && (desc == null || desc.isBlank())) {
                continue;
            }
            list.add(new PlanStep(
                    stepTitle == null ? "" : stepTitle,
                    desc == null ? "" : desc,
                    normalizeKind(kind)));
        }
        if (list.isEmpty()) {
            throw new RuntimeException("学习路径步骤为空");
        }
        return new LearningPathPlan(title == null ? "" : title, list);
    }

    private String normalizeKind(String kind) {
        if ("practice".equalsIgnoreCase(kind)) {
            return "practice";
        }
        if ("review".equalsIgnoreCase(kind)) {
            return "review";
        }
        return "learn";
    }

    private String stageDesc(String stage) {
        if (stage == null) {
            return "未知学段";
        }
        for (StageEnum item : StageEnum.values()) {
            if (item.getCode().equals(stage)) {
                return item.getDesc();
            }
        }
        return "未知学段";
    }

    public record PlanStep(String title, String desc, String kind) {
    }

    public record LearningPathPlan(String title, List<PlanStep> steps) {
        public String toJson() {
            JSONObject root = new JSONObject();
            root.put("title", title);
            JSONArray array = new JSONArray();
            for (PlanStep step : steps) {
                JSONObject item = new JSONObject();
                item.put("title", step.title());
                item.put("desc", step.desc());
                item.put("kind", step.kind());
                array.add(item);
            }
            root.put("steps", array);
            return root.toJSONString();
        }
    }
}