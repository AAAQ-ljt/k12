package com.smart.campus.admin.entity.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public class RolePermissionSaveDTO {

    @NotNull(message = "角色不能为空")
    private Integer roleType;

    private List<String> menuCodes;

    public Integer getRoleType() {
        return roleType;
    }

    public void setRoleType(Integer roleType) {
        this.roleType = roleType;
    }

    public List<String> getMenuCodes() {
        return menuCodes;
    }

    public void setMenuCodes(List<String> menuCodes) {
        this.menuCodes = menuCodes;
    }
}
