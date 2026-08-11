package com.smart.campus.web.controller;

import com.smart.campus.controller.ABaseController;
import com.smart.campus.web.biz.SystemNoticeWebBiz;
import com.smart.campus.entity.vo.ResponseVO;
import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController("webSystemNoticeController")
@RequestMapping("/systemNotice")
public class SystemNoticeController extends ABaseController {

    private final SystemNoticeWebBiz systemNoticeWebBiz;

    public SystemNoticeController(SystemNoticeWebBiz systemNoticeWebBiz) {
        this.systemNoticeWebBiz = systemNoticeWebBiz;
    }

    @RequestMapping("/loadLatest")
    public ResponseVO loadLatest(Integer pageNo, Integer pageSize) {
        return getSuccessResponseVO(systemNoticeWebBiz.loadLatest(pageNo, pageSize));
    }

    @RequestMapping("/getDetail")
    public ResponseVO getDetail(@NotBlank(message = "公告ID不能为空") String noticeId) {
        return getSuccessResponseVO(systemNoticeWebBiz.getDetail(noticeId));
    }
}
