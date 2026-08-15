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

    /**
     * 归属用户ID；NULL=管理端公共目录
     */
    private String ownerId;

    /**
     * 仅查询管理端公共目录（owner_id IS NULL）
     */
    private Boolean ownerIdNull;

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

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public Boolean getOwnerIdNull() {
        return ownerIdNull;
    }

    public void setOwnerIdNull(Boolean ownerIdNull) {
        this.ownerIdNull = ownerIdNull;
    }
}
