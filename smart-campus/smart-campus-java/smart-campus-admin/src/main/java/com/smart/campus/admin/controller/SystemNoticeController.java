package com.smart.campus.admin.controller;

import com.smart.campus.admin.annotation.AdminPermission;
import com.smart.campus.admin.biz.SystemNoticeAdminBiz;
import com.smart.campus.admin.entity.dto.SystemNoticeSaveDTO;
import com.smart.campus.controller.ABaseController;
import com.smart.campus.entity.query.SystemNoticeQuery;
import com.smart.campus.entity.vo.ResponseVO;
import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@AdminPermission("system:notice")
@Validated
@RestController("adminSystemNoticeController")
@RequestMapping("/systemNotice")
public class SystemNoticeController extends ABaseController {

    private final SystemNoticeAdminBiz systemNoticeAdminBiz;

    public SystemNoticeController(SystemNoticeAdminBiz systemNoticeAdminBiz) {
        this.systemNoticeAdminBiz = systemNoticeAdminBiz;
    }

    @RequestMapping("/loadDataList")
    public ResponseVO loadDataList(SystemNoticeQuery query) {
        return getSuccessResponseVO(systemNoticeAdminBiz.loadDataList(query));
    }

    @RequestMapping("/getSystemNoticeById")
    public ResponseVO getSystemNoticeById(@NotBlank(message = "公告ID不能为空") String noticeId) {
        return getSuccessResponseVO(systemNoticeAdminBiz.getSystemNoticeById(noticeId));
    }

    @RequestMapping("/add")
    public ResponseVO add(@RequestBody @Validated(SystemNoticeSaveDTO.Create.class) SystemNoticeSaveDTO dto) {
        return getSuccessResponseVO(systemNoticeAdminBiz.add(dto));
    }

    @RequestMapping("/updateSystemNoticeById")
    public ResponseVO updateSystemNoticeById(@RequestBody @Validated(SystemNoticeSaveDTO.Update.class) SystemNoticeSaveDTO dto) {
        systemNoticeAdminBiz.updateSystemNoticeById(dto);
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/publish")
    public ResponseVO publish(@NotBlank(message = "公告ID不能为空") String noticeId) {
        systemNoticeAdminBiz.publish(noticeId);
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/offline")
    public ResponseVO offline(@NotBlank(message = "公告ID不能为空") String noticeId) {
        systemNoticeAdminBiz.offline(noticeId);
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/deleteSystemNoticeById")
    public ResponseVO deleteSystemNoticeById(@NotBlank(message = "公告ID不能为空") String noticeId) {
        systemNoticeAdminBiz.deleteSystemNoticeById(noticeId);
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/deleteBatch")
    public ResponseVO deleteBatch(@NotBlank(message = "请选择需要删除的公告") String ids) {
        systemNoticeAdminBiz.deleteBatch(ids);
        return getSuccessResponseVO(null);
    }
}
