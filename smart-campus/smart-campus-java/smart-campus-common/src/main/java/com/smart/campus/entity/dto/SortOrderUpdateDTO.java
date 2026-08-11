package com.smart.campus.entity.dto;

public class SortOrderUpdateDTO {

    /**
     * 拖拽后的 ID 顺序，使用逗号分隔。
     */
    private String ids;

    public String getIds() {
        return ids;
    }

    public void setIds(String ids) {
        this.ids = ids;
    }
}
