package com.smart.campus.web.controller;

import com.smart.campus.controller.ABaseController;
import com.smart.campus.entity.vo.ResponseVO;
import com.smart.campus.web.biz.MessageCenterWebBiz;
import com.smart.campus.web.entity.dto.message.MessageCenterQueryDTO;
import com.smart.campus.web.entity.dto.message.MessageReadDTO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("webMessageCenterController")
@RequestMapping("/messageCenter")
public class MessageCenterController extends ABaseController {

    private final MessageCenterWebBiz messageCenterWebBiz;

    public MessageCenterController(MessageCenterWebBiz messageCenterWebBiz) {
        this.messageCenterWebBiz = messageCenterWebBiz;
    }

    @RequestMapping("/loadDashboard")
    public ResponseVO loadDashboard(MessageCenterQueryDTO dto) {
        return getSuccessResponseVO(messageCenterWebBiz.loadDashboard(dto));
    }

    @RequestMapping("/readMessage")
    public ResponseVO readMessage(@Valid MessageReadDTO dto) {
        return getSuccessResponseVO(messageCenterWebBiz.readMessage(dto));
    }
}
