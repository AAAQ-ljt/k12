package com.smart.campus.web.biz;

import com.smart.campus.web.entity.dto.studyplan.StudyPlanItemStatusDTO;
import com.smart.campus.web.entity.dto.studyplan.StudyPlanSaveDTO;
import com.smart.campus.web.entity.dto.studyplan.StudyPlanSaveItemDTO;
import com.smart.campus.entity.enums.ResponseCodeEnum;
import com.smart.campus.entity.enums.UserRoleTypeEnum;
import com.smart.campus.entity.po.CourseChapter;
import com.smart.campus.entity.po.CourseChapterLesson;
import com.smart.campus.entity.po.CourseClass;
import com.smart.campus.entity.po.CourseInfo;
import com.smart.campus.entity.po.CourseStudyProgress;
import com.smart.campus.entity.po.ResourceInfo;
import com.smart.campus.entity.po.StudyPlan;
import com.smart.campus.entity.po.StudyPlanItem;
import com.smart.campus.entity.po.UserInfo;
import com.smart.campus.entity.query.CourseChapterLessonQuery;
import com.smart.campus.entity.query.CourseChapterQuery;
import com.smart.campus.entity.query.CourseClassQuery;
import com.smart.campus.entity.query.CourseInfoQuery;
import com.smart.campus.entity.query.CourseStudyProgressQuery;
import com.smart.campus.entity.query.StudyPlanItemQuery;
import com.smart.campus.entity.query.StudyPlanQuery;
import com.smart.campus.entity.vo.LoginUserVO;
import com.smart.campus.exception.BusinessException;
import com.smart.campus.mappers.StudyPlanItemMapper;
import com.smart.campus.service.CourseChapterLessonService;
import com.smart.campus.service.CourseChapterService;
import com.smart.campus.service.CourseClassService;
import com.smart.campus.service.CourseInfoService;
import com.smart.campus.service.CourseStudyProgressService;
import com.smart.campus.service.ResourceInfoService;
import com.smart.campus.service.StudyPlanItemService;
import com.smart.campus.service.StudyPlanService;
import com.smart.campus.service.UserInfoService;
import com.smart.campus.utils.DateUtil;
import com.smart.campus.utils.LoginUserContextHolder;
import com.smart.campus.utils.StringTools;
import com.smart.campus.web.entity.vo.studyplan.StudyPlanDashboardVO;
import com.smart.campus.web.entity.vo.studyplan.StudyPlanDetailItemVO;
import com.smart.campus.web.entity.vo.studyplan.StudyPlanDetailVO;
import com.smart.campus.web.entity.vo.studyplan.StudyPlanListItemVO;
import com.smart.campus.web.entity.vo.studyplan.StudyPlanScheduleItemVO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class StudyPlanWebBiz {

    private static final int USER_ROLE_STUDENT = UserRoleTypeEnum.STUDENT.getCode();
    private static final int STATUS_PENDING = 0;
    private static final int STATUS_RUNNING = 1;
    private static final int STATUS_COMPLETED = 2;
    private static final int TIME_MULTIPLIER = 10000;
    private static final String DATE_PATTERN = "yyyy-MM-dd";
    private static final String CHAPTER_ORDER_BY = "c.sort_order asc,c.chapter_id asc";
    private static final String LESSON_ORDER_BY = "c.sort_order asc,c.lesson_id asc";

    @Resource
    private UserInfoService userInfoService;
    @Resource
    private CourseInfoService courseInfoService;
    @Resource
    private CourseClassService courseClassService;
    @Resource
    private CourseChapterService courseChapterService;
    @Resource
    private CourseChapterLessonService courseChapterLessonService;
    @Resource
    private ResourceInfoService resourceInfoService;
    @Resource
    private CourseStudyProgressService courseStudyProgressService;
    @Resource
    private StudyPlanService studyPlanService;
    @Resource
    private StudyPlanItemService studyPlanItemService;
    @Resource
    private StudyPlanItemMapper<StudyPlanItem, StudyPlanItemQuery> studyPlanItemMapper;

    public StudyPlanDashboardVO loadDashboard() {
        UserInfo currentStudent = getCurrentStudent();
        List<StudyPlan> planList = loadMyPlanList(currentStudent.getUserId());
        if (planList.isEmpty()) {
            StudyPlanDashboardVO empty = new StudyPlanDashboardVO();
            empty.setTotalPlanCount(0);
            empty.setTotalTaskCount(0);
            empty.setCompletedTaskCount(0);
            empty.setInProgressPlanCount(0);
            empty.setTotalStudyHours(BigDecimal.ZERO.setScale(1, RoundingMode.HALF_UP));
            return empty;
        }

        Map<String, List<StudyPlanItem>> itemMap = loadPlanItemMap(planList);
        syncPlanStatusIfNeeded(planList, itemMap);
        Map<String, CourseInfo> courseMap = loadCourseMap(planList.stream().map(StudyPlan::getCourseId).collect(Collectors.toSet()));
        Map<Integer, ResourceInfo> coverMap = loadCoverMap(courseMap.values());
        Map<String, CourseChapter> chapterMap = loadChapterMap(planList.stream().map(StudyPlan::getCourseId).collect(Collectors.toSet()));
        Map<String, CourseChapterLesson> lessonMap = loadLessonMap(planList.stream().map(StudyPlan::getCourseId).collect(Collectors.toSet()));
        Map<String, CourseStudyProgress> progressMap = loadCourseProgressMap(currentStudent.getUserId());

        StudyPlanDashboardVO dashboard = new StudyPlanDashboardVO();
        List<StudyPlanListItemVO> planItemVOList = new ArrayList<>();
        List<StudyPlanScheduleItemVO> scheduleItemList = new ArrayList<>();

        int totalTaskCount = 0;
        int completedTaskCount = 0;
        int inProgressPlanCount = 0;
        Set<String> plannedCourseIdSet = new LinkedHashSet<>();

        for (StudyPlan plan : planList) {
            List<StudyPlanItem> planItems = itemMap.getOrDefault(plan.getPlanId(), List.of());
            int taskCount = planItems.size();
            int doneCount = (int) planItems.stream().filter(item -> Objects.equals(item.getStatus(), STATUS_COMPLETED)).count();
            totalTaskCount += taskCount;
            completedTaskCount += doneCount;
            if (Objects.equals(plan.getStatus(), STATUS_RUNNING)) {
                inProgressPlanCount++;
            }
            if (!StringTools.isEmpty(plan.getCourseId())) {
                plannedCourseIdSet.add(plan.getCourseId());
            }

            planItemVOList.add(buildPlanListItemVO(plan, courseMap.get(plan.getCourseId()), coverMap, taskCount, doneCount, planItems));
            for (StudyPlanItem planItem : planItems) {
                scheduleItemList.add(buildScheduleItemVO(plan, planItem, courseMap.get(plan.getCourseId()), coverMap, chapterMap.get(planItem.getChapterId()), lessonMap.get(planItem.getLessonId())));
            }
        }

        BigDecimal totalStudyHours = calculateTotalStudyHours(plannedCourseIdSet, progressMap);
        dashboard.setTotalPlanCount(planList.size());
        dashboard.setTotalTaskCount(totalTaskCount);
        dashboard.setCompletedTaskCount(completedTaskCount);
        dashboard.setInProgressPlanCount(inProgressPlanCount);
        dashboard.setTotalStudyHours(totalStudyHours);
        dashboard.setPlanList(planItemVOList);
        dashboard.setCalendarPlanList(scheduleItemList.stream()
                .sorted(Comparator.comparing(StudyPlanScheduleItemVO::getStudyDate, Comparator.nullsLast(String::compareTo))
                        .thenComparing(StudyPlanScheduleItemVO::getStartTimeText, Comparator.nullsLast(String::compareTo)))
                .toList());
        String today = DateUtil.format(new Date(), DATE_PATTERN);
        dashboard.setTodayPlanList(dashboard.getCalendarPlanList().stream()
                .filter(item -> Objects.equals(today, item.getStudyDate()))
                .toList());
        return dashboard;
    }

    public StudyPlanDetailVO getDetail(String planId) {
        UserInfo currentStudent = getCurrentStudent();
        StudyPlan plan = getOwnedPlan(currentStudent.getUserId(), planId);
        return buildPlanDetailVO(plan);
    }

    @Transactional(rollbackFor = Exception.class)
    public StudyPlanDetailVO savePlan(StudyPlanSaveDTO dto) {
        UserInfo currentStudent = getCurrentStudent();
        CourseInfo courseInfo = validateCourseAccess(currentStudent, dto.getCourseId());
        boolean isEdit = !StringTools.isEmpty(dto.getPlanId());
        StudyPlan existingPlan = null;
        if (isEdit) {
            existingPlan = getOwnedPlan(currentStudent.getUserId(), dto.getPlanId());
            if (!Objects.equals(existingPlan.getCourseId(), courseInfo.getCourseId())) {
                throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "当前计划课程不允许修改");
            }
        }

        // 按课时级别校验：不允许选择已在其他计划中的课时
        Set<String> newLessonIds = dto.getItemList().stream()
                .map(StudyPlanSaveItemDTO::getLessonId)
                .filter(id -> !StringTools.isEmpty(id))
                .map(StringTools::trim)
                .collect(Collectors.toSet());
        Set<String> plannedLessonIds = loadPlannedLessonIds(currentStudent.getUserId(), courseInfo.getCourseId(),
                isEdit ? existingPlan.getPlanId() : null);
        Set<String> conflictLessonIds = newLessonIds.stream()
                .filter(plannedLessonIds::contains)
                .collect(Collectors.toSet());
        if (!conflictLessonIds.isEmpty()) {
            throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "部分课时已在其他学习计划中，请取消勾选后重试");
        }

        Map<String, CourseChapter> chapterMap = loadChapterMap(Set.of(courseInfo.getCourseId()));
        Map<String, CourseChapterLesson> lessonMap = loadLessonMap(Set.of(courseInfo.getCourseId()));
        String planId = isEdit ? existingPlan.getPlanId() : generateStringId();
        Map<String, StudyPlanItem> oldItemMap = isEdit ? loadPlanItemMap(List.of(existingPlan)).getOrDefault(planId, List.of()).stream()
                .collect(Collectors.toMap(item -> buildItemKey(item.getChapterId(), item.getLessonId()), item -> item, (left, right) -> left)) : Map.of();

        StudyPlan plan = isEdit ? existingPlan : new StudyPlan();
        plan.setPlanId(planId);
        plan.setCourseId(courseInfo.getCourseId());
        plan.setStudentId(currentStudent.getUserId());
        plan.setDescription(StringTools.trim(dto.getDescription()));
        plan.setStatus(STATUS_PENDING);
        if (isEdit) {
            studyPlanService.updateStudyPlanByPlanId(plan, planId);
            StudyPlanItemQuery deleteQuery = new StudyPlanItemQuery();
            deleteQuery.setPlanId(planId);
            studyPlanItemService.deleteByParam(deleteQuery);
        } else {
            studyPlanService.add(plan);
        }

        List<StudyPlanItem> insertList = new ArrayList<>();
        for (StudyPlanSaveItemDTO itemDTO : dto.getItemList()) {
            validatePlanItem(courseInfo.getCourseId(), itemDTO, chapterMap, lessonMap);
            StudyPlanItem oldItem = oldItemMap.get(buildItemKey(itemDTO.getChapterId(), itemDTO.getLessonId()));
            StudyPlanItem item = new StudyPlanItem();
            item.setPlanId(planId);
            item.setCourseId(courseInfo.getCourseId());
            item.setChapterId(itemDTO.getChapterId());
            item.setLessonId(StringTools.trim(itemDTO.getLessonId()));
            item.setComplateTime(DateUtil.parse(itemDTO.getStudyDate(), DATE_PATTERN));
            item.setStartTime(encodeTimeRange(itemDTO.getStartTime(), itemDTO.getEndTime()));
            item.setStatus(oldItem == null ? STATUS_PENDING : safeStatus(oldItem.getStatus()));
            item.setCreateTime(new Date());
            insertList.add(item);
        }
        studyPlanItemService.addBatch(insertList);
        syncPlanStatus(planId);
        return buildPlanDetailVO(studyPlanService.getStudyPlanByPlanId(planId));
    }

    @Transactional(rollbackFor = Exception.class)
    public StudyPlanDetailVO updateItemStatus(StudyPlanItemStatusDTO dto) {
        UserInfo currentStudent = getCurrentStudent();
        StudyPlanItem item = studyPlanItemService.getStudyPlanItemByItemId(dto.getItemId());
        if (item == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "计划明细不存在");
        }
        StudyPlan plan = getOwnedPlan(currentStudent.getUserId(), item.getPlanId());
        StudyPlanItem update = new StudyPlanItem();
        update.setStatus(validateStatus(dto.getStatus()));
        studyPlanItemService.updateStudyPlanItemByItemId(update, item.getItemId());
        syncPlanStatus(plan.getPlanId());
        return buildPlanDetailVO(studyPlanService.getStudyPlanByPlanId(plan.getPlanId()));
    }

    private StudyPlanDetailVO buildPlanDetailVO(StudyPlan plan) {
        if (plan == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "学习计划不存在");
        }
        CourseInfo courseInfo = courseInfoService.getCourseInfoByCourseId(plan.getCourseId());
        Map<Integer, ResourceInfo> coverMap = courseInfo == null ? Map.of() : loadCoverMap(List.of(courseInfo));
        Map<String, CourseChapter> chapterMap = loadChapterMap(Set.of(plan.getCourseId()));
        Map<String, CourseChapterLesson> lessonMap = loadLessonMap(Set.of(plan.getCourseId()));
        StudyPlanItemQuery itemQuery = new StudyPlanItemQuery();
        itemQuery.setPlanId(plan.getPlanId());
        itemQuery.setOrderBy("s.complate_time asc,s.start_time asc,s.item_id asc");
        List<StudyPlanItem> itemList = studyPlanItemService.findListByParam(itemQuery);
        int taskCount = itemList.size();
        int completedCount = (int) itemList.stream().filter(item -> Objects.equals(item.getStatus(), STATUS_COMPLETED)).count();

        StudyPlanDetailVO vo = new StudyPlanDetailVO();
        vo.setPlanId(plan.getPlanId());
        vo.setCourseId(plan.getCourseId());
        vo.setCourseName(courseInfo == null ? "" : defaultString(courseInfo.getCourseName()));
        vo.setCoverPath(resolveCoverPath(courseInfo, coverMap));
        vo.setDescription(plan.getDescription());
        vo.setStatus(safeStatus(plan.getStatus()));
        vo.setStatusText(resolveStatusText(plan.getStatus()));
        vo.setTaskCount(taskCount);
        vo.setCompletedCount(completedCount);
        vo.setProgress(calculateProgress(taskCount, completedCount));
        vo.setItemList(itemList.stream()
                .map(item -> buildDetailItemVO(item, chapterMap.get(item.getChapterId()), lessonMap.get(item.getLessonId())))
                .toList());
        return vo;
    }

    private StudyPlanListItemVO buildPlanListItemVO(StudyPlan plan,
                                                    CourseInfo courseInfo,
                                                    Map<Integer, ResourceInfo> coverMap,
                                                    int taskCount,
                                                    int completedCount,
                                                    List<StudyPlanItem> planItems) {
        StudyPlanListItemVO vo = new StudyPlanListItemVO();
        vo.setPlanId(plan.getPlanId());
        vo.setCourseId(plan.getCourseId());
        vo.setCourseName(courseInfo == null ? "" : defaultString(courseInfo.getCourseName()));
        vo.setCoverPath(resolveCoverPath(courseInfo, coverMap));
        vo.setDescription(plan.getDescription());
        vo.setStatus(safeStatus(plan.getStatus()));
        vo.setStatusText(resolveStatusText(plan.getStatus()));
        vo.setTaskCount(taskCount);
        vo.setCompletedCount(completedCount);
        vo.setProgress(calculateProgress(taskCount, completedCount));
        Date deadline = planItems.stream()
                .map(StudyPlanItem::getComplateTime)
                .filter(Objects::nonNull)
                .max(Date::compareTo)
                .orElse(null);
        vo.setDeadline(DateUtil.format(deadline, DATE_PATTERN));
        return vo;
    }

    private StudyPlanScheduleItemVO buildScheduleItemVO(StudyPlan plan,
                                                        StudyPlanItem item,
                                                        CourseInfo courseInfo,
                                                        Map<Integer, ResourceInfo> coverMap,
                                                        CourseChapter chapter,
                                                        CourseChapterLesson lesson) {
        TimeRange timeRange = decodeTimeRange(item.getStartTime());
        StudyPlanScheduleItemVO vo = new StudyPlanScheduleItemVO();
        vo.setItemId(item.getItemId());
        vo.setPlanId(plan.getPlanId());
        vo.setCourseId(plan.getCourseId());
        vo.setCourseName(courseInfo == null ? "" : defaultString(courseInfo.getCourseName()));
        vo.setCoverPath(resolveCoverPath(courseInfo, coverMap));
        vo.setChapterId(item.getChapterId());
        vo.setChapterName(chapter == null ? "" : defaultString(chapter.getChapterName()));
        vo.setLessonId(item.getLessonId());
        vo.setLessonName(lesson == null ? "" : defaultString(lesson.getLessonName()));
        vo.setStudyDate(DateUtil.format(item.getComplateTime(), DATE_PATTERN));
        vo.setStartTimeText(timeRange.startText);
        vo.setEndTimeText(timeRange.endText);
        vo.setTimeRangeText(timeRange.rangeText);
        vo.setStatus(safeStatus(item.getStatus()));
        vo.setStatusText(resolveStatusText(item.getStatus()));
        vo.setCompleted(Objects.equals(item.getStatus(), STATUS_COMPLETED));
        return vo;
    }

    private StudyPlanDetailItemVO buildDetailItemVO(StudyPlanItem item, CourseChapter chapter, CourseChapterLesson lesson) {
        TimeRange timeRange = decodeTimeRange(item.getStartTime());
        StudyPlanDetailItemVO vo = new StudyPlanDetailItemVO();
        vo.setItemId(item.getItemId());
        vo.setChapterId(item.getChapterId());
        vo.setChapterName(chapter == null ? "" : defaultString(chapter.getChapterName()));
        vo.setLessonId(item.getLessonId());
        vo.setLessonName(lesson == null ? "" : defaultString(lesson.getLessonName()));
        vo.setStudyDate(DateUtil.format(item.getComplateTime(), DATE_PATTERN));
        vo.setStartTimeText(timeRange.startText);
        vo.setEndTimeText(timeRange.endText);
        vo.setTimeRangeText(timeRange.rangeText);
        vo.setStatus(safeStatus(item.getStatus()));
        vo.setStatusText(resolveStatusText(item.getStatus()));
        return vo;
    }

    private void syncPlanStatusIfNeeded(List<StudyPlan> planList, Map<String, List<StudyPlanItem>> itemMap) {
        for (StudyPlan plan : planList) {
            Integer status = resolvePlanStatus(itemMap.getOrDefault(plan.getPlanId(), List.of()));
            if (!Objects.equals(status, plan.getStatus())) {
                StudyPlan update = new StudyPlan();
                update.setStatus(status);
                studyPlanService.updateStudyPlanByPlanId(update, plan.getPlanId());
                plan.setStatus(status);
            }
        }
    }

    private void syncPlanStatus(String planId) {
        StudyPlanItemQuery itemQuery = new StudyPlanItemQuery();
        itemQuery.setPlanId(planId);
        List<StudyPlanItem> itemList = studyPlanItemService.findListByParam(itemQuery);
        StudyPlan update = new StudyPlan();
        update.setStatus(resolvePlanStatus(itemList));
        studyPlanService.updateStudyPlanByPlanId(update, planId);
    }

    private Integer resolvePlanStatus(List<StudyPlanItem> itemList) {
        if (itemList == null || itemList.isEmpty()) {
            return STATUS_PENDING;
        }
        boolean allCompleted = itemList.stream().allMatch(item -> Objects.equals(item.getStatus(), STATUS_COMPLETED));
        if (allCompleted) {
            return STATUS_COMPLETED;
        }
        boolean hasStarted = itemList.stream().anyMatch(item -> !Objects.equals(item.getStatus(), STATUS_PENDING));
        return hasStarted ? STATUS_RUNNING : STATUS_PENDING;
    }

    private void validatePlanItem(String courseId,
                                  StudyPlanSaveItemDTO itemDTO,
                                  Map<String, CourseChapter> chapterMap,
                                  Map<String, CourseChapterLesson> lessonMap) {
        String chapterId = StringTools.trim(itemDTO.getChapterId());
        CourseChapter chapter = chapterMap.get(chapterId);
        if (chapter == null || !Objects.equals(chapter.getCourseId(), courseId)) {
            throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "学习章节不存在或不属于当前课程");
        }
        String lessonId = StringTools.trim(itemDTO.getLessonId());
        if (StringTools.isEmpty(lessonId)) {
            throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "学习课时不能为空");
        }
        CourseChapterLesson lesson = lessonMap.get(lessonId);
        if (lesson == null || !Objects.equals(lesson.getCourseId(), courseId) || !Objects.equals(lesson.getChapterId(), chapterId)) {
            throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "学习课时不存在或不属于当前章节");
        }
        Date studyDate = DateUtil.parse(itemDTO.getStudyDate(), DATE_PATTERN);
        if (studyDate == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "学习日期格式不正确");
        }
        encodeTimeRange(itemDTO.getStartTime(), itemDTO.getEndTime());
    }

    private StudyPlan getOwnedPlan(Integer userId, String planId) {
        StudyPlan plan = studyPlanService.getStudyPlanByPlanId(StringTools.trim(planId));
        if (plan == null || !Objects.equals(plan.getStudentId(), userId)) {
            throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "学习计划不存在");
        }
        return plan;
    }

    /**
     * 获取某课程下当前学生已规划的课时ID列表（用于前端置灰）
     */
    public List<String> getPlannedLessonIds(String courseId, String excludePlanId) {
        UserInfo currentStudent = getCurrentStudent();
        String normalizedCourseId = StringTools.trim(courseId);
        if (StringTools.isEmpty(normalizedCourseId)) {
            return List.of();
        }
        return new ArrayList<>(loadPlannedLessonIds(currentStudent.getUserId(), normalizedCourseId, excludePlanId));
    }

    /**
     * 查询当前学生在指定课程下已规划的课时ID集合，排除指定计划
     */
    private Set<String> loadPlannedLessonIds(Integer userId, String courseId, String excludePlanId) {
        StudyPlanQuery planQuery = new StudyPlanQuery();
        planQuery.setStudentId(userId);
        planQuery.setCourseId(courseId);
        List<StudyPlan> plans = studyPlanService.findListByParam(planQuery);
        List<String> planIds = plans.stream()
                .map(StudyPlan::getPlanId)
                .filter(id -> !StringTools.isEmpty(id) && !Objects.equals(id, excludePlanId))
                .toList();
        if (planIds.isEmpty()) {
            return Set.of();
        }
        return studyPlanItemMapper.selectByPlanIdList(planIds).stream()
                .map(StudyPlanItem::getLessonId)
                .filter(id -> !StringTools.isEmpty(id))
                .collect(Collectors.toSet());
    }

    private StudyPlan findPlanByStudentAndCourse(Integer userId, String courseId) {
        StudyPlanQuery query = new StudyPlanQuery();
        query.setStudentId(userId);
        query.setCourseId(courseId);
        List<StudyPlan> list = studyPlanService.findListByParam(query);
        return list.isEmpty() ? null : list.get(0);
    }

    private List<StudyPlan> loadMyPlanList(Integer userId) {
        StudyPlanQuery query = new StudyPlanQuery();
        query.setStudentId(userId);
        query.setOrderBy("s.plan_id desc");
        return studyPlanService.findListByParam(query);
    }

    private Map<String, List<StudyPlanItem>> loadPlanItemMap(List<StudyPlan> planList) {
        List<String> planIdList = planList.stream()
                .map(StudyPlan::getPlanId)
                .filter(item -> !StringTools.isEmpty(item))
                .toList();
        if (planIdList.isEmpty()) {
            return Map.of();
        }
        return studyPlanItemMapper.selectByPlanIdList(planIdList).stream()
                .collect(Collectors.groupingBy(StudyPlanItem::getPlanId, LinkedHashMap::new, Collectors.toList()));
    }

    private Map<String, CourseInfo> loadCourseMap(Set<String> courseIdSet) {
        if (courseIdSet == null || courseIdSet.isEmpty()) {
            return Map.of();
        }
        CourseInfoQuery query = new CourseInfoQuery();
        return courseInfoService.findListByParam(query).stream()
                .filter(item -> courseIdSet.contains(item.getCourseId()))
                .collect(Collectors.toMap(CourseInfo::getCourseId, item -> item, (left, right) -> left));
    }

    private Map<String, CourseChapter> loadChapterMap(Set<String> courseIdSet) {
        if (courseIdSet == null || courseIdSet.isEmpty()) {
            return Map.of();
        }
        CourseChapterQuery query = new CourseChapterQuery();
        query.setOrderBy(CHAPTER_ORDER_BY);
        return courseChapterService.findListByParam(query).stream()
                .filter(item -> courseIdSet.contains(item.getCourseId()))
                .collect(Collectors.toMap(CourseChapter::getChapterId, item -> item, (left, right) -> left));
    }

    private Map<String, CourseChapterLesson> loadLessonMap(Set<String> courseIdSet) {
        if (courseIdSet == null || courseIdSet.isEmpty()) {
            return Map.of();
        }
        CourseChapterLessonQuery query = new CourseChapterLessonQuery();
        query.setOrderBy(LESSON_ORDER_BY);
        return courseChapterLessonService.findListByParam(query).stream()
                .filter(item -> courseIdSet.contains(item.getCourseId()))
                .collect(Collectors.toMap(CourseChapterLesson::getLessonId, item -> item, (left, right) -> left));
    }

    private Map<Integer, ResourceInfo> loadCoverMap(Collection<CourseInfo> courseList) {
        Set<Integer> coverIdSet = courseList.stream()
                .map(CourseInfo::getCoverResourceId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (coverIdSet.isEmpty()) {
            return Map.of();
        }
        List<String> resourceIdList = coverIdSet.stream().map(String::valueOf).toList();
        return resourceInfoService.getResourceInfoByResourceIdList(resourceIdList).stream()
                .collect(Collectors.toMap(ResourceInfo::getResourceId, item -> item, (left, right) -> left));
    }

    private Map<String, CourseStudyProgress> loadCourseProgressMap(Integer userId) {
        CourseStudyProgressQuery query = new CourseStudyProgressQuery();
        query.setUserId(userId);
        return courseStudyProgressService.findListByParam(query).stream()
                .collect(Collectors.toMap(CourseStudyProgress::getCourseId, item -> item, (left, right) -> left));
    }

    private BigDecimal calculateTotalStudyHours(Set<String> courseIdSet, Map<String, CourseStudyProgress> progressMap) {
        int totalSeconds = courseIdSet.stream()
                .map(progressMap::get)
                .filter(Objects::nonNull)
                .mapToInt(item -> item.getStudySeconds() == null ? 0 : item.getStudySeconds())
                .sum();
        return BigDecimal.valueOf(totalSeconds)
                .divide(BigDecimal.valueOf(3600), 1, RoundingMode.HALF_UP);
    }

    private CourseInfo validateCourseAccess(UserInfo student, String courseId) {
        String normalizedCourseId = StringTools.trim(courseId);
        if (StringTools.isEmpty(normalizedCourseId)) {
            throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "课程不能为空");
        }
        CourseInfo courseInfo = courseInfoService.getCourseInfoByCourseId(normalizedCourseId);
        if (courseInfo == null || !Objects.equals(courseInfo.getStatus(), 1) || !Objects.equals(courseInfo.getRecordStatus(), 1)) {
            throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "课程不存在、未录制完成或已下线");
        }
        ensureStudentCanAccessCourse(student, normalizedCourseId);
        return courseInfo;
    }

    private void ensureStudentCanAccessCourse(UserInfo student, String courseId) {
        List<Integer> classIdList = parseClassIds(student.getClassId());
        if (classIdList.isEmpty()) {
            throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "当前学生未分配班级");
        }
        for (Integer classId : classIdList) {
            CourseClassQuery query = new CourseClassQuery();
            query.setCourseId(courseId);
            query.setClassId(classId);
            List<CourseClass> relationList = courseClassService.findListByParam(query);
            if (!relationList.isEmpty()) {
                return;
            }
        }
        throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "当前学生无权访问该课程");
    }

    private UserInfo getCurrentStudent() {
        LoginUserVO loginUser = LoginUserContextHolder.get();
        if (loginUser == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_901);
        }
        if (!Objects.equals(loginUser.getRoleType(), USER_ROLE_STUDENT)) {
            throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "请使用学生账号操作学习计划");
        }
        UserInfo currentStudent = userInfoService.getUserInfoByUserId(loginUser.getUserId());
        if (currentStudent == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "学生信息不存在");
        }
        return currentStudent;
    }

    private List<Integer> parseClassIds(String classIdText) {
        if (StringTools.isEmpty(classIdText)) {
            return List.of();
        }
        List<Integer> result = new ArrayList<>();
        for (String item : classIdText.split(",")) {
            String value = StringTools.trim(item);
            if (StringTools.isEmpty(value)) {
                continue;
            }
            try {
                result.add(Integer.valueOf(value));
            } catch (NumberFormatException ignore) {
                // ignore invalid class id
            }
        }
        return result;
    }

    private Integer encodeTimeRange(String startTimeText, String endTimeText) {
        int startMinutes = parseTimeTextToMinutes(startTimeText);
        int endMinutes = parseTimeTextToMinutes(endTimeText);
        if (endMinutes <= startMinutes) {
            throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "结束时间必须晚于开始时间");
        }
        return startMinutes * TIME_MULTIPLIER + endMinutes;
    }

    private TimeRange decodeTimeRange(Integer packedTimeRange) {
        if (packedTimeRange == null || packedTimeRange <= 0) {
            return new TimeRange("--:--", "--:--");
        }
        int startMinutes = packedTimeRange / TIME_MULTIPLIER;
        int endMinutes = packedTimeRange % TIME_MULTIPLIER;
        return new TimeRange(formatMinutes(startMinutes), formatMinutes(endMinutes));
    }

    private int parseTimeTextToMinutes(String timeText) {
        String value = StringTools.trim(timeText);
        if (StringTools.isEmpty(value) || !value.contains(":")) {
            throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "时间格式不正确");
        }
        String[] values = value.split(":");
        if (values.length != 2) {
            throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "时间格式不正确");
        }
        try {
            int hour = Integer.parseInt(values[0]);
            int minute = Integer.parseInt(values[1]);
            if (hour < 0 || hour > 23 || minute < 0 || minute > 59) {
                throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "时间格式不正确");
            }
            return hour * 60 + minute;
        } catch (NumberFormatException e) {
            throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "时间格式不正确");
        }
    }

    private String formatMinutes(int totalMinutes) {
        int hour = Math.max(0, totalMinutes) / 60;
        int minute = Math.max(0, totalMinutes) % 60;
        return String.format("%02d:%02d", hour, minute);
    }

    private Integer validateStatus(Integer status) {
        int safeStatus = safeStatus(status);
        if (safeStatus < STATUS_PENDING || safeStatus > STATUS_COMPLETED) {
            throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "学习状态不正确");
        }
        return safeStatus;
    }

    private Integer safeStatus(Integer status) {
        return status == null ? STATUS_PENDING : status;
    }

    private String resolveStatusText(Integer status) {
        if (Objects.equals(status, STATUS_COMPLETED)) {
            return "已完成";
        }
        if (Objects.equals(status, STATUS_RUNNING)) {
            return "进行中";
        }
        return "未开始";
    }

    private int calculateProgress(int taskCount, int completedCount) {
        if (taskCount <= 0) {
            return 0;
        }
        return Math.min(100, Math.max(0, Math.round(completedCount * 100F / taskCount)));
    }

    private String resolveCoverPath(CourseInfo courseInfo, Map<Integer, ResourceInfo> coverMap) {
        if (courseInfo == null || courseInfo.getCoverResourceId() == null) {
            return "";
        }
        ResourceInfo resourceInfo = coverMap.get(courseInfo.getCoverResourceId());
        if (resourceInfo == null) {
            return "";
        }
        return StringTools.isEmpty(resourceInfo.getCoverPath()) ? defaultString(resourceInfo.getFilePath()) : defaultString(resourceInfo.getCoverPath());
    }

    private String buildItemKey(String chapterId, String lessonId) {
        return defaultString(chapterId) + "#" + defaultString(lessonId);
    }

    private String defaultString(String value) {
        return value == null ? "" : value;
    }

    private String generateStringId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();
    }

    private static class TimeRange {
        private final String startText;
        private final String endText;
        private final String rangeText;

        private TimeRange(String startText, String endText) {
            this.startText = startText;
            this.endText = endText;
            this.rangeText = startText + "-" + endText;
        }
    }
}
