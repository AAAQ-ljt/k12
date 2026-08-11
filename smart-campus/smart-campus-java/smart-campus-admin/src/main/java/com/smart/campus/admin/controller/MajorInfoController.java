package com.smart.campus.admin.controller;

import com.smart.campus.admin.annotation.AdminPermission;
import com.smart.campus.admin.biz.MajorAdminBiz;
import com.smart.campus.controller.ABaseController;
import com.smart.campus.entity.po.MajorInfo;
import com.smart.campus.entity.query.MajorInfoQuery;
import com.smart.campus.entity.vo.ResponseVO;
import com.smart.campus.service.MajorInfoService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@AdminPermission("basic-data:major")
@Validated
@RestController("majorInfoController")
@RequestMapping("/majorInfo")
public class MajorInfoController extends ABaseController {

    private final MajorAdminBiz majorAdminBiz;
    private final MajorInfoService majorInfoService;

    public MajorInfoController(MajorAdminBiz majorAdminBiz, MajorInfoService majorInfoService) {
        this.majorAdminBiz = majorAdminBiz;
        this.majorInfoService = majorInfoService;
    }

    @AdminPermission("teaching:course")
    @RequestMapping("/loadDataList")
    public ResponseVO loadDataList(MajorInfoQuery query) {
        return getSuccessResponseVO(majorAdminBiz.loadDataList(query));
    }

    @AdminPermission("teaching:course")
    @RequestMapping("/loadSortList")
    public ResponseVO loadSortList(MajorInfoQuery query) {
        return getSuccessResponseVO(majorAdminBiz.loadSortList(query));
    }

    @RequestMapping("/add")
    public ResponseVO add(@Validated(MajorInfo.Create.class) MajorInfo bean) {
        return getSuccessResponseVO(majorAdminBiz.add(bean));
    }

    @RequestMapping("/getMajorInfoById")
    public ResponseVO getMajorInfoById(@NotNull(message = "专业ID不能为空") Integer majorId) {
        return getSuccessResponseVO(majorAdminBiz.getMajorInfoById(majorId));
    }

    @RequestMapping("/updateMajorInfoById")
    public ResponseVO updateMajorInfoById(@Validated(MajorInfo.Update.class) MajorInfo bean) {
        majorAdminBiz.updateMajorInfoById(bean);
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/deleteMajorInfoById")
    public ResponseVO deleteMajorInfoById(@NotNull(message = "专业ID不能为空") Integer majorId) {
        majorAdminBiz.deleteMajorInfoById(majorId);
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/updateSortOrder")
    public ResponseVO updateSortOrder(@NotBlank(message = "排序参数不能为空") String ids) {
        majorAdminBiz.updateSortOrder(ids);
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/getMajorInfoByMajorCode")
    public ResponseVO getMajorInfoByMajorCode(@NotBlank(message = "专业编码不能为空") String majorCode) {
        return getSuccessResponseVO(majorInfoService.getMajorInfoByMajorCode(majorCode));
    }

    @RequestMapping("/updateMajorInfoByMajorCode")
    public ResponseVO updateMajorInfoByMajorCode(MajorInfo bean, @NotBlank(message = "专业编码不能为空") String majorCode) {
        majorInfoService.updateMajorInfoByMajorCode(bean, majorCode);
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/deleteMajorInfoByMajorCode")
    public ResponseVO deleteMajorInfoByMajorCode(@NotBlank(message = "专业编码不能为空") String majorCode) {
        majorInfoService.deleteMajorInfoByMajorCode(majorCode);
        return getSuccessResponseVO(null);
    }
}
