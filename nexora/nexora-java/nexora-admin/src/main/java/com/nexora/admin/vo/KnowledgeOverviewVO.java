package com.nexora.admin.vo;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 知识总览统计。
 */
public class KnowledgeOverviewVO {

    private Integer totalDocs = 0;
    private Integer totalPoints = 0;
    private Integer totalChunks = 0;
    private Integer readyDocs = 0;
    private Integer failedDocs = 0;
    private Integer expiredDocs = 0;
    private Map<String, Integer> stageDistribution = new LinkedHashMap<>();
    private Map<String, Integer> vectorStatusDistribution = new LinkedHashMap<>();

    public Integer getTotalDocs() {
        return totalDocs;
    }

    public void setTotalDocs(Integer totalDocs) {
        this.totalDocs = totalDocs;
    }

    public Integer getTotalPoints() {
        return totalPoints;
    }

    public void setTotalPoints(Integer totalPoints) {
        this.totalPoints = totalPoints;
    }

    public Integer getTotalChunks() {
        return totalChunks;
    }

    public void setTotalChunks(Integer totalChunks) {
        this.totalChunks = totalChunks;
    }

    public Integer getReadyDocs() {
        return readyDocs;
    }

    public void setReadyDocs(Integer readyDocs) {
        this.readyDocs = readyDocs;
    }

    public Integer getFailedDocs() {
        return failedDocs;
    }

    public void setFailedDocs(Integer failedDocs) {
        this.failedDocs = failedDocs;
    }

    public Integer getExpiredDocs() {
        return expiredDocs;
    }

    public void setExpiredDocs(Integer expiredDocs) {
        this.expiredDocs = expiredDocs;
    }

    public Map<String, Integer> getStageDistribution() {
        return stageDistribution;
    }

    public void setStageDistribution(Map<String, Integer> stageDistribution) {
        this.stageDistribution = stageDistribution;
    }

    public Map<String, Integer> getVectorStatusDistribution() {
        return vectorStatusDistribution;
    }

    public void setVectorStatusDistribution(Map<String, Integer> vectorStatusDistribution) {
        this.vectorStatusDistribution = vectorStatusDistribution;
    }
}
