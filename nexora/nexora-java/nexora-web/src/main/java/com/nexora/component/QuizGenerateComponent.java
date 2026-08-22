package com.nexora.component;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.nexora.entity.enums.StageEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 对话内出题组件：LLM 生成单选客观题 JSON（题干/选项/答案/解析），解析校验后供前端答题卡片即时判分。
 * 产物结构：{ "title": "...", "questions": [ { "type":"SINGLE", "question":"...", "options":["A. ..."], "answer":0, "analysis":"..." } ] }
 */
@Slf4j
@Component
public class QuizGenerateComponent {

    /** 单轮最多题目数，避免一次生成过多 */
    private static final int MAX_QUESTIONS = 6;

    private static final String SYSTEM_PROMPT = """
            你是 K12 人工智能通识课的出题老师。学生当前学段：%s。
            根据用户给出的学习主题生成一份小测验，只输出一个 JSON 对象，不要输出任何解释或 Markdown 代码块标记。
            JSON 结构：
            {
              "title": "测验标题（简短）",
              "questions": [
                {
                  "type": "SINGLE",
                  "question": "题干（简洁清晰）",
                  "options": ["A. 选项内容", "B. 选项内容", "C. 选项内容", "D. 选项内容"],
                  "answer": 0,
                  "analysis": "答案解析（适合该学段，中文）"
                }
              ]
            }
            要求：
            1. 生成 3-5 道单选客观题，围绕「人工智能通识课」或用户指定主题，难度循序渐进；
            2. 每题 4 个选项且只有一个正确，answer 是正确选项的下标（从 0 开始），必须与 options 一一对应；
            3. analysis 讲清楚为什么对、为什么错，语言生动易懂；
            4. 面向 %s 学段学生，避免超出其范围的表述。""";

    @Resource
    private ChatClient chatClient;

    @Value("${spring.ai.openai.chat.options.model:deepseek-v4-flash}")
    private String chatModel;

    /**
     * 生成校验后的测验脚本；LLM 输出不合法时抛出异常（调用方降级为文字出题）
     */
    public QuizScript generate(String stage, String topic) {
        String stageDesc = stageDesc(stage);
        String systemPrompt = String.format(SYSTEM_PROMPT, stageDesc, stageDesc);
        String userPrompt = "请围绕主题「" + (topic == null ? "" : topic)
                + "」出一份小测验（学生学段：" + stageDesc + "）。只输出 JSON。";
        String raw;
        try {
            raw = chatClient.prompt()
                    .system(systemPrompt)
                    .user(userPrompt)
                    .options(OpenAiChatOptions.builder().model(chatModel).build())
                    .call()
                    .content();
        } catch (Exception e) {
            log.error("出题生成调用失败", e);
            throw new RuntimeException("出题失败");
        }
        return parseAndValidate(raw);
    }

    private QuizScript parseAndValidate(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new RuntimeException("题目为空");
        }
        String text = raw.trim();
        if (text.startsWith("```")) {
            text = text.replaceFirst("^```(?:json)?\\s*", "").replaceFirst("\\s*```$", "");
        }
        JSONObject root;
        try {
            root = JSON.parseObject(text);
        } catch (Exception e) {
            throw new RuntimeException("题目解析失败");
        }
        if (root == null) {
            throw new RuntimeException("题目解析失败");
        }
        String title = root.getString("title");
        JSONArray questions = root.getJSONArray("questions");
        if (questions == null || questions.isEmpty()) {
            throw new RuntimeException("题目列表为空");
        }
        List<QuizQuestion> list = new ArrayList<>();
        for (int i = 0; i < questions.size() && i < MAX_QUESTIONS; i++) {
            JSONObject item = questions.getJSONObject(i);
            if (item == null) {
                continue;
            }
            String question = item.getString("question");
            JSONArray options = item.getJSONArray("options");
            Integer answer = item.getInteger("answer");
            String analysis = item.getString("analysis");
            if (question == null || question.isBlank() || options == null || options.size() < 2
                    || answer == null || answer < 0 || answer >= options.size()) {
                continue;
            }
            List<String> optionList = new ArrayList<>();
            for (int j = 0; j < options.size(); j++) {
                String option = options.getString(j);
                if (option == null || option.isBlank()) {
                    continue;
                }
                optionList.add(option);
            }
            if (optionList.size() < 2 || answer >= optionList.size()) {
                continue;
            }
            list.add(new QuizQuestion(
                    "SINGLE",
                    question,
                    optionList,
                    answer,
                    analysis == null ? "" : analysis));
        }
        if (list.isEmpty()) {
            throw new RuntimeException("有效题目为空");
        }
        return new QuizScript(title == null ? "" : title, list);
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

    public record QuizQuestion(String type, String question, List<String> options, int answer, String analysis) {
    }

    public record QuizScript(String title, List<QuizQuestion> questions) {
        public String toJson() {
            JSONObject root = new JSONObject();
            root.put("title", title);
            JSONArray array = new JSONArray();
            for (QuizQuestion q : questions) {
                JSONObject item = new JSONObject();
                item.put("type", q.type());
                item.put("question", q.question());
                item.put("options", q.options());
                item.put("answer", q.answer());
                item.put("analysis", q.analysis());
                array.add(item);
            }
            root.put("questions", array);
            return root.toJSONString();
        }
    }
}