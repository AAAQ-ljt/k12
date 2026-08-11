package com.smart.campus.web.entity.vo.message;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MessageCenterDashboardVO implements Serializable {

    private Integer unreadCount;
    private List<MessageCenterItemVO> messageList = new ArrayList<>();
    private List<MessageCenterSummaryVO> summaryList = new ArrayList<>();
    private List<MessageCenterContactVO> contactList = new ArrayList<>();

    public Integer getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(Integer unreadCount) {
        this.unreadCount = unreadCount;
    }

    public List<MessageCenterItemVO> getMessageList() {
        return messageList;
    }

    public void setMessageList(List<MessageCenterItemVO> messageList) {
        this.messageList = messageList;
    }

    public List<MessageCenterSummaryVO> getSummaryList() {
        return summaryList;
    }

    public void setSummaryList(List<MessageCenterSummaryVO> summaryList) {
        this.summaryList = summaryList;
    }

    public List<MessageCenterContactVO> getContactList() {
        return contactList;
    }

    public void setContactList(List<MessageCenterContactVO> contactList) {
        this.contactList = contactList;
    }
}
