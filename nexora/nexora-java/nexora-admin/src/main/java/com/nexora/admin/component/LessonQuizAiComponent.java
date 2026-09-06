package com.nexora.admin.component;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.nexora.constants.Constants;
import com.nexora.entity.po.QuestionInfo;
import com.nexora.entity.po.QuestionOption;
import com.nexora.exception.BusinessException;
import com.nexora.service.QuestionInfoService;
import com.nexora.service.QuestionOptionService;
import com.nexora.utils.StringTools;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 课时通关测验 AI 出题组件（admin）：LLM 逐题生成单选客观题，解析校验后落题目库
 * （question_info + question_option，必须挂载知识点），供课时测验异步任务调用。
 * 产物结构：{ "questions": [ { "question": "题干", "options": ["A. ..."], "answer": 0, "analysis": "解析" } ] }
 */
@Slf4j
@Component
public class LessonQuizAiComponent {

    private static final String SYSTEM_PROMPT = """
            你是 K12 人工智能通识课的出题老师。学生学段：%s，年级：%s。
            根据给出的知识点为「课时通关测验」生成%d道单选题，只输出一个 JSON 对象，不要输出任何解释或 Markdown 代码块标记。
            JSON 结构：
            {
              "questions": [
                {
                  "question": "题干（简洁清晰）",
                  "options": ["A. 选项内容", "B. 选项内容", "C. 选项内容", "D. 选项内容"],
                  "answer": 0,
                  "analysis": "答案解析（适合该学段，中文）"
                }
              ]
            }
            要求：
            1. 题数严格为 %d；
            2. 每题 4 个选项且只有一个正确，answer 是正确选项的下标（从 0 开始），必须与 options 一一对应；
            3. analysis 讲清楚为什么对、为什么错，语言生动易懂；
            4. 面向 %s 学段学生，难度为 %s，避免超出其范围的表述。""";

    @Resource
    private ChatClient chatClient;

    @Resource
    private QuestionInfoService questionInfoService;

    @Resource
    private QuestionOptionService questionOptionService;

    @Value("${spring.ai.openai.chat.options.model:deepseek-v4-flash}")
    private String chatModel;

    /** 单题草稿：校验通过后的生成结果 */
    public record QuizDraft(String title, List<String> options, int answerIndex, String analysis) {
    }

    /**
     * 生成单道单选题（异步任务逐题调用，便于上报进度）；LLM 输出不合法时抛出异常
     */
    public QuizDraft generateSingle(String stage, String grade, String topic, int difficulty) {
        int safeDifficulty = Math.max(1, Math.min(difficulty, 3));
        String stageDesc = stageDesc(stage);
        String difficultyText = switch (safeDifficulty) {
            case 1 -> "简单";
            case 2 -> "中等";
            default -> "困难";
        };
        String prompt = "知识点/主题：" + topic
                + "\n请按 JSON 结构生成 1 道单选测验题，题目必须围绕该知识点展开。";

        String content = chatClient.prompt()
                .system(String.format(SYSTEM_PROMPT, stageDesc, blank(grade), 1, 1, stageDesc, difficultyText))
                .user(prompt)
                .options(OpenAiChatOptions.builder().model(chatModel).build())
                .call()
                .content();
        if (StringTools.isEmpty(content)) {
            throw new BusinessException("AI 出题失败，请稍后重试");
        }
        return parseSingle(content);
    }

    /**
     * 草稿落库：题目挂载知识点（question_info.knowledge_point_id 非空约束），返回题目ID
     */
    public String saveDraft(String stage, String grade, int difficulty, String knowledgePointId,
                            QuizDraft draft, Date now) {
        String questionId = StringTools.getRandomNumber(Constants.LENGTH_15);
        QuestionInfo question = new QuestionInfo();
        question.setQuestionId(questionId);
        question.setKnowledgePointId(knowledgePointId);
        question.setStage(stage);
        question.setGrade(grade);
        question.setDifficulty(difficulty);
        question.setQuestionType(0); // 单选
        question.setTitle(draft.title());
        question.setScore(5);
        question.setAnswer(String.valueOf((char) ('A' + draft.answerIndex())));
        question.setAnalysis(draft.analysis());
        question.setSource(2); // 课时测验 AI 生成
        question.setAuditStatus(1);
        question.setStatus(1);
        question.setCreateTime(now);
        question.setUpdateTime(now);
        questionInfoService.add(question);

        List<QuestionOption> options = new ArrayList<>();
        for (int j = 0; j < draft.options().size(); j++) {
            String optionText = draft.options().get(j);
            if (StringTools.isEmpty(optionText)) {
                continue;
            }
            QuestionOption option = new QuestionOption();
            option.setQuestionId(questionId);
            option.setOptionLabel(String.valueOf((char) ('A' + j)));
            option.setOptionContent(optionText);
            option.setIsAnswer(j == draft.answerIndex() ? 1 : 0);
            option.setSort(j + 1);
            option.setCreateTime(now);
            options.add(option);
        }
        if (!options.isEmpty()) {
            questionOptionService.addBatch(options);
        }
        return questionId;
    }

    private QuizDraft parseSingle(String content) {
        String jsonText = extractJson(content);
        JSONObject root = JSON.parseObject(jsonText);
        JSONArray questions = root == null ? null : root.getJSONArray("questions");
        if (questions == null || questions.isEmpty()) {
            throw new BusinessException("AI 出题结果解析失败，请重新生成");
        }
        for (int i = 0; i < questions.size(); i++) {
            JSONObject item = questions.getJSONObject(i);
            String title = item.getString("question");
            JSONArray optionsArr = item.getJSONArray("options");
            Integer answerIndex = item.getInteger("answer");
            String analysis = item.getString("analysis");
            if (StringTools.isEmpty(title) || optionsArr == null || optionsArr.size() < 2
                    || answerIndex == null || answerIndex < 0 || answerIndex >= optionsArr.size()) {
                continue;
            }
            List<String> options = new ArrayList<>();
            for (int j = 0; j < optionsArr.size(); j++) {
                options.add(optionsArr.getString(j));
            }
            return new QuizDraft(title, options, answerIndex, analysis == null ? "" : analysis);
        }
        throw new BusinessException("AI 出题结果校验未通过，请重新生成");
    }

    private String extractJson(String content) {
        String text = content.trim();
        if (text.startsWith("```")) {
            int start = text.indexOf('{');
            int end = text.lastIndexOf('}');
            if (start >= 0 && end > start) {
                return text.substring(start, end + 1);
            }
        }
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return text.substring(start, end + 1);
        }
        return text;
    }

    private String stageDesc(String stage) {
        if (stage == null) {
            return "K12";
        }
        return switch (stage) {
            case "PRIMARY_LOW" -> "小学低年级";
            case "PRIMARY_HIGH" -> "小学高年级";
            case "JUNIOR" -> "初中";
            case "SENIOR" -> "高中";
            default -> stage;
        };
    }

    private String blank(String value) {
        return StringTools.isEmpty(value) ? "K12" : value;
    }
}
