package com.smart.campus.web.biz;

import com.smart.campus.web.entity.dto.analysis.LearningAnalysisQueryDTO;
import com.smart.campus.entity.enums.ResponseCodeEnum;
import com.smart.campus.entity.enums.UserRoleTypeEnum;
import com.smart.campus.entity.po.CourseAssessmentSubmit;
import com.smart.campus.entity.po.CourseStudyLog;
import com.smart.campus.entity.po.UserInfo;
import com.smart.campus.entity.query.CourseAssessmentSubmitQuery;
import com.smart.campus.entity.query.CourseStudyLogQuery;
import com.smart.campus.entity.vo.CourseChapterDetailVO;
import com.smart.campus.entity.vo.CourseDetailVO;
import com.smart.campus.entity.vo.CourseExamListItemVO;
import com.smart.campus.entity.vo.CourseLessonDetailVO;
import com.smart.campus.entity.vo.CourseListItemVO;
import com.smart.campus.entity.vo.LoginUserVO;
import com.smart.campus.exception.BusinessException;
import com.smart.campus.service.CourseAssessmentSubmitService;
import com.smart.campus.service.CourseStudyLogService;
import com.smart.campus.service.UserInfoService;
import com.smart.campus.utils.LoginUserContextHolder;
import com.smart.campus.utils.StringTools;
import com.smart.campus.web.entity.vo.analysis.LearningAnalysisAdviceItemVO;
import com.smart.campus.web.entity.vo.analysis.LearningAnalysisBehaviorVO;
import com.smart.campus.web.entity.vo.analysis.LearningAnalysisCourseItemVO;
import com.smart.campus.web.entity.vo.analysis.LearningAnalysisDashboardVO;
import com.smart.campus.web.entity.vo.analysis.LearningAnalysisKnowledgeItemVO;
import com.smart.campus.web.entity.vo.analysis.LearningAnalysisOverviewVO;
import com.smart.campus.web.entity.vo.analysis.LearningAnalysisReportVO;
import com.smart.campus.web.entity.vo.analysis.LearningAnalysisTimePreferenceItemVO;
import com.smart.campus.web.entity.vo.analysis.LearningAnalysisTrendItemVO;
import com.smart.campus.web.entity.vo.studyplan.StudyPlanDashboardVO;
import com.smart.campus.web.entity.vo.studyplan.StudyPlanScheduleItemVO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class LearningAnalysisWebBiz {

    private static final int USER_ROLE_STUDENT = UserRoleTypeEnum.STUDENT.getCode();
    private static final int TASK_TYPE_EXAM = 2;
    private static final int EXAM_SUBMIT_STATUS_SUBMITTED = 3;
    private static final int PLAN_STATUS_COMPLETED = 2;
    private static final int DEFAULT_RANGE_DAYS = 7;
    private static final ZoneId ZONE_ID = ZoneId.of("Asia/Shanghai");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter MONTH_DAY_FORMATTER = DateTimeFormatter.ofPattern("MM-dd");

    @Resource
    private CourseWebBiz courseWebBiz;

    @Resource
    private StudyPlanWebBiz studyPlanWebBiz;

    @Resource
    private CourseExamWebBiz courseExamWebBiz;

    @Resource
    private CourseAssessmentSubmitService courseAssessmentSubmitService;

    @Resource
    private CourseStudyLogService courseStudyLogService;

    @Resource
    private UserInfoService userInfoService;

    public LearningAnalysisDashboardVO loadDashboard(LearningAnalysisQueryDTO dto) {
        UserInfo currentStudent = getCurrentStudent();
        DateRange dateRange = resolveDateRange(dto);

        List<CourseListItemVO> courseList = courseWebBiz.loadMyCourseList();
        List<CourseDetailVO> courseDetailList = new ArrayList<>();
        for (CourseListItemVO item : courseList) {
            if (StringTools.isEmpty(item.getCourseId())) {
                continue;
            }
            courseDetailList.add(courseWebBiz.getMyCourseDetail(item.getCourseId()));
        }

        StudyPlanDashboardVO planDashboard = studyPlanWebBiz.loadDashboard();
        List<CourseExamListItemVO> examList = courseExamWebBiz.loadMyExamList();
        Map<String, CourseAssessmentSubmit> examSubmitMap = loadExamSubmitMap(currentStudent.getUserId());
        List<CourseStudyLog> rangedStudyLogList = loadStudyLogList(currentStudent.getUserId(), dateRange);
        List<CourseStudyLog> previousStudyLogList = loadStudyLogList(currentStudent.getUserId(), dateRange.previous());

        List<LessonSnapshot> lessonList = buildLessonSnapshotList(courseDetailList);
        List<StudyPlanScheduleItemVO> rangedPlanList = (planDashboard.getCalendarPlanList() == null ? List.<StudyPlanScheduleItemVO>of() : planDashboard.getCalendarPlanList())
                .stream()
                .filter(item -> isWithinRange(item.getStudyDate(), dateRange))
                .toList();
        List<CourseExamListItemVO> submittedExamList = examList.stream()
                .filter(item -> Boolean.TRUE.equals(item.getSubmitted()))
                .toList();
        List<CourseExamListItemVO> rangedExamList = submittedExamList.stream()
                .filter(item -> isExamWithinRange(item, examSubmitMap.get(item.getExamId()), dateRange))
                .toList();

        List<LearningAnalysisCourseItemVO> courseItemList = buildCourseItemList(courseList, courseDetailList);
        List<LearningAnalysisCourseItemVO> courseDistributionList = buildCourseDistributionList(courseItemList);
        List<LearningAnalysisKnowledgeItemVO> knowledgeList = buildKnowledgeList(courseDetailList);
        List<LearningAnalysisTrendItemVO> trendList = buildTrendList(dateRange, rangedStudyLogList);
        LearningAnalysisOverviewVO overview = buildOverview(
                rangedStudyLogList,
                previousStudyLogList,
                rangedPlanList,
                courseItemList,
                rangedExamList,
                planDashboard
        );
        LearningAnalysisBehaviorVO behavior = buildBehavior(rangedPlanList, rangedStudyLogList, rangedExamList, planDashboard);
        List<LearningAnalysisTimePreferenceItemVO> timePreferenceList = buildTimePreferenceList(rangedStudyLogList);
        LearningAnalysisReportVO report = buildReport(overview, behavior, courseDistributionList, knowledgeList, timePreferenceList);

        LearningAnalysisDashboardVO vo = new LearningAnalysisDashboardVO();
        vo.setStartDate(dateRange.getStartText());
        vo.setEndDate(dateRange.getEndText());
        vo.setTrendDataRemark("按学习流水表 course_study_log 统计每日学习时长与学习时段分布。");
        vo.setKnowledgeDataRemark("当前以课程章节作为分析口径，暂未拆分到独立知识点。");
        vo.setOverview(overview);
        vo.setBehavior(behavior);
        vo.setReport(report);
        vo.setTrendList(trendList);
        vo.setDailyStudyList(trendList);
        vo.setCourseList(courseItemList);
        vo.setCourseDistributionList(courseDistributionList);
        vo.setKnowledgeList(knowledgeList);
        vo.setTimePreferenceList(timePreferenceList);
        return vo;
    }

    private LearningAnalysisOverviewVO buildOverview(List<CourseStudyLog> rangedStudyLogList,
                                                     List<CourseStudyLog> previousStudyLogList,
                                                     List<StudyPlanScheduleItemVO> rangedPlanList,
                                                     List<LearningAnalysisCourseItemVO> courseItemList,
                                                     List<CourseExamListItemVO> rangedExamList,
                                                     StudyPlanDashboardVO planDashboard) {
        LearningAnalysisOverviewVO overview = new LearningAnalysisOverviewVO();
        BigDecimal totalStudyHours = sumStudyLogHours(rangedStudyLogList);
        BigDecimal previousStudyHours = sumStudyLogHours(previousStudyLogList);
        overview.setTotalStudyHours(totalStudyHours);
        overview.setPreviousStudyHours(previousStudyHours);
        overview.setHoursGrowthRate(calculateGrowthRate(totalStudyHours, previousStudyHours));
        overview.setTotalTaskCount(rangedPlanList.size());
        overview.setCompletedTaskCount((int) rangedPlanList.stream()
                .filter(item -> Boolean.TRUE.equals(item.getCompleted()) || Objects.equals(item.getStatus(), PLAN_STATUS_COMPLETED))
                .count());
        overview.setCourseCount(courseItemList.size());
        overview.setInProgressCourseCount((int) courseItemList.stream()
                .filter(item -> safeInt(item.getProgress()) > 0 && safeInt(item.getProgress()) < 100)
                .count());
        overview.setAverageCourseProgress(calculateAverageCourseProgress(courseItemList));
        overview.setAverageScore(calculateAverageScore(rangedExamList));
        overview.setCompletedExamCount(rangedExamList.size());
        overview.setTotalPlanCount(safeInt(planDashboard.getTotalPlanCount()));
        overview.setInProgressPlanCount(safeInt(planDashboard.getInProgressPlanCount()));
        return overview;
    }

    private LearningAnalysisBehaviorVO buildBehavior(List<StudyPlanScheduleItemVO> rangedPlanList,
                                                     List<CourseStudyLog> rangedStudyLogList,
                                                     List<CourseExamListItemVO> rangedExamList,
                                                     StudyPlanDashboardVO planDashboard) {
        LearningAnalysisBehaviorVO behavior = new LearningAnalysisBehaviorVO();
        int totalTaskCount = rangedPlanList.size();
        int completedTaskCount = (int) rangedPlanList.stream()
                .filter(item -> Boolean.TRUE.equals(item.getCompleted()) || Objects.equals(item.getStatus(), PLAN_STATUS_COMPLETED))
                .count();
        behavior.setTotalTaskCount(totalTaskCount);
        behavior.setCompletedTaskCount(completedTaskCount);
        behavior.setTaskCompletionRate(totalTaskCount <= 0 ? 0 : Math.round(completedTaskCount * 100F / totalTaskCount));
        behavior.setActiveDays((int) rangedStudyLogList.stream()
                .map(CourseStudyLog::getStartTime)
                .filter(Objects::nonNull)
                .map(this::toLocalDate)
                .distinct()
                .count());
        behavior.setCompletedExamCount(rangedExamList.size());
        behavior.setTotalPlanCount(safeInt(planDashboard.getTotalPlanCount()));
        return behavior;
    }

    private LearningAnalysisReportVO buildReport(LearningAnalysisOverviewVO overview,
                                                 LearningAnalysisBehaviorVO behavior,
                                                 List<LearningAnalysisCourseItemVO> courseDistributionList,
                                                 List<LearningAnalysisKnowledgeItemVO> knowledgeList,
                                                 List<LearningAnalysisTimePreferenceItemVO> timePreferenceList) {
        LearningAnalysisReportVO report = new LearningAnalysisReportVO();
        BigDecimal averageScore = defaultDecimal(overview.getAverageScore());
        int knowledgeAverage = knowledgeList.isEmpty()
                ? 72
                : Math.round((float) knowledgeList.stream().mapToInt(item -> safeInt(item.getMastery())).sum() / knowledgeList.size());
        int reportScore = Math.round(
                safeInt(behavior.getTaskCompletionRate()) * 0.35F
                        + Math.min(100, defaultDecimal(overview.getTotalStudyHours()).multiply(BigDecimal.valueOf(8)).intValue()) * 0.2F
                        + Math.min(100, averageScore.intValue()) * 0.25F
                        + Math.min(100, knowledgeAverage) * 0.2F
        );
        report.setScore(reportScore);

        String topCourseName = courseDistributionList.isEmpty() ? "当前课程" : defaultString(courseDistributionList.get(0).getCourseName());
        long excellentKnowledgeCount = knowledgeList.stream().filter(item -> safeInt(item.getMastery()) >= 85).count();
        report.setSummary(courseDistributionList.isEmpty()
                ? "当前还没有足够的课程学习数据，建议先完成课程学习和计划安排后再查看分析报告。"
                : String.format(
                "本周期你的学习重心主要集中在%s，任务完成率为%d%%，共有%d个章节达到优秀掌握水平，整体学习节奏较稳定。",
                topCourseName,
                safeInt(behavior.getTaskCompletionRate()),
                excellentKnowledgeCount
        ));

        List<String> tags = new ArrayList<>();
        if (safeInt(behavior.getTaskCompletionRate()) >= 70) {
            tags.add("执行力稳定");
        } else {
            tags.add("计划待加强");
        }
        if (averageScore.compareTo(BigDecimal.valueOf(85)) >= 0) {
            tags.add("考试表现优秀");
        } else if (averageScore.compareTo(BigDecimal.ZERO) > 0) {
            tags.add("成绩仍有提升空间");
        }
        LearningAnalysisTimePreferenceItemVO topTimePreference = timePreferenceList.stream()
                .max(Comparator.comparingInt(item -> safeInt(item.getValue())))
                .orElse(null);
        if (topTimePreference != null && safeInt(topTimePreference.getValue()) > 0) {
            tags.add(topTimePreference.getLabel() + "学习更高效");
        }
        if (knowledgeList.stream().anyMatch(item -> safeInt(item.getMastery()) < 60)) {
            tags.add("存在薄弱章节");
        }
        report.setTags(tags);

        List<LearningAnalysisAdviceItemVO> adviceList = new ArrayList<>();
        adviceList.add(buildAdvice("01", "固定每周复盘节奏",
                String.format("建议在每周末用 20 分钟回顾本周期完成的 %d 个任务，并补齐未完成计划。", safeInt(behavior.getCompletedTaskCount()))));
        List<String> weakestList = knowledgeList.stream()
                .sorted(Comparator.comparingInt(item -> safeInt(item.getMastery())))
                .limit(2)
                .map(LearningAnalysisKnowledgeItemVO::getName)
                .filter(item -> !StringTools.isEmpty(item))
                .toList();
        adviceList.add(buildAdvice("02", "优先巩固薄弱章节", weakestList.isEmpty()
                ? "当前章节掌握较均衡，可以继续推进进阶内容学习。"
                : "当前建议优先复习 " + String.join("、", weakestList) + "，先补基础再推进新内容。"));
        String preferredTime = topTimePreference == null ? "晚上" : defaultString(topTimePreference.getLabel());
        adviceList.add(buildAdvice("03", "优化学习时间分配",
                "你在" + preferredTime + "学习更集中，建议把需要深度思考的章节安排在这个时段。"));
        report.setAdviceList(adviceList);
        return report;
    }

    private List<LearningAnalysisTimePreferenceItemVO> buildTimePreferenceList(List<CourseStudyLog> rangedStudyLogList) {
        int morning = 0;
        int afternoon = 0;
        int evening = 0;
        for (CourseStudyLog item : rangedStudyLogList) {
            if (item.getStartTime() == null) {
                continue;
            }
            int hour = toLocalDateTime(item.getStartTime()).getHour();
            int weight = Math.max(1, safeInt(item.getStudySeconds()));
            if (hour >= 6 && hour < 12) {
                morning += weight;
            } else if (hour >= 12 && hour < 18) {
                afternoon += weight;
            } else if (hour >= 18 && hour < 24) {
                evening += weight;
            }
        }
        int total = morning + afternoon + evening;
        return List.of(
                buildTimePreferenceItem("上午", morning, total),
                buildTimePreferenceItem("下午", afternoon, total),
                buildTimePreferenceItem("晚上", evening, total)
        );
    }

    private LearningAnalysisTimePreferenceItemVO buildTimePreferenceItem(String label, int part, int total) {
        LearningAnalysisTimePreferenceItemVO item = new LearningAnalysisTimePreferenceItemVO();
        item.setLabel(label);
        item.setValue(total <= 0 ? 0 : Math.round(part * 100F / total));
        return item;
    }

    private List<LearningAnalysisCourseItemVO> buildCourseDistributionList(List<LearningAnalysisCourseItemVO> courseItemList) {
        BigDecimal totalStudyHours = courseItemList.stream()
                .map(LearningAnalysisCourseItemVO::getStudyHours)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return courseItemList.stream()
                .sorted(Comparator.comparing(LearningAnalysisCourseItemVO::getStudyHours, Comparator.nullsLast(Comparator.reverseOrder())))
                .peek(item -> {
                    if (totalStudyHours.compareTo(BigDecimal.ZERO) <= 0) {
                        item.setPercent(0);
                    } else {
                        item.setPercent(defaultDecimal(item.getStudyHours())
                                .multiply(BigDecimal.valueOf(100))
                                .divide(totalStudyHours, 0, RoundingMode.HALF_UP)
                                .intValue());
                    }
                })
                .toList();
    }

    private List<LearningAnalysisCourseItemVO> buildCourseItemList(List<CourseListItemVO> courseList,
                                                                   List<CourseDetailVO> courseDetailList) {
        Map<String, CourseDetailVO> detailMap = courseDetailList.stream()
                .filter(item -> !StringTools.isEmpty(item.getCourseId()))
                .collect(Collectors.toMap(CourseDetailVO::getCourseId, item -> item, (left, right) -> left));
        List<LearningAnalysisCourseItemVO> result = new ArrayList<>();
        for (CourseListItemVO course : courseList) {
            CourseDetailVO detail = detailMap.get(course.getCourseId());
            List<CourseLessonDetailVO> lessonList = flattenLessonList(detail);
            int lessonCount = lessonList.size();
            int completedLessonCount = (int) lessonList.stream()
                    .filter(item -> Objects.equals(item.getIsCompleted(), 1))
                    .count();
            int progress = calculateCourseProgress(lessonList);

            LearningAnalysisCourseItemVO item = new LearningAnalysisCourseItemVO();
            item.setCourseId(course.getCourseId());
            item.setCourseName(course.getCourseName());
            item.setTeacherName(course.getTeacherName());
            item.setCoverPath(course.getCoverPath());
            item.setChapterCount(detail == null ? safeInt(course.getChapterCount()) : detail.getChapterList().size());
            item.setLessonCount(lessonCount <= 0 ? safeInt(course.getLessonCount()) : lessonCount);
            item.setCompletedLessonCount(completedLessonCount);
            item.setProgress(progress);
            item.setStudyHours(secondsToHours(detail == null ? 0 : safeInt(detail.getStudySeconds())));
            item.setChapterText(String.format("已完成 %d / %d 课时", completedLessonCount, item.getLessonCount()));
            item.setStructureText(String.format("%d 章 / %d 课时", item.getChapterCount(), item.getLessonCount()));
            item.setStatusText(resolveCourseStatusText(progress));
            item.setLastStudyTime(detail == null ? null : detail.getLastStudyTime());
            item.setLastStudyText(detail == null || detail.getLastStudyTime() == null
                    ? "最近暂无学习记录"
                    : DATE_FORMATTER.format(toLocalDate(detail.getLastStudyTime())) + " 学习");
            result.add(item);
        }
        return result;
    }

    private List<LearningAnalysisKnowledgeItemVO> buildKnowledgeList(List<CourseDetailVO> courseDetailList) {
        List<LearningAnalysisKnowledgeItemVO> result = new ArrayList<>();
        for (CourseDetailVO courseDetail : courseDetailList) {
            for (CourseChapterDetailVO chapter : courseDetail.getChapterList()) {
                int mastery = calculateChapterMastery(chapter.getLessonList());
                LearningAnalysisKnowledgeItemVO item = new LearningAnalysisKnowledgeItemVO();
                item.setKey(defaultString(courseDetail.getCourseId()) + "-" + defaultString(chapter.getChapterId()));
                item.setCourseId(courseDetail.getCourseId());
                item.setCourseName(courseDetail.getCourseName());
                item.setChapterId(chapter.getChapterId());
                item.setName(chapter.getChapterName());
                item.setMastery(mastery);
                item.setLevelText(resolveKnowledgeLevelText(mastery));
                item.setLevelTheme(resolveKnowledgeLevelTheme(mastery));
                result.add(item);
            }
        }
        result.sort(Comparator.comparingInt(LearningAnalysisKnowledgeItemVO::getMastery).reversed());
        return result;
    }

    private List<LearningAnalysisTrendItemVO> buildTrendList(DateRange dateRange, List<CourseStudyLog> rangedStudyLogList) {
        Map<LocalDate, BigDecimal> dateValueMap = new LinkedHashMap<>();
        LocalDate cursor = dateRange.getStart();
        while (!cursor.isAfter(dateRange.getEnd())) {
            dateValueMap.put(cursor, BigDecimal.ZERO.setScale(1, RoundingMode.HALF_UP));
            cursor = cursor.plusDays(1);
        }
        for (CourseStudyLog item : rangedStudyLogList) {
            if (item.getStartTime() == null) {
                continue;
            }
            LocalDate date = toLocalDate(item.getStartTime());
            if (!dateValueMap.containsKey(date)) {
                continue;
            }
            dateValueMap.put(date, dateValueMap.get(date).add(secondsToHours(item.getStudySeconds())));
        }
        List<LearningAnalysisTrendItemVO> result = new ArrayList<>();
        for (Map.Entry<LocalDate, BigDecimal> entry : dateValueMap.entrySet()) {
            LearningAnalysisTrendItemVO item = new LearningAnalysisTrendItemVO();
            item.setDate(DATE_FORMATTER.format(entry.getKey()));
            item.setLabel(MONTH_DAY_FORMATTER.format(entry.getKey()));
            item.setValue(scale(entry.getValue()));
            result.add(item);
        }
        return result;
    }

    private Map<String, CourseAssessmentSubmit> loadExamSubmitMap(Integer userId) {
        CourseAssessmentSubmitQuery query = new CourseAssessmentSubmitQuery();
        query.setUserId(userId);
        query.setTaskType(TASK_TYPE_EXAM);
        List<CourseAssessmentSubmit> submitList = courseAssessmentSubmitService.findListByParam(query);
        return submitList.stream()
                .filter(item -> !StringTools.isEmpty(item.getTaskId()))
                .collect(Collectors.toMap(CourseAssessmentSubmit::getTaskId, item -> item, this::pickLatestSubmit));
    }

    private CourseAssessmentSubmit pickLatestSubmit(CourseAssessmentSubmit left, CourseAssessmentSubmit right) {
        Date leftTime = left.getSubmitTime() == null ? left.getUpdateTime() : left.getSubmitTime();
        Date rightTime = right.getSubmitTime() == null ? right.getUpdateTime() : right.getSubmitTime();
        if (leftTime == null) {
            return right;
        }
        if (rightTime == null) {
            return left;
        }
        return leftTime.after(rightTime) ? left : right;
    }

    private List<LessonSnapshot> buildLessonSnapshotList(List<CourseDetailVO> courseDetailList) {
        List<LessonSnapshot> result = new ArrayList<>();
        for (CourseDetailVO courseDetail : courseDetailList) {
            for (CourseChapterDetailVO chapter : courseDetail.getChapterList()) {
                for (CourseLessonDetailVO lesson : chapter.getLessonList()) {
                    LessonSnapshot item = new LessonSnapshot();
                    item.setCourseId(courseDetail.getCourseId());
                    item.setCourseName(courseDetail.getCourseName());
                    item.setChapterId(chapter.getChapterId());
                    item.setChapterName(chapter.getChapterName());
                    item.setLessonId(lesson.getLessonId());
                    item.setLessonName(lesson.getLessonName());
                    item.setStudySeconds(safeInt(lesson.getStudySeconds()));
                    item.setLastStudyTime(lesson.getLastStudyTime());
                    item.setMaxPositionSeconds(safeInt(lesson.getMaxPositionSeconds()));
                    item.setVideoDurationSeconds(safeInt(lesson.getVideoDurationSeconds()));
                    item.setCompleted(Objects.equals(lesson.getIsCompleted(), 1));
                    result.add(item);
                }
            }
        }
        return result;
    }

    private List<CourseLessonDetailVO> flattenLessonList(CourseDetailVO detail) {
        if (detail == null || detail.getChapterList() == null) {
            return List.of();
        }
        return detail.getChapterList().stream()
                .map(CourseChapterDetailVO::getLessonList)
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .toList();
    }

    private int calculateCourseProgress(List<CourseLessonDetailVO> lessonList) {
        if (lessonList == null || lessonList.isEmpty()) {
            return 0;
        }
        double ratioSum = 0D;
        for (CourseLessonDetailVO lesson : lessonList) {
            ratioSum += resolveLessonProgressRatio(
                    Objects.equals(lesson.getIsCompleted(), 1),
                    safeInt(lesson.getMaxPositionSeconds()),
                    safeInt(lesson.getVideoDurationSeconds()),
                    safeInt(lesson.getStudySeconds())
            );
        }
        return Math.min(100, Math.max(0, (int) Math.round(ratioSum * 100 / lessonList.size())));
    }

    private int calculateChapterMastery(List<CourseLessonDetailVO> lessonList) {
        if (lessonList == null || lessonList.isEmpty()) {
            return 0;
        }
        double ratioSum = 0D;
        for (CourseLessonDetailVO lesson : lessonList) {
            ratioSum += resolveLessonProgressRatio(
                    Objects.equals(lesson.getIsCompleted(), 1),
                    safeInt(lesson.getMaxPositionSeconds()),
                    safeInt(lesson.getVideoDurationSeconds()),
                    safeInt(lesson.getStudySeconds())
            );
        }
        return Math.min(100, Math.max(0, (int) Math.round(ratioSum * 100 / lessonList.size())));
    }

    private double resolveLessonProgressRatio(boolean completed,
                                              int maxPositionSeconds,
                                              int videoDurationSeconds,
                                              int studySeconds) {
        if (completed) {
            return 1D;
        }
        if (videoDurationSeconds > 0) {
            return Math.min(0.98D, Math.max(0D, (double) maxPositionSeconds / videoDurationSeconds));
        }
        if (studySeconds > 0) {
            return 0.35D;
        }
        return 0D;
    }

    private String resolveCourseStatusText(int progress) {
        if (progress >= 85) {
            return "掌握稳定";
        }
        if (progress >= 60) {
            return "持续提升";
        }
        if (progress > 0) {
            return "建议加强";
        }
        return "尚未开始";
    }

    private String resolveKnowledgeLevelText(int mastery) {
        if (mastery >= 85) {
            return "掌握优秀";
        }
        if (mastery >= 65) {
            return "掌握良好";
        }
        return "需巩固";
    }

    private String resolveKnowledgeLevelTheme(int mastery) {
        if (mastery >= 85) {
            return "is-high";
        }
        if (mastery >= 65) {
            return "is-middle";
        }
        return "is-low";
    }

    private LearningAnalysisAdviceItemVO buildAdvice(String index, String title, String desc) {
        LearningAnalysisAdviceItemVO item = new LearningAnalysisAdviceItemVO();
        item.setIndex(index);
        item.setTitle(title);
        item.setDesc(desc);
        return item;
    }

    private LearningAnalysisTimePreferenceItemVO buildEmptyTimePreference(String label) {
        LearningAnalysisTimePreferenceItemVO item = new LearningAnalysisTimePreferenceItemVO();
        item.setLabel(label);
        item.setValue(0);
        return item;
    }

    private BigDecimal calculateAverageScore(List<CourseExamListItemVO> examList) {
        if (examList == null || examList.isEmpty()) {
            return BigDecimal.ZERO.setScale(1, RoundingMode.HALF_UP);
        }
        BigDecimal total = examList.stream()
                .map(CourseExamListItemVO::getFinalScore)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return total.divide(BigDecimal.valueOf(examList.size()), 1, RoundingMode.HALF_UP);
    }

    private int calculateAverageCourseProgress(List<LearningAnalysisCourseItemVO> courseItemList) {
        if (courseItemList == null || courseItemList.isEmpty()) {
            return 0;
        }
        int totalProgress = courseItemList.stream()
                .mapToInt(item -> safeInt(item.getProgress()))
                .sum();
        return Math.round((float) totalProgress / courseItemList.size());
    }

    private int calculateGrowthRate(BigDecimal current, BigDecimal previous) {
        BigDecimal safeCurrent = defaultDecimal(current);
        BigDecimal safePrevious = defaultDecimal(previous);
        if (safePrevious.compareTo(BigDecimal.ZERO) <= 0) {
            return safeCurrent.compareTo(BigDecimal.ZERO) > 0 ? 100 : 0;
        }
        return safeCurrent.subtract(safePrevious)
                .multiply(BigDecimal.valueOf(100))
                .divide(safePrevious, 0, RoundingMode.HALF_UP)
                .intValue();
    }

    private BigDecimal sumStudyLogHours(List<CourseStudyLog> logList) {
        return scale(logList.stream()
                .map(item -> secondsToHours(item.getStudySeconds()))
                .reduce(BigDecimal.ZERO, BigDecimal::add));
    }

    private List<CourseStudyLog> loadStudyLogList(Integer userId, DateRange dateRange) {
        if (userId == null || dateRange == null) {
            return List.of();
        }
        CourseStudyLogQuery query = new CourseStudyLogQuery();
        query.setUserId(userId);
        query.setStartTimeStart(dateRange.getStartText() + " 00:00:00");
        query.setStartTimeEnd(dateRange.getEndText() + " 23:59:59");
        query.setOrderBy("c.start_time asc,c.id asc");
        return courseStudyLogService.findListByParam(query);
    }

    private BigDecimal secondsToHours(Integer seconds) {
        return BigDecimal.valueOf(Math.max(0, seconds == null ? 0 : seconds))
                .divide(BigDecimal.valueOf(3600), 1, RoundingMode.HALF_UP);
    }

    private BigDecimal scale(BigDecimal value) {
        return defaultDecimal(value).setScale(1, RoundingMode.HALF_UP);
    }

    private BigDecimal defaultDecimal(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private boolean isExamWithinRange(CourseExamListItemVO exam, CourseAssessmentSubmit submit, DateRange dateRange) {
        if (exam == null || submit == null || !Objects.equals(submit.getSubmitStatus(), EXAM_SUBMIT_STATUS_SUBMITTED)) {
            return false;
        }
        Date referenceTime = submit.getSubmitTime() == null ? exam.getEndTime() : submit.getSubmitTime();
        return isWithinRange(referenceTime, dateRange);
    }

    private boolean isWithinRange(Date date, DateRange dateRange) {
        if (date == null || dateRange == null) {
            return false;
        }
        LocalDate localDate = toLocalDate(date);
        return !localDate.isBefore(dateRange.getStart()) && !localDate.isAfter(dateRange.getEnd());
    }

    private boolean isWithinRange(String dateText, DateRange dateRange) {
        if (StringTools.isEmpty(dateText) || dateRange == null) {
            return false;
        }
        LocalDate localDate = LocalDate.parse(dateText, DATE_FORMATTER);
        return !localDate.isBefore(dateRange.getStart()) && !localDate.isAfter(dateRange.getEnd());
    }

    private LocalDate toLocalDate(Date date) {
        return Instant.ofEpochMilli(date.getTime()).atZone(ZONE_ID).toLocalDate();
    }

    private java.time.LocalDateTime toLocalDateTime(Date date) {
        return Instant.ofEpochMilli(date.getTime()).atZone(ZONE_ID).toLocalDateTime();
    }

    private DateRange resolveDateRange(LearningAnalysisQueryDTO dto) {
        LocalDate end = LocalDate.now(ZONE_ID);
        LocalDate start = end.minusDays(DEFAULT_RANGE_DAYS - 1L);
        if (dto != null && !StringTools.isEmpty(dto.getStartDate()) && !StringTools.isEmpty(dto.getEndDate())) {
            try {
                start = LocalDate.parse(StringTools.trim(dto.getStartDate()), DATE_FORMATTER);
                end = LocalDate.parse(StringTools.trim(dto.getEndDate()), DATE_FORMATTER);
            } catch (Exception exception) {
                throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "日期格式不正确");
            }
            if (end.isBefore(start)) {
                throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "结束日期不能早于开始日期");
            }
        }
        if (ChronoUnit.DAYS.between(start, end) > 60) {
            throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "分析时间范围不能超过 60 天");
        }
        return new DateRange(start, end);
    }

    private UserInfo getCurrentStudent() {
        LoginUserVO loginUser = LoginUserContextHolder.get();
        if (loginUser == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_901);
        }
        if (!Objects.equals(loginUser.getRoleType(), USER_ROLE_STUDENT)) {
            throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "请使用学生账号查看学习分析");
        }
        UserInfo currentStudent = userInfoService.getUserInfoByUserId(loginUser.getUserId());
        if (currentStudent == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "学生信息不存在");
        }
        return currentStudent;
    }

    private int safeInt(Integer value) {
        return value == null ? 0 : Math.max(0, value);
    }

    private String defaultString(String value) {
        return value == null ? "" : value;
    }

    private static class LessonSnapshot {
        private String courseId;
        private String courseName;
        private String chapterId;
        private String chapterName;
        private String lessonId;
        private String lessonName;
        private Integer studySeconds;
        private Date lastStudyTime;
        private Integer maxPositionSeconds;
        private Integer videoDurationSeconds;
        private boolean completed;

        public String getCourseId() {
            return courseId;
        }

        public void setCourseId(String courseId) {
            this.courseId = courseId;
        }

        public String getCourseName() {
            return courseName;
        }

        public void setCourseName(String courseName) {
            this.courseName = courseName;
        }

        public String getChapterId() {
            return chapterId;
        }

        public void setChapterId(String chapterId) {
            this.chapterId = chapterId;
        }

        public String getChapterName() {
            return chapterName;
        }

        public void setChapterName(String chapterName) {
            this.chapterName = chapterName;
        }

        public String getLessonId() {
            return lessonId;
        }

        public void setLessonId(String lessonId) {
            this.lessonId = lessonId;
        }

        public String getLessonName() {
            return lessonName;
        }

        public void setLessonName(String lessonName) {
            this.lessonName = lessonName;
        }

        public Integer getStudySeconds() {
            return studySeconds;
        }

        public void setStudySeconds(Integer studySeconds) {
            this.studySeconds = studySeconds;
        }

        public Date getLastStudyTime() {
            return lastStudyTime;
        }

        public void setLastStudyTime(Date lastStudyTime) {
            this.lastStudyTime = lastStudyTime;
        }

        public Integer getMaxPositionSeconds() {
            return maxPositionSeconds;
        }

        public void setMaxPositionSeconds(Integer maxPositionSeconds) {
            this.maxPositionSeconds = maxPositionSeconds;
        }

        public Integer getVideoDurationSeconds() {
            return videoDurationSeconds;
        }

        public void setVideoDurationSeconds(Integer videoDurationSeconds) {
            this.videoDurationSeconds = videoDurationSeconds;
        }

        public boolean isCompleted() {
            return completed;
        }

        public void setCompleted(boolean completed) {
            this.completed = completed;
        }
    }

    private static class DateRange {
        private final LocalDate start;
        private final LocalDate end;

        private DateRange(LocalDate start, LocalDate end) {
            this.start = start;
            this.end = end;
        }

        public LocalDate getStart() {
            return start;
        }

        public LocalDate getEnd() {
            return end;
        }

        public String getStartText() {
            return DATE_FORMATTER.format(start);
        }

        public String getEndText() {
            return DATE_FORMATTER.format(end);
        }

        public DateRange previous() {
            long days = ChronoUnit.DAYS.between(start, end) + 1;
            LocalDate previousEnd = start.minusDays(1);
            LocalDate previousStart = previousEnd.minusDays(days - 1);
            return new DateRange(previousStart, previousEnd);
        }
    }
}
