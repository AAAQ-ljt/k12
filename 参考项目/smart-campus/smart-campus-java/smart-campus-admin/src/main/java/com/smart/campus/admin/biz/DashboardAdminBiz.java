package com.smart.campus.admin.biz;

import com.smart.campus.entity.enums.DateTimePatternEnum;
import com.smart.campus.entity.enums.UserRoleTypeEnum;
import com.smart.campus.entity.po.CourseAssessmentSubmit;
import com.smart.campus.entity.po.CourseChapterLesson;
import com.smart.campus.entity.po.CourseInfo;
import com.smart.campus.entity.po.ExamInfo;
import com.smart.campus.entity.po.ResourceInfo;
import com.smart.campus.entity.po.SystemNotice;
import com.smart.campus.entity.query.CourseAssessmentSubmitQuery;
import com.smart.campus.entity.query.CourseInfoQuery;
import com.smart.campus.entity.query.ExamInfoQuery;
import com.smart.campus.entity.query.ResourceInfoQuery;
import com.smart.campus.entity.query.SystemNoticeQuery;
import com.smart.campus.entity.query.UserInfoQuery;
import com.smart.campus.admin.entity.vo.AdminDashboardActivityVO;
import com.smart.campus.admin.entity.vo.AdminDashboardMetricVO;
import com.smart.campus.admin.entity.vo.AdminDashboardResourceStatVO;
import com.smart.campus.admin.entity.vo.AdminDashboardTodoVO;
import com.smart.campus.admin.entity.vo.AdminDashboardTrendVO;
import com.smart.campus.admin.entity.vo.AdminDashboardVO;
import com.smart.campus.entity.vo.LoginUserVO;
import com.smart.campus.entity.vo.PaginationResultVO;
import com.smart.campus.service.CourseAssessmentSubmitService;
import com.smart.campus.service.CourseChapterLessonService;
import com.smart.campus.service.CourseInfoService;
import com.smart.campus.service.ExamInfoService;
import com.smart.campus.service.ResourceInfoService;
import com.smart.campus.service.SystemNoticeService;
import com.smart.campus.service.UserInfoService;
import com.smart.campus.utils.DateUtil;
import com.smart.campus.utils.LoginUserContextHolder;
import com.smart.campus.utils.StringTools;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Service
public class DashboardAdminBiz {

    private static final int USER_STATUS_ENABLED = 1;
    private static final int COURSE_STATUS_ENABLED = 1;
    private static final int RESOURCE_NODE_RESOURCE = 2;
    private static final int RESOURCE_STATUS_SUCCESS = 3;
    private static final int EXAM_STATUS_PUBLISHED = 1;
    private static final int NOTICE_STATUS_DRAFT = 0;
    private static final int NOTICE_STATUS_PUBLISHED = 1;
    private static final int TASK_TYPE_HOMEWORK = 1;
    private static final int TASK_TYPE_EXAM = 2;
    private static final int SUBMIT_STATUS_SUBMITTED = 3;
    private static final int JUDGE_STATUS_MANUAL_PENDING = 2;
    private static final int TREND_DAYS = 7;
    private static final int LATEST_SIZE = 4;
    private static final long STORAGE_CAPACITY_BYTES = 100L * 1024 * 1024 * 1024;
    private static final String DATE_TIME_PATTERN = DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern();
    private static final String ROUTE_COURSE = "/teaching/course";
    private static final String ROUTE_HOMEWORK = "/teaching/course/homework";
    private static final String ROUTE_EXAM = "/teaching/exam";
    private static final String ROUTE_RESOURCE = "/resource/manage";
    private static final String ROUTE_NOTICE = "/system/notice";

    @Resource
    private UserInfoService userInfoService;

    @Resource
    private CourseInfoService courseInfoService;

    @Resource
    private ResourceInfoService resourceInfoService;

    @Resource
    private ExamInfoService examInfoService;

    @Resource
    private CourseAssessmentSubmitService courseAssessmentSubmitService;

    @Resource
    private CourseChapterLessonService courseChapterLessonService;

    @Resource
    private SystemNoticeService systemNoticeService;

    /**
     * 聚合后台首页需要的核心指标、趋势、资源、待办和动态数据。
     */
    public AdminDashboardVO loadDashboard() {
        AdminDashboardVO dashboard = new AdminDashboardVO();
        ResourceSummary resourceSummary = loadResourceSummary();
        dashboard.setMetricCards(loadMetricCards(resourceSummary));
        dashboard.setTeachingTrend(loadTeachingTrend());
        dashboard.setResourceStats(loadResourceStats(resourceSummary));
        dashboard.setTodoList(loadTodoList());
        dashboard.setActivityList(loadActivityList());
        dashboard.setTotalResourceCount(resourceSummary.totalCount);
        dashboard.setStorageUsagePercent(resourceSummary.storageUsagePercent);
        return dashboard;
    }

    private List<AdminDashboardMetricVO> loadMetricCards(ResourceSummary resourceSummary) {
        List<AdminDashboardMetricVO> metricList = new ArrayList<>();
        Integer studentCount = countUsers(UserRoleTypeEnum.STUDENT.getCode());
        Integer teacherCount = countUsers(UserRoleTypeEnum.TEACHER.getCode());
        Integer courseCount = countEnabledCourses(null, null);
        metricList.add(buildMetric("student", "在校学生", studentCount, "人", "icon-user", "is-blue"));
        metricList.add(buildMetric("teacher", "授课教师", teacherCount, "人", "icon-geren", "is-green"));
        metricList.add(buildMetric("course", "开设课程", courseCount, "门", "icon-xinrenkecheng", "is-purple"));
        metricList.add(buildMetric("resource", "资源文件", resourceSummary.totalCount, "个", "icon-attachment", "is-orange"));
        return metricList;
    }

    private Integer countUsers(Integer roleType) {
        UserInfoQuery query = new UserInfoQuery();
        query.setRoleType(roleType);
        query.setStatus(USER_STATUS_ENABLED);
        return safeCount(userInfoService.findCountByParam(query));
    }

    private AdminDashboardMetricVO buildMetric(String key, String title, Integer value, String unit, String icon, String theme) {
        AdminDashboardMetricVO metric = new AdminDashboardMetricVO();
        metric.setKey(key);
        metric.setTitle(title);
        metric.setValue(safeCount(value));
        metric.setUnit(unit);
        metric.setIcon(icon);
        metric.setTheme(theme);
        return metric;
    }

    /**
     * 按最近 7 个自然日统计课程、考试和作业提交趋势。
     */
    private List<AdminDashboardTrendVO> loadTeachingTrend() {
        List<AdminDashboardTrendVO> trendList = new ArrayList<>();
        LocalDate today = LocalDate.now();
        for (int index = TREND_DAYS - 1; index >= 0; index--) {
            LocalDate day = today.minusDays(index);
            String startTime = formatDateTime(day.atStartOfDay());
            String endTime = formatDateTime(day.atTime(LocalTime.MAX));
            AdminDashboardTrendVO trend = new AdminDashboardTrendVO();
            trend.setDay(formatDayLabel(day));
            trend.setLabel(day.toString());
            trend.setCourse(countEnabledCourses(startTime, endTime));
            trend.setExam(countPublishedExams(startTime, endTime));
            trend.setHomework(countSubmittedHomework(startTime, endTime));
            trendList.add(trend);
        }
        return trendList;
    }

    private Integer countEnabledCourses(String startTime, String endTime) {
        CourseInfoQuery query = new CourseInfoQuery();
        query.setStatus(COURSE_STATUS_ENABLED);
        query.setCreateTimeStart(startTime);
        query.setCreateTimeEnd(endTime);
        return safeCount(courseInfoService.findCountByParam(query));
    }

    private Integer countPublishedExams(String startTime, String endTime) {
        return countPublishedExams(startTime, endTime, null);
    }

    private Integer countPublishedExams(String startTime, String endTime, LoginUserVO teacher) {
        ExamInfoQuery query = new ExamInfoQuery();
        query.setStatus(EXAM_STATUS_PUBLISHED);
        query.setStartTimeStart(startTime);
        query.setStartTimeEnd(endTime);
        if (teacher != null) {
            query.setTeacherId(teacher.getUserId());
        }
        return safeCount(examInfoService.findCountByParam(query));
    }

    private Integer countSubmittedHomework(String startTime, String endTime) {
        CourseAssessmentSubmitQuery query = new CourseAssessmentSubmitQuery();
        query.setTaskType(TASK_TYPE_HOMEWORK);
        query.setSubmitStatus(SUBMIT_STATUS_SUBMITTED);
        query.setSubmitTimeStart(startTime);
        query.setSubmitTimeEnd(endTime);
        return safeCount(courseAssessmentSubmitService.findCountByParam(query));
    }

    private List<AdminDashboardResourceStatVO> loadResourceStats(ResourceSummary resourceSummary) {
        List<AdminDashboardResourceStatVO> statList = new ArrayList<>();
        statList.add(buildResourceStat(3, "document", "文档资料", "is-blue", "icon-attachment", resourceSummary));
        statList.add(buildResourceStat(1, "video", "教学视频", "is-purple", "icon-play-cover", resourceSummary));
        statList.add(buildResourceStat(2, "image", "教学图片", "is-green", "icon-calendar", resourceSummary));
        statList.add(buildResourceStat(4, "archive", "压缩资料", "is-orange", "icon-collection", resourceSummary));
        return statList;
    }

    private AdminDashboardResourceStatVO buildResourceStat(Integer resourceType, String typeKey, String typeName, String theme, String icon, ResourceSummary resourceSummary) {
        ResourceTypeSummary typeSummary = resourceSummary.findTypeSummary(resourceType);
        AdminDashboardResourceStatVO stat = new AdminDashboardResourceStatVO();
        stat.setResourceType(resourceType);
        stat.setTypeKey(typeKey);
        stat.setTypeName(typeName);
        stat.setCount(typeSummary.count);
        stat.setFileSize(typeSummary.fileSize);
        stat.setPercent(resourceSummary.totalCount == 0 ? 0 : Math.round(typeSummary.count * 100F / resourceSummary.totalCount));
        stat.setTheme(theme);
        stat.setIcon(icon);
        return stat;
    }

    /**
     * 只统计上传成功的资源文件节点，并汇总文件数和存储占用。
     */
    private ResourceSummary loadResourceSummary() {
        ResourceInfoQuery query = new ResourceInfoQuery();
        query.setNodeType(RESOURCE_NODE_RESOURCE);
        query.setStatus(RESOURCE_STATUS_SUCCESS);
        List<ResourceInfo> resourceList = resourceInfoService.findListByParam(query);
        ResourceSummary summary = new ResourceSummary();
        if (resourceList == null) {
            return summary;
        }
        for (ResourceInfo resource : resourceList) {
            Integer resourceType = resource.getResourceType() == null ? 5 : resource.getResourceType();
            Long fileSize = resource.getFileSize() == null ? 0L : resource.getFileSize();
            summary.totalCount++;
            summary.totalFileSize += fileSize;
            ResourceTypeSummary typeSummary = summary.findTypeSummary(resourceType);
            typeSummary.count++;
            typeSummary.fileSize += fileSize;
        }
        summary.storageUsagePercent = Math.min(100, Math.round(summary.totalFileSize * 100F / STORAGE_CAPACITY_BYTES));
        return summary;
    }

    /**
     * 汇总后台首页建议优先处理的教学和公告待办。
     */
    private List<AdminDashboardTodoVO> loadTodoList() {
        List<AdminDashboardTodoVO> todoList = new ArrayList<>();
        LoginUserVO teacher = getCurrentTeacher();
        todoList.add(buildTodo("homework", "作", "待批改作业", countPendingJudge(TASK_TYPE_HOMEWORK, teacher), "份作业需要人工批改", "is-orange", buildPendingHomeworkRoutePath(teacher)));
        todoList.add(buildTodo("exam", "考", "待批改考试", countPendingJudge(TASK_TYPE_EXAM, teacher), "份考试答卷等待处理", "is-purple", ROUTE_EXAM));
        todoList.add(buildTodo("todayExam", "今", "今日考试", countTodayExam(teacher), "场考试安排在今天进行", "is-green", ROUTE_EXAM));
        todoList.add(buildTodo("notice", "告", "公告待发布", countDraftNotice(teacher), "条系统通知待确认发布", "is-blue", ROUTE_NOTICE));
        return todoList;
    }

    private Integer countPendingJudge(Integer taskType, LoginUserVO teacher) {
        CourseAssessmentSubmitQuery query = new CourseAssessmentSubmitQuery();
        query.setTaskType(taskType);
        query.setJudgeStatus(JUDGE_STATUS_MANUAL_PENDING);
        List<CourseAssessmentSubmit> submitList = courseAssessmentSubmitService.findListByParam(query);
        int count = 0;
        for (CourseAssessmentSubmit submit : safeList(submitList)) {
            if (belongsToTeacher(submit, teacher)) {
                count++;
            }
        }
        return count;
    }

    /**
     * 待批改作业入口需要带上具体课程ID，避免作业抽屉缺少课程参数。
     */
    private String buildPendingHomeworkRoutePath(LoginUserVO teacher) {
        CourseAssessmentSubmitQuery query = new CourseAssessmentSubmitQuery();
        query.setTaskType(TASK_TYPE_HOMEWORK);
        query.setJudgeStatus(JUDGE_STATUS_MANUAL_PENDING);
        query.setOrderBy("c.submit_id desc");
        List<CourseAssessmentSubmit> submitList = courseAssessmentSubmitService.findListByParam(query);
        for (CourseAssessmentSubmit submit : safeList(submitList)) {
            CourseChapterLesson lesson = courseChapterLessonService.getCourseChapterLessonByLessonId(submit.getTaskId());
            if (lesson != null && !StringTools.isEmpty(lesson.getCourseId()) && belongsToTeacher(lesson.getCourseId(), teacher)) {
                return ROUTE_HOMEWORK + "?courseId=" + lesson.getCourseId();
            }
        }
        return ROUTE_COURSE;
    }

    private boolean belongsToTeacher(CourseAssessmentSubmit submit, LoginUserVO teacher) {
        if (teacher == null) {
            return true;
        }
        if (Objects.equals(submit.getTaskType(), TASK_TYPE_EXAM)) {
            ExamInfo examInfo = examInfoService.getExamInfoByExamId(submit.getTaskId());
            return examInfo != null && Objects.equals(examInfo.getTeacherId(), teacher.getUserId());
        }
        CourseChapterLesson lesson = courseChapterLessonService.getCourseChapterLessonByLessonId(submit.getTaskId());
        return lesson != null && belongsToTeacher(lesson.getCourseId(), teacher);
    }

    private boolean belongsToTeacher(String courseId, LoginUserVO teacher) {
        if (teacher == null) {
            return true;
        }
        CourseInfo courseInfo = courseInfoService.getCourseInfoByCourseId(courseId);
        return courseInfo != null && Objects.equals(courseInfo.getTeacherId(), teacher.getUserId());
    }

    private Integer countTodayExam(LoginUserVO teacher) {
        LocalDate today = LocalDate.now();
        return countPublishedExams(formatDateTime(today.atStartOfDay()), formatDateTime(today.atTime(LocalTime.MAX)), teacher);
    }

    private Integer countDraftNotice(LoginUserVO teacher) {
        SystemNoticeQuery query = new SystemNoticeQuery();
        query.setStatus(NOTICE_STATUS_DRAFT);
        if (teacher != null) {
            query.setCreateUserId(teacher.getUserId());
        }
        return safeCount(systemNoticeService.findCountByParam(query));
    }

    private AdminDashboardTodoVO buildTodo(String key, String tag, String title, Integer count, String descSuffix, String theme, String routePath) {
        AdminDashboardTodoVO todo = new AdminDashboardTodoVO();
        todo.setKey(key);
        todo.setTag(tag);
        todo.setTitle(title + " " + safeCount(count) + "");
        todo.setDesc(safeCount(count) + descSuffix);
        todo.setCount(safeCount(count));
        todo.setTheme(theme);
        todo.setRoutePath(routePath);
        return todo;
    }

    /**
     * 从课程、资源、考试和公告中取最新记录，统一排序后展示。
     */
    private List<AdminDashboardActivityVO> loadActivityList() {
        List<ActivityRecord> recordList = new ArrayList<>();
        loadLatestCourses(recordList);
        loadLatestResources(recordList);
        loadLatestExams(recordList);
        loadLatestNotices(recordList);
        recordList.sort(Comparator.comparing(ActivityRecord::date, Comparator.nullsLast(Date::compareTo)).reversed());
        List<AdminDashboardActivityVO> activityList = new ArrayList<>();
        for (int index = 0; index < Math.min(LATEST_SIZE, recordList.size()); index++) {
            activityList.add(recordList.get(index).activity);
        }
        return activityList;
    }

    private void loadLatestCourses(List<ActivityRecord> recordList) {
        CourseInfoQuery query = new CourseInfoQuery();
        query.setPageNo(1);
        query.setPageSize(LATEST_SIZE);
        query.setStatus(COURSE_STATUS_ENABLED);
        query.setOrderBy("c.create_time desc");
        PaginationResultVO<CourseInfo> page = courseInfoService.findListByPage(query);
        for (CourseInfo course : safeList(page == null ? null : page.getList())) {
            recordList.add(new ActivityRecord(course.getCreateTime(), buildActivity(course.getCourseId(), course.getCourseName(), "课程已更新", course.getCreateTime(), "is-blue", ROUTE_COURSE)));
        }
    }

    private void loadLatestResources(List<ActivityRecord> recordList) {
        ResourceInfoQuery query = new ResourceInfoQuery();
        query.setPageNo(1);
        query.setPageSize(LATEST_SIZE);
        query.setNodeType(RESOURCE_NODE_RESOURCE);
        query.setStatus(RESOURCE_STATUS_SUCCESS);
        query.setOrderBy("r.create_time desc");
        PaginationResultVO<ResourceInfo> page = resourceInfoService.findListByPage(query);
        for (ResourceInfo resource : safeList(page == null ? null : page.getList())) {
            recordList.add(new ActivityRecord(resource.getCreateTime(), buildActivity(String.valueOf(resource.getResourceId()), resource.getResourceName(), "资源上传成功", resource.getCreateTime(), "is-green", ROUTE_RESOURCE)));
        }
    }

    private void loadLatestExams(List<ActivityRecord> recordList) {
        ExamInfoQuery query = new ExamInfoQuery();
        query.setPageNo(1);
        query.setPageSize(LATEST_SIZE);
        query.setStatus(EXAM_STATUS_PUBLISHED);
        query.setOrderBy("e.create_time desc");
        PaginationResultVO<ExamInfo> page = examInfoService.findListByPage(query);
        for (ExamInfo exam : safeList(page == null ? null : page.getList())) {
            recordList.add(new ActivityRecord(exam.getCreateTime(), buildActivity(exam.getExamId(), exam.getExamName(), "考试已发布", exam.getCreateTime(), "is-purple", ROUTE_EXAM)));
        }
    }

    private void loadLatestNotices(List<ActivityRecord> recordList) {
        SystemNoticeQuery query = new SystemNoticeQuery();
        query.setPageNo(1);
        query.setPageSize(LATEST_SIZE);
        query.setStatus(NOTICE_STATUS_PUBLISHED);
        query.setOrderBy("s.publish_time desc,s.create_time desc");
        PaginationResultVO<SystemNotice> page = systemNoticeService.findListByPage(query);
        for (SystemNotice notice : safeList(page == null ? null : page.getList())) {
            Date activityTime = notice.getPublishTime() == null ? notice.getCreateTime() : notice.getPublishTime();
            recordList.add(new ActivityRecord(activityTime, buildActivity(notice.getNoticeId(), notice.getNoticeTitle(), "系统公告已发布", activityTime, "is-orange", ROUTE_NOTICE)));
        }
    }

    private AdminDashboardActivityVO buildActivity(String id, String title, String desc, Date time, String theme, String routePath) {
        AdminDashboardActivityVO activity = new AdminDashboardActivityVO();
        activity.setId(id);
        activity.setTitle(title);
        activity.setDesc(desc);
        activity.setTime(formatActivityTime(time));
        activity.setTheme(theme);
        activity.setRoutePath(routePath);
        return activity;
    }

    private String formatDateTime(LocalDateTime dateTime) {
        return dateTime.format(java.time.format.DateTimeFormatter.ofPattern(DATE_TIME_PATTERN));
    }

    private String formatDayLabel(LocalDate day) {
        String[] weekNames = {"周一", "周二", "周三", "周四", "周五", "周六", "周日"};
        return weekNames[day.getDayOfWeek().getValue() - 1];
    }

    private String formatActivityTime(Date time) {
        if (time == null) {
            return "--";
        }
        LocalDate activityDay = time.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate today = LocalDate.now();
        if (Objects.equals(activityDay, today)) {
            return DateUtil.format(time, "HH:mm");
        }
        if (Objects.equals(activityDay, today.minusDays(1))) {
            return "昨天";
        }
        return DateUtil.format(time, DateTimePatternEnum.YYYY_MM_DD.getPattern());
    }

    private LoginUserVO getCurrentTeacher() {
        LoginUserVO loginUser = LoginUserContextHolder.get();
        if (loginUser != null && Objects.equals(loginUser.getRoleType(), UserRoleTypeEnum.TEACHER.getCode())) {
            return loginUser;
        }
        return null;
    }

    private Integer safeCount(Integer count) {
        return count == null ? 0 : count;
    }

    private <T> List<T> safeList(List<T> list) {
        return list == null ? new ArrayList<>() : list;
    }

    private static class ResourceSummary {
        private Integer totalCount = 0;
        private Long totalFileSize = 0L;
        private Integer storageUsagePercent = 0;
        private final List<ResourceTypeSummary> typeSummaryList = new ArrayList<>();

        private ResourceTypeSummary findTypeSummary(Integer resourceType) {
            for (ResourceTypeSummary summary : typeSummaryList) {
                if (Objects.equals(summary.resourceType, resourceType)) {
                    return summary;
                }
            }
            ResourceTypeSummary summary = new ResourceTypeSummary(resourceType);
            typeSummaryList.add(summary);
            return summary;
        }
    }

    private static class ResourceTypeSummary {
        private final Integer resourceType;
        private Integer count = 0;
        private Long fileSize = 0L;

        private ResourceTypeSummary(Integer resourceType) {
            this.resourceType = resourceType;
        }
    }

    private record ActivityRecord(Date date, AdminDashboardActivityVO activity) {
    }
}
