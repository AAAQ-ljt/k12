package com.nexora.admin.vo;

import java.util.ArrayList;
import java.util.List;

/**
 * 知识目录树节点：学段 → 学科 → 知识点。
 */
public class KnowledgeTreeNodeVO {

    private String key;
    private String label;
    private String type;
    private String stage;
    private String subject;
    private String knowledgePointId;
    private Integer difficulty;
    private Integer docCount = 0;
    private List<KnowledgeTreeNodeVO> children = new ArrayList<>();

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStage() {
        return stage;
    }

    public void setStage(String stage) {
        this.stage = stage;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getKnowledgePointId() {
        return knowledgePointId;
    }

    public void setKnowledgePointId(String knowledgePointId) {
        this.knowledgePointId = knowledgePointId;
    }

    public Integer getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(Integer difficulty) {
        this.difficulty = difficulty;
    }

    public Integer getDocCount() {
        return docCount;
    }

    public void setDocCount(Integer docCount) {
        this.docCount = docCount;
    }

    public List<KnowledgeTreeNodeVO> getChildren() {
        return children;
    }

    public void setChildren(List<KnowledgeTreeNodeVO> children) {
        this.children = children;
    }
}
