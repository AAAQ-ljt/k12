package com.smart.campus.admin.controller;

import com.smart.campus.admin.annotation.AdminPermission;
import com.smart.campus.admin.biz.ClassAdminBiz;
import com.smart.campus.controller.ABaseController;
import com.smart.campus.entity.po.ClassInfo;
import com.smart.campus.entity.query.ClassInfoQuery;
import com.smart.campus.entity.vo.ResponseVO;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@AdminPermission("basic-data:class")
@Validated
@RestController("classInfoController")
@RequestMapping("/classInfo")
public class ClassInfoController extends ABaseController {

    private final ClassAdminBiz classAdminBiz;

    public ClassInfoController(ClassAdminBiz classAdminBiz) {
        this.classAdminBiz = classAdminBiz;
    }

    @AdminPermission("teaching:course")
    @RequestMapping("/loadDataList")
    public ResponseVO loadDataList(ClassInfoQuery query) {
        return getSuccessResponseVO(classAdminBiz.loadDataList(query));
    }

    @AdminPermission("teaching:course")
    @RequestMapping("/loadSortList")
    public ResponseVO loadSortList(ClassInfoQuery query) {
        return getSuccessResponseVO(classAdminBiz.loadSortList(query));
    }

    @RequestMapping("/add")
    public ResponseVO add(@Validated(ClassInfo.Create.class) ClassInfo bean) {
        return getSuccessResponseVO(classAdminBiz.add(bean));
    }

    @RequestMapping("/getClassInfoById")
    public ResponseVO getClassInfoById(@NotNull(message = "班级ID不能为空") Integer classId) {
        return getSuccessResponseVO(classAdminBiz.getClassInfoById(classId));
    }

    @RequestMapping("/updateClassInfoById")
    public ResponseVO updateClassInfoById(@Validated(ClassInfo.Update.class) ClassInfo bean) {
        classAdminBiz.updateClassInfoById(bean);
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/deleteClassInfoById")
    public ResponseVO deleteClassInfoById(@NotNull(message = "班级ID不能为空") Integer classId) {
        classAdminBiz.deleteClassInfoById(classId);
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/deleteBatch")
    public ResponseVO deleteBatch(@NotBlank(message = "请选择需要删除的班级") String ids) {
        classAdminBiz.deleteBatch(ids);
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/updateSortOrder")
    public ResponseVO updateSortOrder(@NotBlank(message = "排序参数不能为空") String ids) {
        classAdminBiz.updateSortOrder(ids);
        return getSuccessResponseVO(null);
    }
}
