package com.nexora.admin.vo;

/**
 * 提示词生效情况（管理端「模型与提示词」页：枚举默认 / 数据库覆盖 / Redis 覆盖 三层）
 */
public class PromptEffectiveVO {

    /** 场景编码（PromptTypeEnum.scene），如 EXPLAIN / QUIZ / WIKI_SUMMARY */
    private String scene;

    /** 场景名称 */
    private String templateName;

    /** 查询学段 */
    private String stage;

    /** 生效来源：ENUM_DEFAULT 枚举默认 / DB 数据库 / REDIS Redis 覆盖 */
    private String source;

    /** 当前生效内容 */
    private String content;

    /** 数据库行的启用状态（1启用 0停用），来源非 DB 时为空 */
    private Integer status;

    /** 数据库行ID（用于编辑现有覆盖），无覆盖时为空 */
    private Integer id;

    /** 是否存在数据库覆盖行 */
    private Boolean dbOverride;

    public String getScene() {
        return scene;
    }

    public void setScene(String scene) {
        this.scene = scene;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public String getStage() {
        return stage;
    }

    public void setStage(String stage) {
        this.stage = stage;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Boolean getDbOverride() {
        return dbOverride;
    }

    public void setDbOverride(Boolean dbOverride) {
        this.dbOverride = dbOverride;
    }
}
