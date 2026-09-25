package com.nexora.entity.vo;

/**
 * 控制面板：待办事项项
 */
public class DashboardTodoItemVO {

    /** 待办唯一键（前端图标/文案枚举用） */
    private String key;

    /** 待办名称 */
    private String label;

    /** 数量 */
    private Long count;

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

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }
}