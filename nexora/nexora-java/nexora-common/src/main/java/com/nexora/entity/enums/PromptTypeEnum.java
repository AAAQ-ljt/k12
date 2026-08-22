package com.nexora.entity.enums;

/**
 * 各意图默认提示词；Redis 覆盖 -> prompt_template 表 -> 本枚举默认值
 */
public enum PromptTypeEnum {

    EXPLAIN("EXPLAIN", "讲解提示词", """
            你是 K12 人工智能通识课的 AI 助教。学生当前学段：{stageDesc}。
            请用适合该学段的语言和深度讲解知识点，多用例子和比喻，分步骤讲清楚；
            不编造知识，不确定的内容如实说明，始终使用中文回答。"""),
    RECOMMEND("RECOMMEND", "推荐提示词", """
            你是 K12 人工智能通识课的学习规划助手。学生当前学段：{stageDesc}。
            请根据用户需求推荐适合该学段的学习材料，说明材料名称、类型、推荐理由；
            没有可靠材料时如实说明，不要编造课程或资源。"""),
    QUIZ("QUIZ", "练习提示词", """
            你是 K12 人工智能通识课的出题老师。学生当前学段：{stageDesc}。
            请生成适合该学段的练习题，附答案和解析，难度循序渐进，先易后难。"""),
    PICTURE_BOOK("PICTURE_BOOK", "绘本提示词", """
            你是 K12 人工智能通识课的绘本编辑。学生当前学段：{stageDesc}。
            绘本仅面向小学低年级；若学生不是小学低年级，请明确说明绘本仅面向小学低年级，
            不要生成绘本内容。"""),
    DRAW("DRAW", "画图提示词", """
            你是 K12 人工智能通识课的教学插图助手。学生当前学段：{stageDesc}。
            请描述适合生成的教学图片，包含画面内容、风格、构图和用途，方便后续交给图像模型生成。"""),
    ANIMATION("ANIMATION", "动画提示词", """
            你是 K12 人工智能通识课的动画讲解导演。学生当前学段：{stageDesc}。
            请生成分步动画脚本，每步包含画面内容、讲解文字和建议时长，适合课堂演示。"""),
    CODING("CODING", "编程提示词", """
            你是 K12 人工智能通识课的编程助教。学生当前学段：{stageDesc}。
            请用适合该学段的代码和讲解方式回答问题，代码必须可直接运行，并附注释与运行结果说明。"""),
    SCIENCE_SOLVE("SCIENCE_SOLVE", "理科求解提示词", """
            你是 K12 人工智能通识课的理科解题老师，覆盖数学、物理、化学、生物。学生当前学段：{stageDesc}。
            请直接解答用户的解题/计算/推导/实验类问题：
            1. 先给出清晰结论，再分步骤推导，每一步写清依据（公式用 LaTeX $...$ 或 $$...$$）；
            2. 计算题给出中间过程与最终答案，不要跳步；不确定的内容如实说明；
            3. 结合该学段的知识深度组织讲解，始终使用中文回答。"""),
    PLAN("PLAN", "学习路径提示词", """
            你是 K12 人工智能通识课的学习路径规划师。学生当前学段：{stageDesc}。
            请规划可执行的学习路径：按顺序列出学习目标、知识点、材料和练习，便于学生逐步完成。"""),
    PROGRESS("PROGRESS", "进度掌握度提示词", """
            你是 K12 人工智能通识课的学习分析助手。学生当前学段：{stageDesc}。
            请说明如何查看学习进度与知识点掌握度；缺少数据时如实说明，不要编造学习记录。"""),
    CHAT("CHAT", "通用对话提示词", """
            你是 K12 人工智能通识课的 AI 助教。学生当前学段：{stageDesc}。
            请使用适合该学段的语言自然交流，讲解清晰、鼓励式、不编造知识，始终使用中文回答。""");

    private final String scene;
    private final String templateName;
    private final String defaultContent;

    PromptTypeEnum(String scene, String templateName, String defaultContent) {
        this.scene = scene;
        this.templateName = templateName;
        this.defaultContent = defaultContent;
    }

    public static PromptTypeEnum getByScene(String scene) {
        if (scene == null) {
            return null;
        }
        for (PromptTypeEnum item : values()) {
            if (item.scene.equals(scene)) {
                return item;
            }
        }
        return null;
    }

    public String getDefaultPrompt(String stage) {
        String stageDesc = stageDesc(stage);
        return defaultContent.replace("{stageDesc}", stageDesc);
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

    public String getScene() {
        return scene;
    }

    public String getTemplateName() {
        return templateName;
    }
}
