package com.nexora.admin.biz;

import com.nexora.admin.dto.QuestionGenerateDTO;
import com.nexora.admin.dto.QuestionTypeCountDTO;
import com.nexora.admin.vo.QuestionDraftVO;
import com.nexora.entity.enums.StageEnum;
import com.nexora.entity.po.KnowledgePoint;
import com.nexora.entity.po.QuestionOption;
import com.nexora.entity.po.ResourceInfo;
import com.nexora.entity.query.KnowledgePointQuery;
import com.nexora.entity.query.ResourceInfoQuery;
import com.nexora.exception.BusinessException;
import com.nexora.service.KnowledgePointService;
import com.nexora.service.ResourceInfoService;
import com.nexora.utils.StringTools;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * AI 出题与题目 Markdown 解析：解析产物为结构化草稿，管理员确认后由 QuestionBiz 批量入库。
 */
@Service
public class QuestionGenerateBiz {

    private static final String GENERATE_SYSTEM_PROMPT = """
            你是 K12 命题专家。严格按照下面的 Markdown 格式输出题目，不要输出任何解释，不要生成含图片的题目。
            每道题一个块，格式如下：
            ### 题目 1
            - 年级：高一
            - 难度：中等
            - 题型：单选题
            - 分值：5
            - 知识点：函数单调性
            - 题干：题目内容，公式用 $...$ 或 $$...$$ 表示
            - 选项：
              - A. 选项内容
              - B. 选项内容
            - 答案：A
            - 解析：答案解析
            注意：非选择题不要输出“选项”；选择题必须给出选项并给出唯一答案字母；题目数量必须与要求完全一致。""";

    private static final Pattern FIELD_LINE = Pattern.compile(
            "^[-*]?\\s*(年级|难度|题型|分值|知识点|题干|选项|答案|解析)\\s*[:：]\\s*(.*)$",
            Pattern.MULTILINE);
    private static final Pattern OPTION_LINE = Pattern.compile(
            "^[-*•]?\\s*([A-Ha-h])\\s*[.、)．:：]\\s*(.*)$");
    private static final Pattern IMAGE_LINK = Pattern.compile("!\\[[^\\]]*\\]\\(@([A-Za-z0-9_-]+)\\)");

    @Resource
    private ChatClient chatClient;

    @Resource
    private KnowledgePointService knowledgePointService;

    @Resource
    private ResourceInfoService resourceInfoService;

    @Value("${spring.ai.openai.chat.options.model:deepseek-v4-flash}")
    private String chatModel;

    public String generate(QuestionGenerateDTO dto) {
        validateGenerate(dto);
        KnowledgePoint point = knowledgePointService.getKnowledgePointByKnowledgePointId(dto.getKnowledgePointId());
        if (point == null) {
            throw new BusinessException("知识点不存在");
        }
        String stage = StageEnum.matchByGrade(dto.getGrade());
        if (stage == null || !stage.equals(point.getStage())) {
            throw new BusinessException("知识点与所选年级不匹配");
        }
        String resourceText = loadResourceText(dto.getResourceIds());
        String userPrompt = buildGeneratePrompt(dto, point.getName(), resourceText);
        String content = chatClient.prompt()
                .system(GENERATE_SYSTEM_PROMPT)
                .user(userPrompt)
                .options(OpenAiChatOptions.builder().model(chatModel).build())
                .call()
                .content();
        if (StringTools.isEmpty(content)) {
            throw new BusinessException("AI 出题失败，请稍后重试");
        }
        return normalizeKnowledgePoint(content, point.getName());
    }

    public List<QuestionDraftVO> parseMd(String markdown) {
        if (StringTools.isEmpty(markdown)) {
            throw new BusinessException("Markdown 内容不能为空");
        }
        List<String> blocks = splitBlocks(markdown);
        List<QuestionDraftVO> drafts = new ArrayList<>();
        for (String block : blocks) {
            QuestionDraftVO draft = parseBlock(block);
            if (draft != null) {
                drafts.add(draft);
            }
        }
        if (drafts.isEmpty()) {
            throw new BusinessException("未解析到有效题目");
        }
        resolveKnowledgePoints(drafts);
        return drafts;
    }

    private void validateGenerate(QuestionGenerateDTO dto) {
        if (dto == null || StringTools.isEmpty(dto.getGrade())) {
            throw new BusinessException("请选择年级");
        }
        if (StringTools.isEmpty(dto.getKnowledgePointId())) {
            throw new BusinessException("请选择知识点");
        }
        if (StageEnum.matchByGrade(dto.getGrade()) == null) {
            throw new BusinessException("非法的年级");
        }
        int total = distributionTotal(dto.getEasyDistribution())
                + distributionTotal(dto.getMediumDistribution())
                + distributionTotal(dto.getHardDistribution());
        if (total < 1 || total > 50) {
            throw new BusinessException("总题数必须为 1-50");
        }
        validateDistribution(dto.getEasyDistribution());
        validateDistribution(dto.getMediumDistribution());
        validateDistribution(dto.getHardDistribution());
    }

    private void validateDistribution(List<QuestionTypeCountDTO> distribution) {
        if (distribution == null || distribution.isEmpty()) {
            return;
        }
        Set<Integer> typeSet = new HashSet<>();
        for (QuestionTypeCountDTO item : distribution) {
            if (item.getQuestionType() == null || item.getQuestionType() < 0 || item.getQuestionType() > 7) {
                throw new BusinessException("非法的题型");
            }
            if (!typeSet.add(item.getQuestionType())) {
                throw new BusinessException("同一难度下题型不能重复");
            }
            if (item.getCount() == null || item.getCount() < 1 || item.getCount() > 30) {
                throw new BusinessException("单个题型的数量必须为 1-30");
            }
        }
    }

    private int distributionTotal(List<QuestionTypeCountDTO> distribution) {
        if (distribution == null || distribution.isEmpty()) {
            return 0;
        }
        return distribution.stream()
                .mapToInt(item -> item.getCount() == null ? 0 : item.getCount())
                .sum();
    }

    private String loadResourceText(List<String> resourceIds) {
        if (resourceIds == null || resourceIds.isEmpty()) {
            return "";
        }
        ResourceInfoQuery query = new ResourceInfoQuery();
        query.setResourceIds(resourceIds);
        List<ResourceInfo> resources = resourceInfoService.findListByParam(query);
        return resources.stream()
                .map(ResourceInfo::getResourceName)
                .filter(name -> !StringTools.isEmpty(name))
                .collect(Collectors.joining("、"));
    }

    private String buildGeneratePrompt(QuestionGenerateDTO dto, String pointName, String resourceText) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("年级：").append(dto.getGrade()).append("\n");
        prompt.append("知识点：").append(pointName).append("\n");
        if (!StringTools.isEmpty(dto.getDescription())) {
            prompt.append("出题要求：").append(dto.getDescription()).append("\n");
        }
        if (!StringTools.isEmpty(resourceText)) {
            prompt.append("参考资料：").append(resourceText).append("\n");
        }
        prompt.append("知识点必须严格使用：").append(pointName).append("，禁止改写、缩写或追加说明\n");
        prompt.append("题量分布：\n");
        prompt.append("- 简单：").append(distributionText(dto.getEasyDistribution())).append("\n");
        prompt.append("- 中等：").append(distributionText(dto.getMediumDistribution())).append("\n");
        prompt.append("- 困难：").append(distributionText(dto.getHardDistribution())).append("\n");
        return prompt.toString();
    }

    private String normalizeKnowledgePoint(String markdown, String pointName) {
        if (StringTools.isEmpty(markdown)) {
            return markdown;
        }
        String safeName = Matcher.quoteReplacement(pointName == null ? "" : pointName);
        return markdown.replaceAll("(?m)^(\\s*[-*]?\\s*知识点\\s*[:：]\\s*).*$", "$1" + safeName);
    }

    private String distributionText(List<QuestionTypeCountDTO> distribution) {
        if (distribution == null || distribution.isEmpty()) {
            return "不生成";
        }
        return distribution.stream()
                .map(item -> typeName(item.getQuestionType()) + item.getCount() + "题")
                .collect(Collectors.joining("、"));
    }

    private String typeName(Integer type) {
        return switch (type) {
            case 0 -> "单选题";
            case 1 -> "多选题";
            case 2 -> "判断题";
            case 3 -> "填空题";
            case 4 -> "简答题";
            case 5 -> "解答题";
            case 6 -> "论述题";
            case 7 -> "材料分析题";
            default -> "单选题";
        };
    }

    private List<String> splitBlocks(String markdown) {
        List<String> blocks = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        for (String line : markdown.split("\\r?\\n")) {
            String trimmed = line.trim();
            boolean start = trimmed.matches("^#{2,6}\\s*题目.*")
                    || trimmed.matches("^#{2,6}\\s*\\d+\\s*[.、)]?\\s*$")
                    || trimmed.matches("^[-=]{3,}\\s*$");
            if (start && current.toString().trim().length() > 0) {
                blocks.add(current.toString());
                current.setLength(0);
            }
            current.append(line).append("\n");
        }
        if (current.toString().trim().length() > 0) {
            blocks.add(current.toString());
        }
        return blocks;
    }

    private QuestionDraftVO parseBlock(String block) {
        String grade = fieldValue(block, "年级");
        String difficultyText = fieldValue(block, "难度");
        String typeText = fieldValue(block, "题型");
        String scoreText = fieldValue(block, "分值");
        String pointName = fieldValue(block, "知识点");
        String title = section(block, "题干");
        String optionsText = section(block, "选项");
        String answer = section(block, "答案");
        String analysis = section(block, "解析");
        if (StringTools.isEmpty(grade) || StringTools.isEmpty(pointName) || StringTools.isEmpty(title)) {
            return null;
        }
        String stage = StageEnum.matchByGrade(grade);
        if (stage == null) {
            throw new BusinessException("题目包含非法的年级：" + grade);
        }
        int difficulty = parseDifficulty(difficultyText);
        int questionType = parseQuestionType(typeText);
        int score = StringTools.isEmpty(scoreText) ? 5 : parseScore(scoreText);
        List<QuestionOption> options = parseOptions(optionsText);
        if (questionType == 0 || questionType == 1) {
            applyAnswerLetters(options, answer, questionType);
        } else if (StringTools.isEmpty(answer)) {
            throw new BusinessException("题目缺少答案：" + title);
        }
        QuestionDraftVO draft = new QuestionDraftVO();
        draft.setGrade(grade);
        draft.setStage(stage);
        draft.setKnowledgePointName(pointName);
        draft.setDifficulty(difficulty);
        draft.setQuestionType(questionType);
        draft.setScore(score);
        draft.setTitle(title);
        draft.setAnswer(answer);
        draft.setAnalysis(analysis);
        draft.setOptions(questionType == 0 || questionType == 1 ? options : null);
        draft.setQuestionImage(collectImages(title, optionsText, analysis));
        return draft;
    }

    private String fieldValue(String block, String field) {
        Matcher matcher = FIELD_LINE.matcher(block);
        while (matcher.find()) {
            if (field.equals(matcher.group(1))) {
                return matcher.group(2).trim();
            }
        }
        return null;
    }

    private String section(String block, String field) {
        Pattern pattern = Pattern.compile(
                "(?s)[-*]?\\s*" + field + "\\s*[:：]\\s*(.*?)(?=\\r?\\n\\s*[-*]?\\s*(?:年级|难度|题型|分值|知识点|题干|选项|答案|解析)\\s*[:：]|$)");
        Matcher matcher = pattern.matcher(block);
        if (!matcher.find()) {
            return null;
        }
        String value = matcher.group(1).trim();
        return value.isEmpty() ? null : value;
    }

    private List<QuestionOption> parseOptions(String optionsText) {
        List<QuestionOption> options = new ArrayList<>();
        if (StringTools.isEmpty(optionsText)) {
            return options;
        }
        for (String line : optionsText.split("\\r?\\n")) {
            Matcher matcher = OPTION_LINE.matcher(line.trim());
            if (!matcher.matches()) {
                continue;
            }
            QuestionOption option = new QuestionOption();
            option.setOptionLabel(matcher.group(1).toUpperCase());
            option.setOptionContent(matcher.group(2).trim());
            option.setIsAnswer(0);
            option.setSort(options.size() + 1);
            options.add(option);
        }
        return options;
    }

    private void applyAnswerLetters(List<QuestionOption> options, String answer, int questionType) {
        if (options.isEmpty()) {
            throw new BusinessException("选择题必须配置选项");
        }
        if (StringTools.isEmpty(answer)) {
            throw new BusinessException("选择题必须填写答案");
        }
        String letters = answer.toUpperCase().replaceAll("[^A-H]", "");
        int answerCount = 0;
        for (QuestionOption option : options) {
            if (letters.contains(option.getOptionLabel())) {
                option.setIsAnswer(1);
                answerCount++;
            }
        }
        if (answerCount == 0) {
            throw new BusinessException("选择题答案与选项不匹配：" + answer);
        }
        if (questionType == 0 && answerCount != 1) {
            throw new BusinessException("单选题只能有一个正确答案");
        }
    }

    private int parseDifficulty(String text) {
        if (StringTools.isEmpty(text)) {
            throw new BusinessException("题目缺少难度");
        }
        return switch (text.trim()) {
            case "简单", "易", "容易" -> 1;
            case "中等", "中" -> 2;
            case "困难", "难", "较难" -> 3;
            default -> throw new BusinessException("非法的难度：" + text);
        };
    }

    private int parseQuestionType(String text) {
        if (StringTools.isEmpty(text)) {
            throw new BusinessException("题目缺少题型");
        }
        String type = text.trim();
        if (type.startsWith("单选")) {
            return 0;
        }
        if (type.startsWith("多选")) {
            return 1;
        }
        if (type.startsWith("判断")) {
            return 2;
        }
        if (type.startsWith("填空")) {
            return 3;
        }
        if (type.startsWith("简答")) {
            return 4;
        }
        if (type.startsWith("解答")) {
            return 5;
        }
        if (type.startsWith("论述")) {
            return 6;
        }
        if (type.startsWith("材料")) {
            return 7;
        }
        throw new BusinessException("非法的题型：" + text);
    }

    private int parseScore(String text) {
        try {
            return Integer.parseInt(text.trim());
        } catch (NumberFormatException e) {
            throw new BusinessException("非法的分值：" + text);
        }
    }

    private String collectImages(String title, String optionsText, String analysis) {
        StringBuilder text = new StringBuilder();
        if (title != null) {
            text.append(title);
        }
        if (optionsText != null) {
            text.append(optionsText);
        }
        if (analysis != null) {
            text.append(analysis);
        }
        Set<String> ids = new LinkedHashSet<>();
        Matcher matcher = IMAGE_LINK.matcher(text);
        while (matcher.find()) {
            ids.add(matcher.group(1));
        }
        return ids.isEmpty() ? null : String.join(",", ids);
    }

    private void resolveKnowledgePoints(List<QuestionDraftVO> drafts) {
        Map<String, Map<String, KnowledgePoint>> stagePointMap = new HashMap<>();
        for (QuestionDraftVO draft : drafts) {
            String stage = draft.getStage();
            if (!stagePointMap.containsKey(stage)) {
                KnowledgePointQuery query = new KnowledgePointQuery();
                query.setStage(stage);
                Map<String, KnowledgePoint> byName = new HashMap<>();
                for (KnowledgePoint point : knowledgePointService.findListByParam(query)) {
                    byName.put(point.getName(), point);
                }
                stagePointMap.put(stage, byName);
            }
            KnowledgePoint point = stagePointMap.get(stage).get(draft.getKnowledgePointName());
            if (point == null) {
                throw new BusinessException("知识点不存在：" + draft.getKnowledgePointName() + "（" + draft.getGrade() + "）");
            }
            draft.setKnowledgePointId(point.getKnowledgePointId());
        }
    }
}
