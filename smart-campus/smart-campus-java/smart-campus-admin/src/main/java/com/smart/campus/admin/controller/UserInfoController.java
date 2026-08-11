package com.smart.campus.admin.controller;

import com.smart.campus.admin.annotation.AdminPermission;
import com.smart.campus.admin.biz.UserAdminBiz;
import com.smart.campus.controller.ABaseController;
import com.smart.campus.entity.po.UserInfo;
import com.smart.campus.entity.query.UserInfoQuery;
import com.smart.campus.entity.vo.ResponseVO;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController("userInfoController")
@RequestMapping("/userInfo")
public class UserInfoController extends ABaseController {

    private final UserAdminBiz userAdminBiz;

    public UserInfoController(UserAdminBiz userAdminBiz) {
        this.userAdminBiz = userAdminBiz;
    }

    @AdminPermission("basic-data:student")
    @RequestMapping("/loadStudentList")
    public ResponseVO loadStudentList(UserInfoQuery query) {
        return getSuccessResponseVO(userAdminBiz.loadStudentList(query));
    }

    @AdminPermission("basic-data:student")
    @RequestMapping("/getStudentById")
    public ResponseVO getStudentById(@NotNull(message = "学生ID不能为空") Integer userId) {
        return getSuccessResponseVO(userAdminBiz.getStudentById(userId));
    }

    @AdminPermission("basic-data:student")
    @RequestMapping("/addStudent")
    public ResponseVO addStudent(@Validated(UserInfo.CreateStudent.class) UserInfo bean) {
        return getSuccessResponseVO(userAdminBiz.addStudent(bean));
    }

    @AdminPermission("basic-data:student")
    @RequestMapping("/updateStudentById")
    public ResponseVO updateStudentById(@Validated(UserInfo.UpdateStudent.class) UserInfo bean) {
        userAdminBiz.updateStudentById(bean);
        return getSuccessResponseVO(null);
    }

    @AdminPermission("basic-data:student")
    @RequestMapping("/deleteStudentById")
    public ResponseVO deleteStudentById(@NotNull(message = "学生ID不能为空") Integer userId) {
        userAdminBiz.deleteStudentById(userId);
        return getSuccessResponseVO(null);
    }

    @AdminPermission("basic-data:student")
    @RequestMapping("/deleteStudentBatch")
    public ResponseVO deleteStudentBatch(@NotBlank(message = "请选择需要删除的学生") String ids) {
        userAdminBiz.deleteStudentBatch(ids);
        return getSuccessResponseVO(null);
    }

    @AdminPermission("basic-data:teacher")
    @RequestMapping("/loadTeacherList")
    public ResponseVO loadTeacherList(UserInfoQuery query) {
        return getSuccessResponseVO(userAdminBiz.loadTeacherList(query));
    }

    @AdminPermission("basic-data:teacher")
    @RequestMapping("/getTeacherById")
    public ResponseVO getTeacherById(@NotNull(message = "教师ID不能为空") Integer userId) {
        return getSuccessResponseVO(userAdminBiz.getTeacherById(userId));
    }

    @AdminPermission("basic-data:teacher")
    @RequestMapping("/addTeacher")
    public ResponseVO addTeacher(@Validated(UserInfo.CreateTeacher.class) UserInfo bean) {
        return getSuccessResponseVO(userAdminBiz.addTeacher(bean));
    }

    @AdminPermission("basic-data:teacher")
    @RequestMapping("/updateTeacherById")
    public ResponseVO updateTeacherById(@Validated(UserInfo.UpdateTeacher.class) UserInfo bean) {
        userAdminBiz.updateTeacherById(bean);
        return getSuccessResponseVO(null);
    }

    @AdminPermission("basic-data:teacher")
    @RequestMapping("/deleteTeacherById")
    public ResponseVO deleteTeacherById(@NotNull(message = "教师ID不能为空") Integer userId) {
        userAdminBiz.deleteTeacherById(userId);
        return getSuccessResponseVO(null);
    }

    @AdminPermission("basic-data:teacher")
    @RequestMapping("/deleteTeacherBatch")
    public ResponseVO deleteTeacherBatch(@NotBlank(message = "请选择需要删除的教师") String ids) {
        userAdminBiz.deleteTeacherBatch(ids);
        return getSuccessResponseVO(null);
    }
}
