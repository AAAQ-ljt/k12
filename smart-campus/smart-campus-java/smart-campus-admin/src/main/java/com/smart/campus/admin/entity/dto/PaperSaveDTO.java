package com.smart.campus.admin.entity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

public class PaperSaveDTO implements Serializable {

    public interface Create {}

    public interface Update extends Create {}

    @NotBlank(message = "试卷ID不能为空", groups = Update.class)
    private String paperId;

    @NotBlank(message = "试卷名称不能为空", groups = {Create.class, Update.class})
    private String paperName;

    @NotNull(message = "试卷类型不能为空", groups = {Create.class, Update.class})
    private Integer paperType;

    private String description;

    public String getPaperId() {
        return paperId;
    }

    public void setPaperId(String paperId) {
        this.paperId = paperId;
    }

    public String getPaperName() {
        return paperName;
    }

    public void setPaperName(String paperName) {
        this.paperName = paperName;
    }

    public Integer getPaperType() {
        return paperType;
    }

    public void setPaperType(Integer paperType) {
        this.paperType = paperType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

}
