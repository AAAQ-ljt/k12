package com.smart.campus.entity.vo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ResourceTreeNodeVO implements Serializable {

    private Integer id;

    private Integer resourceId;

    private Integer parentId;

    private String resourceName;

    private Integer nodeType;

    private Integer resourceType;

    private Integer status;

    private List<ResourceTreeNodeVO> children = new ArrayList<>();

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getResourceId() {
        return resourceId;
    }

    public void setResourceId(Integer resourceId) {
        this.resourceId = resourceId;
    }

    public Integer getParentId() {
        return parentId;
    }

    public void setParentId(Integer parentId) {
        this.parentId = parentId;
    }

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    public Integer getNodeType() {
        return nodeType;
    }

    public void setNodeType(Integer nodeType) {
        this.nodeType = nodeType;
    }

    public Integer getResourceType() {
        return resourceType;
    }

    public void setResourceType(Integer resourceType) {
        this.resourceType = resourceType;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public List<ResourceTreeNodeVO> getChildren() {
        return children;
    }

    public void setChildren(List<ResourceTreeNodeVO> children) {
        this.children = children;
    }
}
