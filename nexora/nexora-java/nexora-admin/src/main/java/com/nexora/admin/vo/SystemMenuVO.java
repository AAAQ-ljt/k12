package com.nexora.admin.vo;

import com.nexora.entity.po.SystemMenu;

import java.util.List;

/**
 * 菜单树节点 VO — 继承 SystemMenu 并增加 children 字段
 */
public class SystemMenuVO extends SystemMenu {

    private List<SystemMenuVO> children;

    public List<SystemMenuVO> getChildren() {
        return children;
    }

    public void setChildren(List<SystemMenuVO> children) {
        this.children = children;
    }
}
