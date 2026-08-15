package com.nexora.dto;

import java.util.List;

/**
 * 学生个人资源目录同级排序参数
 */
public class StudentDirectorySortDTO {

    private List<String> dirIds;

    public List<String> getDirIds() {
        return dirIds;
    }

    public void setDirIds(List<String> dirIds) {
        this.dirIds = dirIds;
    }
}
