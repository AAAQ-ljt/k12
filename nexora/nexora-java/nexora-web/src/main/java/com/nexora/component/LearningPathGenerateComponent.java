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
 * 个性化学习路径生成组件：结合学习档案（目标 / 兴趣 / 关键问题）与已学内容，由 LLM 产出**可执行、可验收**的节点化路径。
 *
 * 产物（叙事层，存 ai_generation_record.content，带 pathId 索引）：
 * { "pathId", "title", "goal", "outcome", "cadence", "startHint",
 *   "stages": [ { "name", "goal", "checkpoint", "nodes": [ { "title","task","way","minutes","must","kind" } ] } ],
 *   "branches": [ { "name", "nodes": [ 同结构 ] } ] }
 *
 * 结构层（节点 / 三态 / 进度）由 LearningPathComponent 落 learning_path / learning_path_item，
 * 与叙事层按知识点名对齐。
 */
@Slf4j
@Component
public class LearningPathGenerateComponent {

    /** 阶段数量上限 */
    private static final int MAX_STAGES = 4;

    /** 单阶段节点上限 */
    private static final int MAX_STAGE_NODES = 4;

    /** 兴趣分支数量上限 */
    private static final int MAX_BRANCHES = 2;

    /** 单分支节点上限 */
    private static final int MAX_BRANCH_NODES = 3;

    private static final String SYSTEM_PROMPT = """
            你是 K12 人工智能通识课的「个性化学习路径规划师」，服务对象是学生本人。学生学段：%s。
            请根据学生的学习档案（目标 / 兴趣 / 关键问题）与已学内容，规划一份**清晰、可执行、可验收**的学习路径。
            只输出一个 JSON 对象，不要输出任何解释或 Markdown 代码块标记。

            JSON 结构：
            {
              "title": "路线标题（10 字以内，体现目标，如：三步做出气候分析作品）",
              "goal": "总目标：一句话说清学完能达到什么水平（面向学生，30 字以内）",
              "outcome": "产出物：学完能拿出的具体成果，1-2 项",
              "cadence": "建议节奏：每周几次、每次多久、约几周完成",
              "startHint": "起点建议：结合学生已有基础，说明从哪开始、哪些可以快速跳过",
              "stages": [
                {
                  "name": "阶段名（4-8 字，如：数据基础）",
                  "goal": "这一阶段的目标（一句话）",
                  "checkpoint": "阶段验收：能独立做出什么 / 答对什么才算过关",
                  "nodes": [
                    {
                      "title": "知识点名称（4-10 字，可学习可检测，如「数据与变量」；禁止「学习变量」「复习与调试」这类动作句）",
                      "task": "这一节点要动手完成的具体任务（可验收，如「写出 5 条地理变量清单」）",
                      "way": "学习方式（如：AI 讲解 + 动画；动手：表格工具；练习：3 道题）",
                      "minutes": 40,
                      "must": true,
                      "kind": "learn"
                    }
                  ]
                }
              ],
              "branches": [
                { "name": "兴趣分支名（如：气候变化分析方向）", "nodes": [ 与上面节点同结构 ] }
              ]
            }

            硬性要求：
            1. stages 为 2-4 个阶段，每阶段 2-4 个节点，主线节点合计 8-14 个，按先易后难、由基础到综合排序；
            2. 节点 title 必须是**可学习、可检测的知识点或主题**；task 必须是可验收的动手产出；minutes 取 20-60 的整数；
            3. must=true 表示必学核心节点（每阶段至少 1 个）；选学/拓展节点 must=false；
            4. kind 取 learn（讲解学习）/ practice（动手练习）/ review（阶段复习），每个阶段最后优先安排 review 或 practice；
            5. branches 为 0-2 个兴趣分支（依据学生兴趣），每个分支 2-3 个节点，must 一律 false；
            6. 语言面向学生本人、具体可执行，不要出现「教师」「课堂」「指导老师」等表述；
            7. 必须围绕学生的学习目标规划；学生给出的关键问题，至少一半节点要能回应。""";

    private final ChatClient chatClient;

    public LearningPathGenerateComponent(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    /**
     * 生成学习路径（叙事层结构）；LLM 输出不合法时抛出异常（调用方提示重试）
     */
    public LearningPathPlan generate(String stage, String profileText, List<String> learnedTitles) {
        String stageDesc = stageDesc(stage);
        StringBuilder userPrompt = new StringBuilder();
        userPrompt.append("我的学习档案：\n")
                .append(profileText == null || profileText.isBlank() ? "（未填写）" : profileText)
                .append("\n");
        if (learnedTitles != null && !learnedTitles.isEmpty()) {
            userPrompt.append("\n我已经学过的内容（可据此调整起点、避免重复）：\n");
            for (String title : learnedTitles) {
                userPrompt.append("- ").append(title).append("\n");
            }
        }
        userPrompt.append("\n请按上述要求规划我的学习路径（学段：").append(stageDesc).append("）。只输出 JSON。");
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
        List<StageDraft> stages = parseStages(root.getJSONArray("stages"));
        if (stages.isEmpty()) {
            // 兼容旧结构：steps / mainLine 视为单一主线阶段
            List<PlanNode> legacy = parseNodes(root.getJSONArray("mainLine"), MAX_STAGES * MAX_STAGE_NODES, true);
            if (legacy.isEmpty()) {
                legacy = parseNodes(root.getJSONArray("steps"), MAX_STAGES * MAX_STAGE_NODES, true);
            }
            if (legacy.isEmpty()) {
                throw new RuntimeException("学习路径缺少节点");
            }
            stages.add(new StageDraft("学习主线", "", "", legacy));
        }
        List<BranchDraft> branches = new ArrayList<>();
        JSONArray branchArray = root.getJSONArray("branches");
        if (branchArray != null) {
            for (int i = 0; i < branchArray.size() && branches.size() < MAX_BRANCHES; i++) {
                JSONObject branch = branchArray.getJSONObject(i);
                if (branch == null) {
                    continue;
                }
                List<PlanNode> nodes = parseNodes(branch.getJSONArray("nodes"), MAX_BRANCH_NODES, false);
                if (nodes.isEmpty()) {
                    continue;
                }
                String name = branch.getString("name");
                branches.add(new BranchDraft(name == null || name.isBlank() ? "兴趣拓展" : name.trim(), nodes));
            }
        }
        return new LearningPathPlan(
                textOf(root.getString("title"), "我的学习路径", 100),
                textOf(root.getString("goal"), "", 200),
                textOf(root.getString("outcome"), "", 300),
                textOf(root.getString("cadence"), "", 200),
                textOf(root.getString("startHint"), "", 300),
                stages,
                branches);
    }

    private List<StageDraft> parseStages(JSONArray array) {
        List<StageDraft> stages = new ArrayList<>();
        if (array == null) {
            return stages;
        }
        for (int i = 0; i < array.size() && stages.size() < MAX_STAGES; i++) {
            JSONObject stage = array.getJSONObject(i);
            if (stage == null) {
                continue;
            }
            List<PlanNode> nodes = parseNodes(stage.getJSONArray("nodes"), MAX_STAGE_NODES, true);
            if (nodes.isEmpty()) {
                continue;
            }
            stages.add(new StageDraft(
                    textOf(stage.getString("name"), "阶段" + (stages.size() + 1) + "：推进", 50),
                    textOf(stage.getString("goal"), "", 200),
                    textOf(stage.getString("checkpoint"), "", 200),
                    nodes));
        }
        return stages;
    }

    private List<PlanNode> parseNodes(JSONArray array, int max, boolean mainLine) {
        List<PlanNode> nodes = new ArrayList<>();
        if (array == null) {
            return nodes;
        }
        for (int i = 0; i < array.size() && nodes.size() < max; i++) {
            JSONObject node = array.getJSONObject(i);
            if (node == null) {
                continue;
            }
            String title = node.getString("title");
            if (title == null || title.isBlank()) {
                continue;
            }
            Integer minutes = node.getInteger("minutes");
            if (minutes == null || minutes < 10 || minutes > 120) {
                minutes = 40;
            }
            // 主线节点默认必学、分支节点一律选学
            Boolean must = mainLine ? !Boolean.FALSE.equals(node.getBoolean("must")) : Boolean.FALSE;
            nodes.add(new PlanNode(
                    textOf(title, "", 100),
                    textOf(node.getString("task"), "", 300),
                    textOf(node.getString("way"), "", 200),
                    minutes,
                    must,
                    normalizeKind(node.getString("kind"))));
        }
        return nodes;
    }

    private String textOf(String value, String fallback, int maxLength) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        String text = value.trim();
        return text.length() > maxLength ? text.substring(0, maxLength) : text;
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

    /** 路径节点（叙事层） */
    public record PlanNode(String title, String task, String way, Integer minutes, Boolean must, String kind) {
    }

    /** 阶段（叙事层） */
    public record StageDraft(String name, String goal, String checkpoint, List<PlanNode> nodes) {
    }

    /** 兴趣分支（叙事层） */
    public record BranchDraft(String name, List<PlanNode> nodes) {
    }

    /**
     * 路径叙事层：总目标 / 产出物 / 节奏 / 起点建议 + 阶段与分支
     */
    public record LearningPathPlan(String title, String goal, String outcome, String cadence, String startHint,
                                   List<StageDraft> stages, List<BranchDraft> branches) {

        /** 主线的扁平节点（用于落 learning_path_item） */
        public List<PlanNode> flatMainLine() {
            List<PlanNode> list = new ArrayList<>();
            for (StageDraft stage : stages) {
                list.addAll(stage.nodes());
            }
            return list;
        }

        /** 叙事层 JSON（存 ai_generation_record.content，pathId 做索引） */
        public String toJson(String pathId) {
            JSONObject root = new JSONObject();
            root.put("pathId", pathId);
            root.put("title", title);
            root.put("goal", goal);
            root.put("outcome", outcome);
            root.put("cadence", cadence);
            root.put("startHint", startHint);
            JSONArray stageArray = new JSONArray();
            for (StageDraft stage : stages) {
                JSONObject stageJson = new JSONObject();
                stageJson.put("name", stage.name());
                stageJson.put("goal", stage.goal());
                stageJson.put("checkpoint", stage.checkpoint());
                stageJson.put("nodes", nodesJson(stage.nodes()));
                stageArray.add(stageJson);
            }
            root.put("stages", stageArray);
            JSONArray branchArray = new JSONArray();
            for (BranchDraft branch : branches) {
                JSONObject branchJson = new JSONObject();
                branchJson.put("name", branch.name());
                branchJson.put("nodes", nodesJson(branch.nodes()));
                branchArray.add(branchJson);
            }
            root.put("branches", branchArray);
            return root.toJSONString();
        }

        private JSONArray nodesJson(List<PlanNode> nodes) {
            JSONArray array = new JSONArray();
            for (PlanNode node : nodes) {
                JSONObject json = new JSONObject();
                json.put("title", node.title());
                json.put("task", node.task());
                json.put("way", node.way());
                json.put("minutes", node.minutes());
                json.put("must", node.must());
                json.put("kind", node.kind());
                array.add(json);
            }
            return array;
        }
    }
}
