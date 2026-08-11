package com.smart.campus.admin.entity.dto;

import jakarta.validation.constraints.NotBlank;

import java.io.Serializable;

public class QuestionOptionSaveDTO implements Serializable {

    @NotBlank(message = "选项内容不能为空", groups = {QuestionSaveDTO.Create.class, QuestionSaveDTO.Update.class})
    private String optionContent;

    private Integer sortOrder;

    public String getOptionContent() {
        return optionContent;
    }

    public void setOptionContent(String optionContent) {
        this.optionContent = optionContent;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }
}
