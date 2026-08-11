package com.smart.campus.admin.controller;

import com.smart.campus.admin.annotation.AdminPermission;
import com.smart.campus.admin.biz.DepartmentAdminBiz;
import com.smart.campus.controller.ABaseController;
import com.smart.campus.entity.po.DepartmentInfo;
import com.smart.campus.entity.query.DepartmentInfoQuery;
import com.smart.campus.entity.vo.ResponseVO;
import com.smart.campus.service.DepartmentInfoService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@AdminPermission("basic-data:department")
@Validated
@RestController("departmentInfoController")
@RequestMapping("/departmentInfo")
public class DepartmentInfoController extends ABaseController {

    private final DepartmentAdminBiz departmentAdminBiz;
    private final DepartmentInfoService departmentInfoService;

    public DepartmentInfoController(DepartmentAdminBiz departmentAdminBiz, DepartmentInfoService departmentInfoService) {
        this.departmentAdminBiz = departmentAdminBiz;
        this.departmentInfoService = departmentInfoService;
    }

    @RequestMapping("/loadDataList")
    public ResponseVO loadDataList(DepartmentInfoQuery query) {
        return getSuccessResponseVO(departmentAdminBiz.loadDataList(query));
    }

    @RequestMapping("/loadSortList")
    public ResponseVO loadSortList(DepartmentInfoQuery query) {
        return getSuccessResponseVO(departmentAdminBiz.loadSortList(query));
    }

    @RequestMapping("/add")
    public ResponseVO add(@Validated(DepartmentInfo.Create.class) DepartmentInfo bean) {
        return getSuccessResponseVO(departmentAdminBiz.add(bean));
    }

    @RequestMapping("/getDepartmentInfoById")
    public ResponseVO getDepartmentInfoById(@NotNull(message = "院系ID不能为空") Integer departmentId) {
        return getSuccessResponseVO(departmentAdminBiz.getDepartmentInfoById(departmentId));
    }

    @RequestMapping("/updateDepartmentInfoById")
    public ResponseVO updateDepartmentInfoById(@Validated(DepartmentInfo.Update.class) DepartmentInfo bean) {
        departmentAdminBiz.updateDepartmentInfoById(bean);
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/deleteDepartmentInfoById")
    public ResponseVO deleteDepartmentInfoById(@NotNull(message = "院系ID不能为空") Integer departmentId) {
        departmentAdminBiz.deleteDepartmentInfoById(departmentId);
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/deleteBatch")
    public ResponseVO deleteBatch(@NotBlank(message = "请选择需要删除的院系") String ids) {
        departmentAdminBiz.deleteBatch(ids);
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/updateSortOrder")
    public ResponseVO updateSortOrder(@NotBlank(message = "排序参数不能为空") String ids) {
        departmentAdminBiz.updateSortOrder(ids);
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/getDepartmentInfoByDepartmentCode")
    public ResponseVO getDepartmentInfoByDepartmentCode(@NotBlank(message = "院系编码不能为空") String departmentCode) {
        return getSuccessResponseVO(departmentInfoService.getDepartmentInfoByDepartmentCode(departmentCode));
    }

    @RequestMapping("/updateDepartmentInfoByDepartmentCode")
    public ResponseVO updateDepartmentInfoByDepartmentCode(DepartmentInfo bean, @NotBlank(message = "院系编码不能为空") String departmentCode) {
        departmentInfoService.updateDepartmentInfoByDepartmentCode(bean, departmentCode);
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/deleteDepartmentInfoByDepartmentCode")
    public ResponseVO deleteDepartmentInfoByDepartmentCode(@NotBlank(message = "院系编码不能为空") String departmentCode) {
        departmentInfoService.deleteDepartmentInfoByDepartmentCode(departmentCode);
        return getSuccessResponseVO(null);
    }
}
