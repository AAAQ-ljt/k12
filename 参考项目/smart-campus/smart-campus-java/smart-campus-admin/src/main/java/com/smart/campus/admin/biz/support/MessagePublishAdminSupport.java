package com.smart.campus.admin.biz.support;

import com.smart.campus.entity.enums.DateTimePatternEnum;
import com.smart.campus.entity.enums.UserRoleTypeEnum;
import com.smart.campus.entity.po.CourseInfo;
import com.smart.campus.entity.po.ExamInfo;
import com.smart.campus.entity.po.MessageInfo;
import com.smart.campus.entity.po.MessageUser;
import com.smart.campus.entity.po.SystemNotice;
import com.smart.campus.entity.po.UserInfo;
import com.smart.campus.entity.query.ClassInfoQuery;
import com.smart.campus.entity.query.UserInfoQuery;
import com.smart.campus.entity.vo.LoginUserVO;
import com.smart.campus.service.ClassInfoService;
import com.smart.campus.service.MessageInfoService;
import com.smart.campus.service.MessageUserService;
import com.smart.campus.service.UserInfoService;
import com.smart.campus.utils.DateUtil;
import com.smart.campus.utils.StringTools;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Component
public class MessagePublishAdminSupport {

    private static final int USER_ROLE_STUDENT = UserRoleTypeEnum.STUDENT.getCode();
    private static final int MESSAGE_TYPE_NOTICE = 1;
    private static final int MESSAGE_TYPE_COURSE = 2;
    private static final int MESSAGE_TYPE_EXAM = 4;
    private static final int BIZ_TYPE_COURSE = 1;
    private static final int BIZ_TYPE_EXAM = 3;
    private static final int BIZ_TYPE_NOTICE = 4;
    private static final int SEND_SCOPE_MULTI = 2;
    private static final int SEND_SCOPE_SINGLE = 1;
    private static final int SEND_SCOPE_ALL_STUDENT = 3;
    private static final int READ_FLAG_NO = 0;
    private static final int DELETE_FLAG_NO = 0;
    private static final int USER_STATUS_ENABLED = 1;
    private static final int NOTICE_TARGET_STUDENT = 1;
    private static final int NOTICE_TARGET_CLASS = 2;
    private static final int NOTICE_TARGET_MAJOR = 3;

    @Resource
    private MessageInfoService messageInfoService;

    @Resource
    private MessageUserService messageUserService;

    @Resource
    private UserInfoService userInfoService;

    @Resource
    private ClassInfoService classInfoService;

    public void sendCoursePublishMessage(CourseInfo courseInfo, List<Integer> classIdList, LoginUserVO loginUser) {
        if (courseInfo == null || loginUser == null) {
            return;
        }
        Set<Integer> receiverIdSet = loadStudentIdSet(classIdList);
        if (receiverIdSet.isEmpty()) {
            return;
        }
        String senderName = resolveSenderName(loginUser);
        String courseName = defaultString(courseInfo.getCourseName(), "新课程");
        Date now = new Date();

        MessageInfo messageInfo = new MessageInfo();
        messageInfo.setMessageTitle("新课程已发布：" + courseName);
        messageInfo.setMessageContent(senderName + " 发布了课程《" + courseName + "》，请及时进入课程查看并开始学习。");
        messageInfo.setMessageType(MESSAGE_TYPE_COURSE);
        messageInfo.setBizType(BIZ_TYPE_COURSE);
        messageInfo.setBizId(courseInfo.getCourseId());
        messageInfo.setSenderId(loginUser.getUserId());
        messageInfo.setSenderName(senderName);
        messageInfo.setSendScope(SEND_SCOPE_MULTI);
        messageInfo.setJumpPath("/courses");
        messageInfo.setSendTime(now);
        messageInfoService.add(messageInfo);

        saveReceivers(messageInfo.getMessageId(), receiverIdSet, now);
    }

    public void sendExamPublishMessage(ExamInfo examInfo, CourseInfo courseInfo, List<Integer> classIdList, LoginUserVO loginUser) {
        if (examInfo == null || loginUser == null) {
            return;
        }
        Set<Integer> receiverIdSet = loadStudentIdSet(classIdList);
        if (receiverIdSet.isEmpty()) {
            return;
        }
        String senderName = resolveSenderName(loginUser);
        String examName = defaultString(examInfo.getExamName(), "新考试");
        String courseName = courseInfo == null ? "" : StringTools.trim(courseInfo.getCourseName());
        String startTimeText = examInfo.getStartTime() == null ? "" : DateUtil.format(examInfo.getStartTime(),
                DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern());
        Date now = new Date();

        MessageInfo messageInfo = new MessageInfo();
        messageInfo.setMessageTitle("新考试已发布：" + examName);
        messageInfo.setMessageContent(buildExamMessageContent(senderName, examName, courseName, startTimeText));
        messageInfo.setMessageType(MESSAGE_TYPE_EXAM);
        messageInfo.setBizType(BIZ_TYPE_EXAM);
        messageInfo.setBizId(examInfo.getExamId());
        messageInfo.setSenderId(loginUser.getUserId());
        messageInfo.setSenderName(senderName);
        messageInfo.setSendScope(SEND_SCOPE_MULTI);
        messageInfo.setJumpPath("/exams");
        messageInfo.setSendTime(now);
        messageInfoService.add(messageInfo);

        saveReceivers(messageInfo.getMessageId(), receiverIdSet, now);
    }

    public void sendHomeworkJudgedSystemMessage(CourseInfo courseInfo,
                                                String lessonId,
                                                String lessonName,
                                                Integer studentId,
                                                String studentName,
                                                LoginUserVO loginUser,
                                                String scoreText) {
        if (studentId == null || loginUser == null) {
            return;
        }
        String senderName = resolveSenderName(loginUser);
        String courseName = courseInfo == null ? "课程" : defaultString(courseInfo.getCourseName(), "课程");
        String normalizedLessonName = defaultString(lessonName, "作业");
        Date now = new Date();

        MessageInfo messageInfo = new MessageInfo();
        messageInfo.setMessageTitle("作业已批改：" + normalizedLessonName);
        messageInfo.setMessageContent(buildHomeworkJudgedContent(senderName, courseName, normalizedLessonName, studentName, scoreText));
        messageInfo.setMessageType(1);
        messageInfo.setBizType(2);
        messageInfo.setBizId(lessonId);
        messageInfo.setSenderId(loginUser.getUserId());
        messageInfo.setSenderName(senderName);
        messageInfo.setSendScope(SEND_SCOPE_SINGLE);
        messageInfo.setJumpPath(buildHomeworkJumpPath(courseInfo == null ? null : courseInfo.getCourseId(), lessonId));
        messageInfo.setSendTime(now);
        messageInfoService.add(messageInfo);

        saveReceivers(messageInfo.getMessageId(), Set.of(studentId), now);
    }

    public void sendExamJudgedSystemMessage(ExamInfo examInfo,
                                            Integer studentId,
                                            String studentName,
                                            LoginUserVO loginUser,
                                            String scoreText) {
        if (examInfo == null || studentId == null || loginUser == null) {
            return;
        }
        String senderName = resolveSenderName(loginUser);
        String examName = defaultString(examInfo.getExamName(), "考试");
        Date now = new Date();

        MessageInfo messageInfo = new MessageInfo();
        messageInfo.setMessageTitle("考试已批改：" + examName);
        messageInfo.setMessageContent(buildExamJudgedContent(senderName, examName, studentName, scoreText));
        messageInfo.setMessageType(1);
        messageInfo.setBizType(3);
        messageInfo.setBizId(examInfo.getExamId());
        messageInfo.setSenderId(loginUser.getUserId());
        messageInfo.setSenderName(senderName);
        messageInfo.setSendScope(SEND_SCOPE_SINGLE);
        messageInfo.setJumpPath(buildExamJumpPath(examInfo.getExamId()));
        messageInfo.setSendTime(now);
        messageInfoService.add(messageInfo);

        saveReceivers(messageInfo.getMessageId(), Set.of(studentId), now);
    }

    public void sendNoticePublishMessage(SystemNotice notice, LoginUserVO loginUser) {
        if (notice == null || loginUser == null) {
            return;
        }
        Set<Integer> receiverIdSet = loadNoticeReceiverIdSet(notice);
        if (receiverIdSet.isEmpty()) {
            return;
        }
        String senderName = resolveSenderName(loginUser);
        Date now = new Date();

        MessageInfo messageInfo = new MessageInfo();
        messageInfo.setMessageTitle(notice.getNoticeTitle());
        messageInfo.setMessageContent(notice.getNoticeContent());
        messageInfo.setMessageType(MESSAGE_TYPE_NOTICE);
        messageInfo.setBizType(BIZ_TYPE_NOTICE);
        messageInfo.setBizId(notice.getNoticeId());
        messageInfo.setSenderId(loginUser.getUserId());
        messageInfo.setSenderName(senderName);
        messageInfo.setSendScope(resolveNoticeSendScope(notice.getTargetType()));
        messageInfo.setJumpPath("/message-center");
        messageInfo.setSendTime(now);
        messageInfoService.add(messageInfo);

        saveReceivers(messageInfo.getMessageId(), receiverIdSet, now);
    }

    private Set<Integer> loadNoticeReceiverIdSet(SystemNotice notice) {
        Integer targetType = notice.getTargetType();
        if (Objects.equals(targetType, NOTICE_TARGET_STUDENT)) {
            return loadAllStudentIdSet();
        }
        if (Objects.equals(targetType, NOTICE_TARGET_CLASS)) {
            return loadStudentIdSet(parseTargetIntegerIds(notice.getTargetIds()));
        }
        if (Objects.equals(targetType, NOTICE_TARGET_MAJOR)) {
            return loadStudentIdSet(loadMajorClassIdList(parseTargetIntegerIds(notice.getTargetIds())));
        }
        return Set.of();
    }

    private Set<Integer> loadAllStudentIdSet() {
        UserInfoQuery query = new UserInfoQuery();
        query.setRoleType(USER_ROLE_STUDENT);
        query.setStatus(USER_STATUS_ENABLED);
        List<UserInfo> studentList = userInfoService.findListByParam(query);
        LinkedHashSet<Integer> receiverIdSet = new LinkedHashSet<>();
        for (UserInfo student : studentList) {
            if (student != null && student.getUserId() != null) {
                receiverIdSet.add(student.getUserId());
            }
        }
        return receiverIdSet;
    }

    private List<Integer> loadMajorClassIdList(List<Integer> majorIdList) {
        if (majorIdList == null || majorIdList.isEmpty()) {
            return List.of();
        }
        LinkedHashSet<Integer> classIdSet = new LinkedHashSet<>();
        for (Integer majorId : new LinkedHashSet<>(majorIdList)) {
            ClassInfoQuery query = new ClassInfoQuery();
            query.setMajorId(majorId);
            classInfoService.findListByParam(query).stream()
                    .map(com.smart.campus.entity.po.ClassInfo::getClassId)
                    .filter(Objects::nonNull)
                    .forEach(classIdSet::add);
        }
        return new ArrayList<>(classIdSet);
    }

    private List<Integer> parseTargetIntegerIds(String targetIds) {
        if (StringTools.isEmpty(targetIds)) {
            return List.of();
        }
        LinkedHashSet<Integer> idSet = new LinkedHashSet<>();
        for (String targetId : targetIds.split(",")) {
            if (StringTools.isEmpty(targetId)) {
                continue;
            }
            try {
                idSet.add(Integer.valueOf(StringTools.trim(targetId)));
            } catch (NumberFormatException ignored) {
            }
        }
        return new ArrayList<>(idSet);
    }

    private Integer resolveNoticeSendScope(Integer targetType) {
        if (Objects.equals(targetType, NOTICE_TARGET_STUDENT)) {
            return SEND_SCOPE_ALL_STUDENT;
        }
        return SEND_SCOPE_MULTI;
    }

    private void saveReceivers(Long messageId, Set<Integer> receiverIdSet, Date createTime) {
        if (messageId == null || receiverIdSet.isEmpty()) {
            return;
        }
        List<MessageUser> saveList = new ArrayList<>();
        for (Integer userId : receiverIdSet) {
            if (userId == null) {
                continue;
            }
            MessageUser item = new MessageUser();
            item.setMessageId(messageId);
            item.setUserId(userId);
            item.setReadFlag(READ_FLAG_NO);
            item.setDeleteFlag(DELETE_FLAG_NO);
            item.setCreateTime(createTime);
            saveList.add(item);
        }
        if (!saveList.isEmpty()) {
            messageUserService.addBatch(saveList);
        }
    }

    private Set<Integer> loadStudentIdSet(List<Integer> classIdList) {
        LinkedHashSet<Integer> receiverIdSet = new LinkedHashSet<>();
        if (classIdList == null || classIdList.isEmpty()) {
            return receiverIdSet;
        }
        for (Integer classId : new LinkedHashSet<>(classIdList)) {
            if (classId == null) {
                continue;
            }
            UserInfoQuery query = new UserInfoQuery();
            query.setRoleType(USER_ROLE_STUDENT);
            query.setStatus(USER_STATUS_ENABLED);
            query.setClassId(String.valueOf(classId));
            List<UserInfo> studentList = userInfoService.findListByParam(query);
            for (UserInfo student : studentList) {
                if (student == null || student.getUserId() == null) {
                    continue;
                }
                if (containsClassId(student.getClassId(), classId)) {
                    receiverIdSet.add(student.getUserId());
                }
            }
        }
        return receiverIdSet;
    }

    private boolean containsClassId(String classIdText, Integer classId) {
        if (classId == null || StringTools.isEmpty(classIdText)) {
            return false;
        }
        for (String item : classIdText.split(",")) {
            String value = StringTools.trim(item);
            if (StringTools.isEmpty(value)) {
                continue;
            }
            try {
                if (Objects.equals(Integer.valueOf(value), classId)) {
                    return true;
                }
            } catch (NumberFormatException ignored) {
            }
        }
        return false;
    }

    private String buildExamMessageContent(String senderName, String examName, String courseName, String startTimeText) {
        StringBuilder builder = new StringBuilder();
        builder.append(senderName).append(" 发布了考试《").append(examName).append("》");
        if (!StringTools.isEmpty(courseName)) {
            builder.append("，所属课程《").append(courseName).append("》");
        }
        if (!StringTools.isEmpty(startTimeText)) {
            builder.append("，开始时间 ").append(startTimeText);
        }
        builder.append("，请及时查看并按时参加。");
        return builder.toString();
    }

    private String buildHomeworkJudgedContent(String senderName,
                                              String courseName,
                                              String lessonName,
                                              String studentName,
                                              String scoreText) {
        StringBuilder builder = new StringBuilder();
        if (!StringTools.isEmpty(studentName)) {
            builder.append(studentName).append("同学，");
        }
        builder.append(senderName)
                .append(" 已完成《")
                .append(courseName)
                .append("》中作业《")
                .append(lessonName)
                .append("》的批改");
        if (!StringTools.isEmpty(scoreText)) {
            builder.append("，当前得分 ").append(scoreText).append(" 分");
        }
        builder.append("，请及时查看。");
        return builder.toString();
    }

    private String buildExamJudgedContent(String senderName, String examName, String studentName, String scoreText) {
        StringBuilder builder = new StringBuilder();
        if (!StringTools.isEmpty(studentName)) {
            builder.append(studentName).append("同学，");
        }
        builder.append(senderName)
                .append(" 已完成考试《")
                .append(examName)
                .append("》的批改");
        if (!StringTools.isEmpty(scoreText)) {
            builder.append("，当前得分 ").append(scoreText).append(" 分");
        }
        builder.append("，请及时查看。");
        return builder.toString();
    }

    private String buildHomeworkJumpPath(String courseId, String lessonId) {
        if (StringTools.isEmpty(courseId) || StringTools.isEmpty(lessonId)) {
            return "/courses";
        }
        return "/courses/" + courseId + "/homework/" + lessonId;
    }

    private String buildExamJumpPath(String examId) {
        if (StringTools.isEmpty(examId)) {
            return "/exams";
        }
        return "/exams/" + examId;
    }

    private String resolveSenderName(LoginUserVO loginUser) {
        return defaultString(loginUser == null ? null : loginUser.getRealName(), "老师");
    }

    private String defaultString(String value, String defaultValue) {
        String normalized = StringTools.trim(value);
        return StringTools.isEmpty(normalized) ? defaultValue : normalized;
    }
}
