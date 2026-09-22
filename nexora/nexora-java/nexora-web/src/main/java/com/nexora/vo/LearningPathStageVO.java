package com.nexora.vo;

import java.util.List;

/**
 * 学习路径阶段（叙事层分组：阶段目标 + 阶段验收 + 节点）
 */
public class LearningPathStageVO {

    /** 阶段名 */
    private String name;

    /** 阶段目标 */
    private String goal;

    /** 阶段验收口径 */
    private String checkpoint;

    /** 该阶段节点（有序） */
    private List<LearningPathNodeVO> nodes;

    /** 阶段是否已全部掌握 */
    private Boolean finished;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGoal() {
        return goal;
    }

    public void setGoal(String goal) {
        this.goal = goal;
    }

    public String getCheckpoint() {
        return checkpoint;
    }

    public void setCheckpoint(String checkpoint) {
        this.checkpoint = checkpoint;
    }

    public List<LearningPathNodeVO> getNodes() {
        return nodes;
    }

    public void setNodes(List<LearningPathNodeVO> nodes) {
        this.nodes = nodes;
    }

    public Boolean getFinished() {
        return finished;
    }

    public void setFinished(Boolean finished) {
        this.finished = finished;
    }
}
