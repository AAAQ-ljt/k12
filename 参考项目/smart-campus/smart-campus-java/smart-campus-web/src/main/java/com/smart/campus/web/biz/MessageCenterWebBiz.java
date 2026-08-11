package com.smart.campus.web.biz;

import com.smart.campus.web.entity.dto.message.MessageCenterQueryDTO;
import com.smart.campus.web.entity.dto.message.MessageReadDTO;
import com.smart.campus.entity.enums.ResponseCodeEnum;
import com.smart.campus.entity.enums.UserRoleTypeEnum;
import com.smart.campus.entity.po.MessageInfo;
import com.smart.campus.entity.po.MessageUser;
import com.smart.campus.entity.po.UserInfo;
import com.smart.campus.entity.query.MessageUserQuery;
import com.smart.campus.entity.vo.LoginUserVO;
import com.smart.campus.exception.BusinessException;
import com.smart.campus.service.MessageInfoService;
import com.smart.campus.service.MessageUserService;
import com.smart.campus.service.UserInfoService;
import com.smart.campus.utils.LoginUserContextHolder;
import com.smart.campus.utils.StringTools;
import com.smart.campus.web.entity.vo.message.MessageCenterContactVO;
import com.smart.campus.web.entity.vo.message.MessageCenterDashboardVO;
import com.smart.campus.web.entity.vo.message.MessageCenterItemVO;
import com.smart.campus.web.entity.vo.message.MessageCenterSummaryVO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class MessageCenterWebBiz {

    private static final int USER_ROLE_STUDENT = UserRoleTypeEnum.STUDENT.getCode();
    private static final int READ_FLAG_NO = 0;
    private static final int READ_FLAG_YES = 1;
    private static final int DELETE_FLAG_NO = 0;
    private static final ZoneId ZONE_ID = ZoneId.of("Asia/Shanghai");
    private static final DateTimeFormatter MONTH_DAY_TIME_FORMATTER = DateTimeFormatter.ofPattern("MM-dd HH:mm");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    @Resource
    private UserInfoService userInfoService;

    @Resource
    private MessageInfoService messageInfoService;

    @Resource
    private MessageUserService messageUserService;

    public MessageCenterDashboardVO loadDashboard(MessageCenterQueryDTO dto) {
        UserInfo currentStudent = getCurrentStudent();
        List<MessageUser> receiptList = loadUserMessageList(currentStudent.getUserId(), dto);
        Map<Long, MessageInfo> messageInfoMap = loadMessageInfoMap(receiptList);

        MessageCenterDashboardVO vo = new MessageCenterDashboardVO();
        vo.setMessageList(buildMessageList(receiptList, messageInfoMap));

        List<MessageUser> allReceiptList = loadAllUserMessageList(currentStudent.getUserId());
        Map<Long, MessageInfo> allMessageInfoMap = loadMessageInfoMap(allReceiptList);
        vo.setUnreadCount((int) allReceiptList.stream()
                .filter(item -> Objects.equals(item.getReadFlag(), READ_FLAG_NO))
                .count());
        vo.setSummaryList(buildSummaryList(allReceiptList, allMessageInfoMap));
        vo.setContactList(buildContactList(allReceiptList, allMessageInfoMap));
        return vo;
    }

    @Transactional(rollbackFor = Exception.class)
    public Boolean readMessage(MessageReadDTO dto) {
        UserInfo currentStudent = getCurrentStudent();
        MessageUser receipt = messageUserService.getMessageUserByMessageIdAndUserId(dto.getMessageId(), currentStudent.getUserId());
        if (receipt == null || Objects.equals(receipt.getDeleteFlag(), 1)) {
            throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "消息不存在");
        }
        if (Objects.equals(receipt.getReadFlag(), READ_FLAG_YES)) {
            return true;
        }
        MessageUser updateBean = new MessageUser();
        updateBean.setReadFlag(READ_FLAG_YES);
        updateBean.setReadTime(new Date());
        messageUserService.updateMessageUserByMessageIdAndUserId(updateBean, dto.getMessageId(), currentStudent.getUserId());
        return true;
    }

    private List<MessageUser> loadUserMessageList(Integer userId, MessageCenterQueryDTO dto) {
        MessageUserQuery receiptQuery = new MessageUserQuery();
        receiptQuery.setUserId(userId);
        receiptQuery.setDeleteFlag(DELETE_FLAG_NO);
        receiptQuery.setOrderBy("m.create_time desc,m.id desc");
        List<MessageUser> receiptList = messageUserService.findListByParam(receiptQuery);
        if (receiptList.isEmpty()) {
            return List.of();
        }
        Map<Long, MessageInfo> messageInfoMap = loadMessageInfoMap(receiptList);
        Integer messageType = dto == null ? null : dto.getMessageType();
        return receiptList.stream()
                .filter(item -> {
                    if (messageType == null) {
                        return true;
                    }
                    MessageInfo messageInfo = messageInfoMap.get(item.getMessageId());
                    return messageInfo != null && Objects.equals(messageInfo.getMessageType(), messageType);
                })
                .limit(resolvePageSize(dto))
                .toList();
    }

    private List<MessageUser> loadAllUserMessageList(Integer userId) {
        MessageUserQuery receiptQuery = new MessageUserQuery();
        receiptQuery.setUserId(userId);
        receiptQuery.setDeleteFlag(DELETE_FLAG_NO);
        receiptQuery.setOrderBy("m.create_time desc,m.id desc");
        return messageUserService.findListByParam(receiptQuery);
    }

    private Map<Long, MessageInfo> loadMessageInfoMap(List<MessageUser> receiptList) {
        Set<Long> messageIdSet = receiptList.stream()
                .map(MessageUser::getMessageId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (messageIdSet.isEmpty()) {
            return Map.of();
        }
        List<MessageInfo> messageInfoList = new ArrayList<>();
        for (Long messageId : messageIdSet) {
            MessageInfo messageInfo = messageInfoService.getMessageInfoByMessageId(messageId);
            if (messageInfo != null) {
                messageInfoList.add(messageInfo);
            }
        }
        return messageInfoList.stream().collect(Collectors.toMap(MessageInfo::getMessageId, item -> item, (left, right) -> left));
    }

    private List<MessageCenterItemVO> buildMessageList(List<MessageUser> receiptList, Map<Long, MessageInfo> messageInfoMap) {
        List<MessageCenterItemVO> result = new ArrayList<>();
        for (MessageUser receipt : receiptList) {
            MessageInfo messageInfo = messageInfoMap.get(receipt.getMessageId());
            if (messageInfo == null) {
                continue;
            }
            MessageCenterItemVO item = new MessageCenterItemVO();
            item.setMessageId(messageInfo.getMessageId());
            item.setTitle(defaultString(messageInfo.getMessageTitle()));
            item.setContent(defaultString(messageInfo.getMessageContent()));
            item.setMessageType(messageInfo.getMessageType());
            item.setBizType(messageInfo.getBizType());
            item.setBizId(messageInfo.getBizId());
            item.setTypeKey(resolveTypeKey(messageInfo.getMessageType()));
            item.setTypeLabel(resolveTypeLabel(messageInfo.getMessageType()));
            item.setTimeText(formatMessageTime(messageInfo.getSendTime()));
            item.setUnread(Objects.equals(receipt.getReadFlag(), READ_FLAG_NO));
            item.setIcon(resolveTypeIcon(messageInfo.getMessageType()));
            item.setTheme(resolveTypeTheme(messageInfo.getMessageType()));
            item.setJumpPath(messageInfo.getJumpPath());
            item.setSenderName(messageInfo.getSenderName());
            result.add(item);
        }
        return result;
    }

    private List<MessageCenterSummaryVO> buildSummaryList(List<MessageUser> receiptList, Map<Long, MessageInfo> messageInfoMap) {
        List<Integer> typeList = List.of(1, 2, 4);
        List<MessageCenterSummaryVO> result = new ArrayList<>();
        for (Integer messageType : typeList) {
            int unreadCount = 0;
            for (MessageUser receipt : receiptList) {
                MessageInfo messageInfo = messageInfoMap.get(receipt.getMessageId());
                if (messageInfo == null || !Objects.equals(messageInfo.getMessageType(), messageType)) {
                    continue;
                }
                if (Objects.equals(receipt.getReadFlag(), READ_FLAG_NO)) {
                    unreadCount++;
                }
            }
            MessageCenterSummaryVO item = new MessageCenterSummaryVO();
            item.setMessageType(messageType);
            item.setKey(resolveTypeKey(messageType));
            item.setLabel(resolveTypeLabel(messageType));
            item.setCount(unreadCount);
            item.setIcon(resolveTypeIcon(messageType));
            item.setTheme(resolveTypeTheme(messageType));
            result.add(item);
        }
        return result;
    }

    private List<MessageCenterContactVO> buildContactList(List<MessageUser> receiptList, Map<Long, MessageInfo> messageInfoMap) {
        LinkedHashMap<String, MessageCenterContactVO> contactMap = new LinkedHashMap<>();
        List<MessageUser> sortedReceiptList = receiptList.stream()
                .sorted(Comparator.comparing(MessageUser::getCreateTime, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(MessageUser::getId, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
        for (MessageUser receipt : sortedReceiptList) {
            MessageInfo messageInfo = messageInfoMap.get(receipt.getMessageId());
            if (messageInfo == null || StringTools.isEmpty(messageInfo.getSenderName())) {
                continue;
            }
            String contactKey = defaultString(messageInfo.getSenderName()) + "-" + String.valueOf(messageInfo.getSenderId());
            if (contactMap.containsKey(contactKey)) {
                continue;
            }
            MessageCenterContactVO item = new MessageCenterContactVO();
            item.setName(defaultString(messageInfo.getSenderName()));
            item.setMeta(resolveContactMeta(messageInfo));
            item.setPreview(defaultString(messageInfo.getMessageContent()));
            item.setTimeText(formatMessageTime(messageInfo.getSendTime()));
            item.setUnread(Objects.equals(receipt.getReadFlag(), READ_FLAG_NO));
            item.setAvatar(extractAvatarText(messageInfo.getSenderName()));
            item.setAvatarTheme(resolveAvatarTheme(messageInfo.getMessageType()));
            contactMap.put(contactKey, item);
            if (contactMap.size() >= 4) {
                break;
            }
        }
        return new ArrayList<>(contactMap.values());
    }

    private int resolvePageSize(MessageCenterQueryDTO dto) {
        if (dto == null || dto.getPageSize() == null || dto.getPageSize() <= 0) {
            return 50;
        }
        return Math.min(100, dto.getPageSize());
    }

    private String resolveTypeKey(Integer messageType) {
        if (Objects.equals(messageType, 1)) {
            return "system";
        }
        if (Objects.equals(messageType, 2)) {
            return "course";
        }
        if (Objects.equals(messageType, 3)) {
            return "homework";
        }
        if (Objects.equals(messageType, 4)) {
            return "exam";
        }
        return "all";
    }

    private String resolveTypeLabel(Integer messageType) {
        if (Objects.equals(messageType, 1)) {
            return "系统通知";
        }
        if (Objects.equals(messageType, 2)) {
            return "课程消息";
        }
        if (Objects.equals(messageType, 3)) {
            return "作业消息";
        }
        if (Objects.equals(messageType, 4)) {
            return "考试消息";
        }
        return "消息";
    }

    private String resolveTypeIcon(Integer messageType) {
        if (Objects.equals(messageType, 1)) {
            return "铃";
        }
        if (Objects.equals(messageType, 2)) {
            return "课";
        }
        if (Objects.equals(messageType, 3)) {
            return "作";
        }
        if (Objects.equals(messageType, 4)) {
            return "考";
        }
        return "信";
    }

    private String resolveTypeTheme(Integer messageType) {
        if (Objects.equals(messageType, 1)) {
            return "is-blue";
        }
        if (Objects.equals(messageType, 2)) {
            return "is-green";
        }
        if (Objects.equals(messageType, 3)) {
            return "is-orange";
        }
        if (Objects.equals(messageType, 4)) {
            return "is-purple";
        }
        return "is-blue";
    }

    private String resolveAvatarTheme(Integer messageType) {
        if (Objects.equals(messageType, 1)) {
            return "is-assistant";
        }
        if (Objects.equals(messageType, 2)) {
            return "is-teacher";
        }
        if (Objects.equals(messageType, 3)) {
            return "is-teacher-dark";
        }
        if (Objects.equals(messageType, 4)) {
            return "is-student";
        }
        return "is-teacher";
    }

    private String resolveContactMeta(MessageInfo messageInfo) {
        if (StringTools.isEmpty(messageInfo.getSenderName())) {
            return resolveTypeLabel(messageInfo.getMessageType());
        }
        return resolveTypeLabel(messageInfo.getMessageType());
    }

    private String formatMessageTime(Date sendTime) {
        if (sendTime == null) {
            return "";
        }
        Instant instant = sendTime.toInstant();
        LocalDate targetDate = instant.atZone(ZONE_ID).toLocalDate();
        LocalDate today = LocalDate.now(ZONE_ID);
        if (Objects.equals(targetDate, today)) {
            return TIME_FORMATTER.format(instant.atZone(ZONE_ID).toLocalTime());
        }
        if (Objects.equals(targetDate, today.minusDays(1))) {
            return "昨天 " + TIME_FORMATTER.format(instant.atZone(ZONE_ID).toLocalTime());
        }
        long days = Math.abs(Duration.between(instant, Instant.now()).toDays());
        if (days < 365) {
            return MONTH_DAY_TIME_FORMATTER.format(instant.atZone(ZONE_ID).toLocalDateTime());
        }
        return instant.atZone(ZONE_ID).toLocalDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }

    private String extractAvatarText(String senderName) {
        String value = defaultString(StringTools.trim(senderName));
        return value.isEmpty() ? "信" : value.substring(0, 1);
    }

    private String defaultString(String value) {
        return value == null ? "" : value;
    }

    private UserInfo getCurrentStudent() {
        LoginUserVO loginUser = LoginUserContextHolder.get();
        if (loginUser == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_901);
        }
        if (!Objects.equals(loginUser.getRoleType(), USER_ROLE_STUDENT)) {
            throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "请使用学生账号查看消息中心");
        }
        UserInfo userInfo = userInfoService.getUserInfoByUserId(loginUser.getUserId());
        if (userInfo == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "当前学生信息不存在");
        }
        return userInfo;
    }
}
