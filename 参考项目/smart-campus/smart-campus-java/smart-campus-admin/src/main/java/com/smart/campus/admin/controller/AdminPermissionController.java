package com.smart.campus.admin.controller;

import com.smart.campus.admin.annotation.AdminPermission;
import com.smart.campus.admin.biz.AdminPermissionBiz;
import com.smart.campus.admin.entity.dto.RolePermissionSaveDTO;
import com.smart.campus.controller.ABaseController;
import com.smart.campus.entity.vo.ResponseVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController("adminPermissionController")
@RequestMapping("/permission")
public class AdminPermissionController extends ABaseController {

    private final AdminPermissionBiz adminPermissionBiz;

    public AdminPermissionController(AdminPermissionBiz adminPermissionBiz) {
        this.adminPermissionBiz = adminPermissionBiz;
    }

    @RequestMapping("/loadRoleList")
    @AdminPermission("system:permission")
    public ResponseVO loadRoleList() {
        return getSuccessResponseVO(adminPermissionBiz.loadRoleList());
    }

    @RequestMapping("/loadMenuTree")
    @AdminPermission("system:permission")
    public ResponseVO loadMenuTree() {
        return getSuccessResponseVO(adminPermissionBiz.loadMenuTree());
    }

    @RequestMapping("/getRolePermission")
    @AdminPermission("system:permission")
    public ResponseVO getRolePermission(@NotNull(message = "角色不能为空") Integer roleType) {
        return getSuccessResponseVO(adminPermissionBiz.getRolePermission(roleType));
    }

    @RequestMapping("/saveRolePermission")
    @AdminPermission("system:permission")
    public ResponseVO saveRolePermission(@RequestBody @Validated @Valid RolePermissionSaveDTO dto) {
        adminPermissionBiz.saveRolePermission(dto);
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/getCurrentMenuList")
    public ResponseVO getCurrentMenuList() {
        return getSuccessResponseVO(adminPermissionBiz.getCurrentMenuList());
    }
}
