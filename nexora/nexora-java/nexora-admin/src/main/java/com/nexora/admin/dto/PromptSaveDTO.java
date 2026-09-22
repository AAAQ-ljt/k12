package com.nexora.admin.dto;

/**
 * 提示词保存入参（管理端「系统设置 → 模型与提示词」）
 */
public class PromptSaveDTO {

    /** 学段：ALL 或 StageEnum 编码 */
    private String stage;

    /** 场景：PromptTypeEnum.scene */
    private String scene;

    /** 提示词内容（支持 {stageDesc} 占位符） */
    private String content;

    /** 状态：1启用 0停用，可空默认启用 */
    private Integer status;

    public String getStage() {
        return stage;
    }

    public void setStage(String stage) {
        this.stage = stage;
    }

    public String getScene() {
        return scene;
    }

    public void setScene(String scene) {
        this.scene = scene;
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
}
