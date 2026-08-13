package com.nexora.entity.query;

/**
 * 资源目录表查询参数
 */
public class ResourceDirectoryQuery extends BaseParam {

    private String dirId;

    private String dirName;

    private String dirNameFuzzy;

    private String parentId;

    private Integer sort;

    public String getDirId() {
        return dirId;
    }

    public void setDirId(String dirId) {
        this.dirId = dirId;
    }

    public String getDirName() {
        return dirName;
    }

    public void setDirName(String dirName) {
        this.dirName = dirName;
    }

    public String getDirNameFuzzy() {
        return dirNameFuzzy;
    }

    public void setDirNameFuzzy(String dirNameFuzzy) {
        this.dirNameFuzzy = dirNameFuzzy;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public Integer getSort() {
        return sort;
    }

    public void setSort(Integer sort) {
        this.sort = sort;
    }
}
