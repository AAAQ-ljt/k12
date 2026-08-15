package com.nexora.admin.dto;

import java.util.List;

/**
 * AI 出题配置：按简单/中等/困难三档分配各题型数量，不单独设置总题数。
 */
public class QuestionGenerateDTO {

    private String grade;

    private String knowledgePointId;

    private String description;

    private List<String> resourceIds;

    private List<QuestionTypeCountDTO> easyDistribution;

    private List<QuestionTypeCountDTO> mediumDistribution;

    private List<QuestionTypeCountDTO> hardDistribution;

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public String getKnowledgePointId() {
        return knowledgePointId;
    }

    public void setKnowledgePointId(String knowledgePointId) {
        this.knowledgePointId = knowledgePointId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getResourceIds() {
        return resourceIds;
    }

    public void setResourceIds(List<String> resourceIds) {
        this.resourceIds = resourceIds;
    }

    public List<QuestionTypeCountDTO> getEasyDistribution() {
        return easyDistribution;
    }

    public void setEasyDistribution(List<QuestionTypeCountDTO> easyDistribution) {
        this.easyDistribution = easyDistribution;
    }

    public List<QuestionTypeCountDTO> getMediumDistribution() {
        return mediumDistribution;
    }

    public void setMediumDistribution(List<QuestionTypeCountDTO> mediumDistribution) {
        this.mediumDistribution = mediumDistribution;
    }

    public List<QuestionTypeCountDTO> getHardDistribution() {
        return hardDistribution;
    }

    public void setHardDistribution(List<QuestionTypeCountDTO> hardDistribution) {
        this.hardDistribution = hardDistribution;
    }
}
