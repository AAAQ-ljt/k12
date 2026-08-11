package com.smart.campus.admin.entity.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class PaperStructureSaveDTO implements Serializable {

    @NotBlank(message = "试卷ID不能为空")
    private String paperId;

    @Valid
    private List<PaperSectionSaveDTO> sectionList = new ArrayList<>();

    public String getPaperId() {
        return paperId;
    }

    public void setPaperId(String paperId) {
        this.paperId = paperId;
    }

    public List<PaperSectionSaveDTO> getSectionList() {
        return sectionList;
    }

    public void setSectionList(List<PaperSectionSaveDTO> sectionList) {
        this.sectionList = sectionList;
    }
}
