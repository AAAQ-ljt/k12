package com.smart.campus.entity.vo;

import java.util.ArrayList;
import java.util.List;

public class SystemMenuVO {

    private String menuCode;

    private String menuName;

    private String parentCode;

    private String menuPath;

    private String routeName;

    private Integer menuType;

    private Integer sortOrder;

    private List<SystemMenuVO> children = new ArrayList<>();

    public String getMenuCode() {
        return menuCode;
    }

    public void setMenuCode(String menuCode) {
        this.menuCode = menuCode;
    }

    public String getMenuName() {
        return menuName;
    }

    public void setMenuName(String menuName) {
        this.menuName = menuName;
    }

    public String getParentCode() {
        return parentCode;
    }

    public void setParentCode(String parentCode) {
        this.parentCode = parentCode;
    }

    public String getMenuPath() {
        return menuPath;
    }

    public void setMenuPath(String menuPath) {
        this.menuPath = menuPath;
    }

    public String getRouteName() {
        return routeName;
    }

    public void setRouteName(String routeName) {
        this.routeName = routeName;
    }

    public Integer getMenuType() {
        return menuType;
    }

    public void setMenuType(Integer menuType) {
        this.menuType = menuType;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public List<SystemMenuVO> getChildren() {
        return children;
    }

    public void setChildren(List<SystemMenuVO> children) {
        this.children = children;
    }
}
