package com.nexora.vo;

import java.util.List;

/**
 * 学习路径兴趣分支（按 branchName 分组的节点）
 */
public class LearningPathBranchVO {

    /** 分支名 */
    private String branchName;

    /** 分支下节点 */
    private List<LearningPathNodeVO> nodes;

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    public List<LearningPathNodeVO> getNodes() {
        return nodes;
    }

    public void setNodes(List<LearningPathNodeVO> nodes) {
        this.nodes = nodes;
    }
}
