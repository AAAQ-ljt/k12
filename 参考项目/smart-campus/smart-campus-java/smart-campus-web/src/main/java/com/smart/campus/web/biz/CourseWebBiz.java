package com.smart.campus.web.biz;

import com.smart.campus.redis.CourseStudyProgressRedisComponent;
import com.smart.campus.entity.dto.CourseStudyProgressReportDTO;
import com.smart.campus.entity.enums.ResponseCodeEnum;
import com.smart.campus.entity.enums.UserRoleTypeEnum;
import com.smart.campus.entity.po.ClassInfo;
import com.smart.campus.entity.po.CourseChapter;
import com.smart.campus.entity.po.CourseChapterLesson;
import com.smart.campus.entity.po.CourseChapterLessonResource;
import com.smart.campus.entity.po.CourseClass;
import com.smart.campus.entity.po.CourseInfo;
import com.smart.campus.entity.po.CourseStudyLessonProgress;
import com.smart.campus.entity.po.CourseStudyLog;
import com.smart.campus.entity.po.CourseStudyProgress;
import com.smart.campus.entity.po.CourseUserCollection;
import com.smart.campus.entity.po.PaperInfo;
import com.smart.campus.entity.po.ResourceInfo;
import com.smart.campus.entity.po.UserInfo;
import com.smart.campus.entity.query.CourseChapterLessonQuery;
import com.smart.campus.entity.query.CourseChapterLessonResourceQuery;
import com.smart.campus.entity.query.CourseChapterQuery;
import com.smart.campus.entity.query.CourseClassQuery;
import com.smart.campus.entity.query.CourseInfoQuery;
import com.smart.campus.entity.query.CourseStudyLessonProgressQuery;
import com.smart.campus.entity.query.CourseStudyProgressQuery;
import com.smart.campus.entity.query.CourseUserCollectionQuery;
import com.smart.campus.entity.vo.CourseChapterDetailVO;
import com.smart.campus.entity.vo.CourseDetailVO;
import com.smart.campus.entity.vo.CourseLessonDetailVO;
import com.smart.campus.entity.vo.CourseLessonResourceVO;
import com.smart.campus.entity.vo.CourseListItemVO;
import com.smart.campus.entity.vo.CourseStudyReportVO;
import com.smart.campus.entity.vo.LoginUserVO;
import com.smart.campus.exception.BusinessException;
import com.smart.campus.service.ClassInfoService;
import com.smart.campus.service.CourseChapterLessonResourceService;
import com.smart.campus.service.CourseChapterLessonService;
import com.smart.campus.service.CourseChapterService;
import com.smart.campus.service.CourseClassService;
import com.smart.campus.service.CourseInfoService;
import com.smart.campus.service.CourseStudyLessonProgressService;
import com.smart.campus.service.CourseStudyLogService;
import com.smart.campus.service.CourseStudyProgressService;
import com.smart.campus.service.CourseUserCollectionService;
import com.smart.campus.service.PaperInfoService;
import com.smart.campus.service.ResourceInfoService;
import com.smart.campus.service.UserInfoService;
import com.smart.campus.utils.LoginUserContextHolder;
import com.smart.campus.utils.StringTools;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CourseWebBiz {

    private static final String COURSE_ORDER_BY = "c.update_time desc,c.create_time desc";
    private static final String CHAPTER_ORDER_BY = "c.sort_order asc,c.chapter_id asc";
    private static final String LESSON_ORDER_BY = "c.sort_order asc,c.lesson_id asc";
    private static final String LESSON_RESOURCE_ORDER_BY = "c.is_primary desc,c.sort_order asc,c.id asc";
    private static final int LESSON_RESOURCE_ROLE_VIDEO = 1;
    private static final int LESSON_RESOURCE_ROLE_COURSEWARE = 2;
    private static final int LESSON_RESOURCE_ROLE_PAPER = 3;
    private static final int PAPER_TYPE_HOMEWORK = 1;
    private static final int PAPER_TYPE_EXAM = 2;
    private static final int STUDY_STATUS_NOT_STARTED = 0;
    private static final int STUDY_STATUS_STUDYING = 1;
    private static final int STUDY_STATUS_COMPLETED = 2;
    private static final int BOOLEAN_YES = 1;
    private static final double LESSON_COMPLETION_THRESHOLD = 0.9D;

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
    private CourseChapterLessonResourceService courseChapterLessonResourceService;

    @Resource
    private ClassInfoService classInfoService;

    @Resource
    private ResourceInfoService resourceInfoService;

    @Resource
    private PaperInfoService paperInfoService;

    @Resource
    private CourseStudyProgressService courseStudyProgressService;

    @Resource
    private CourseStudyLessonProgressService courseStudyLessonProgressService;

    @Resource
    private CourseStudyLogService courseStudyLogService;

    @Resource
    private CourseStudyProgressRedisComponent courseStudyProgressRedisComponent;

    @Resource
    private CourseUserCollectionService courseUserCollectionService;

    public List<CourseListItemVO> loadMyCourseList() {
        UserInfo currentStudent = getCurrentStudent();
        List<Integer> classIdList = parseClassIds(currentStudent.getClassId());
        if (classIdList.isEmpty()) {
            return List.of();
        }

        Map<String, List<CourseClass>> matchedCourseClassMap = loadMatchedCourseClassMap(classIdList);
        if (matchedCourseClassMap.isEmpty()) {
            return List.of();
        }

        Set<String> courseIdSet = matchedCourseClassMap.keySet();
        List<CourseInfo> courseList = loadEnabledCourses(courseIdSet);
        Set<String> collectedCourseIdSet = loadCollectedCourseIdSet(currentStudent.getUserId());
        return buildCourseList(currentStudent.getUserId(), courseList, matchedCourseClassMap, collectedCourseIdSet);
    }

    public CourseDetailVO getMyCourseDetail(String courseId) {
        String normalizedCourseId = StringTools.trim(courseId);
        if (StringTools.isEmpty(normalizedCourseId)) {
            throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "课程ID不能为空");
        }

        UserInfo currentStudent = getCurrentStudent();
        List<CourseClass> matchedClassList = ensureStudentCanAccessCourse(currentStudent, normalizedCourseId);

        CourseInfo courseInfo = courseInfoService.getCourseInfoByCourseId(normalizedCourseId);
        if (courseInfo == null || !Objects.equals(courseInfo.getStatus(), 1) || !Objects.equals(courseInfo.getRecordStatus(), 1)) {
            throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "课程信息不存在、未录制完成或已下线");
        }
        return buildCourseDetail(courseInfo, matchedClassList, currentStudent.getUserId());
    }

    @Transactional(rollbackFor = Exception.class)
    public Integer saveCourseCollection(String courseId, Integer collectFlag) {
        String normalizedCourseId = StringTools.trim(courseId);
        if (StringTools.isEmpty(normalizedCourseId)) {
            throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "课程ID不能为空");
        }
        UserInfo currentStudent = getCurrentStudent();
        ensureStudentCanAccessCourse(currentStudent, normalizedCourseId);
        CourseInfo courseInfo = courseInfoService.getCourseInfoByCourseId(normalizedCourseId);
        if (courseInfo == null || !Objects.equals(courseInfo.getStatus(), 1) || !Objects.equals(courseInfo.getRecordStatus(), 1)) {
            throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "课程信息不存在、未录制完成或已下线");
        }
        int targetCollectFlag = Objects.equals(collectFlag, 1) ? 1 : 0;
        CourseUserCollection dbCollection = courseUserCollectionService
                .getCourseUserCollectionByCourseIdAndUserId(normalizedCourseId, currentStudent.getUserId());
        if (targetCollectFlag == 1) {
            if (dbCollection == null) {
                CourseUserCollection collection = new CourseUserCollection();
                collection.setCourseId(normalizedCourseId);
                collection.setUserId(currentStudent.getUserId());
                courseUserCollectionService.add(collection);
            }
            return 1;
        }
        if (dbCollection != null) {
            courseUserCollectionService.deleteCourseUserCollectionByCourseIdAndUserId(normalizedCourseId, currentStudent.getUserId());
        }
        return 0;
    }

    @Transactional(rollbackFor = Exception.class)
    public CourseStudyReportVO reportStudyProgress(CourseStudyProgressReportDTO dto) {
        UserInfo currentStudent = getCurrentStudent();
        String courseId = StringTools.trim(dto.getCourseId());
        String chapterId = StringTools.trim(dto.getChapterId());
        String lessonId = StringTools.trim(dto.getLessonId());
        String sessionId = StringTools.trim(dto.getSessionId());
        if (StringTools.isEmpty(courseId) || StringTools.isEmpty(chapterId) || StringTools.isEmpty(lessonId)) {
            throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "学习进度参数不完整");
        }

        int watchSeconds = Math.max(0, safeInt(dto.getWatchSeconds()));
        int positionSeconds = Math.max(0, safeInt(dto.getPositionSeconds()));
        int durationSeconds = Math.max(0, safeInt(dto.getDurationSeconds()));
        boolean forceComplete = Objects.equals(dto.getForceComplete(), BOOLEAN_YES);
        Date now = new Date();
        saveStudyLog(currentStudent.getUserId(), sessionId, courseId, chapterId, lessonId, watchSeconds, now);

        CourseStudyLessonProgress lessonProgress = getMergedLessonStudyProgress(currentStudent.getUserId(), lessonId);
        boolean isNewLessonProgress = lessonProgress == null;
        if (isNewLessonProgress) {
            lessonProgress = new CourseStudyLessonProgress();
            lessonProgress.setUserId(currentStudent.getUserId());
            lessonProgress.setCourseId(courseId);
            lessonProgress.setChapterId(chapterId);
            lessonProgress.setLessonId(lessonId);
        }
        lessonProgress.setVideoResourceId(dto.getVideoResourceId());
        lessonProgress.setStudySeconds(safeAdd(lessonProgress.getStudySeconds(), watchSeconds));
        lessonProgress.setLastPositionSeconds(positionSeconds);
        lessonProgress.setMaxPositionSeconds(Math.max(safeInt(lessonProgress.getMaxPositionSeconds()), positionSeconds));
        lessonProgress.setVideoDurationSeconds(Math.max(safeInt(lessonProgress.getVideoDurationSeconds()), durationSeconds));
        boolean lessonCompleted = shouldMarkLessonCompleted(
                forceComplete,
                lessonProgress.getMaxPositionSeconds(),
                lessonProgress.getVideoDurationSeconds(),
                lessonProgress.getIsCompleted()
        );
        lessonProgress.setIsCompleted(lessonCompleted ? BOOLEAN_YES : STUDY_STATUS_NOT_STARTED);
        if (lessonCompleted && lessonProgress.getCompleteTime() == null) {
            lessonProgress.setCompleteTime(now);
        }
        lessonProgress.setLastStudyTime(now);
        courseStudyProgressRedisComponent.saveLessonProgress(lessonProgress);

        CourseStudyProgress courseProgress = getMergedCourseStudyProgress(currentStudent.getUserId(), courseId);
        boolean isNewCourseProgress = courseProgress == null;
        if (isNewCourseProgress) {
            courseProgress = new CourseStudyProgress();
            courseProgress.setUserId(currentStudent.getUserId());
            courseProgress.setCourseId(courseId);
        }
        courseProgress.setCurrentChapterId(chapterId);
        courseProgress.setCurrentLessonId(lessonId);
        courseProgress.setStudySeconds(safeAdd(courseProgress.getStudySeconds(), watchSeconds));
        courseProgress.setLastStudyTime(now);

        int totalLessonCount = countCourseLesson(courseId);
        Map<String, CourseStudyLessonProgress> mergedLessonProgressMap = loadMergedLessonStudyProgressMap(currentStudent.getUserId(), courseId);
        mergedLessonProgressMap.put(lessonId, lessonProgress);
        int completedLessonCount = countCompletedLesson(mergedLessonProgressMap);
        courseProgress.setStatus(resolveCourseStudyStatus(totalLessonCount, completedLessonCount, courseProgress.getStudySeconds()));
        courseStudyProgressRedisComponent.saveCourseProgress(courseProgress);
        return buildCourseStudyReportVO(courseProgress, lessonProgress);
    }

    private void saveStudyLog(Integer userId,
                              String sessionId,
                              String courseId,
                              String chapterId,
                              String lessonId,
                              int watchSeconds,
                              Date now) {
        if (userId == null || StringTools.isEmpty(sessionId)) {
            return;
        }
        CourseStudyLog dbLog = courseStudyLogService.getCourseStudyLogBySessionId(sessionId);
        if (dbLog == null) {
            if (watchSeconds <= 0) {
                return;
            }
            CourseStudyLog addLog = new CourseStudyLog();
            addLog.setSessionId(sessionId);
            addLog.setUserId(userId);
            addLog.setCourseId(courseId);
            addLog.setChapterId(chapterId);
            addLog.setLessonId(lessonId);
            addLog.setStartTime(new Date(now.getTime() - watchSeconds * 1000L));
            addLog.setEndTime(now);
            addLog.setStudySeconds(watchSeconds);
            courseStudyLogService.add(addLog);
            return;
        }
        CourseStudyLog updateLog = new CourseStudyLog();
        updateLog.setCourseId(courseId);
        updateLog.setChapterId(chapterId);
        updateLog.setLessonId(lessonId);
        updateLog.setEndTime(now);
        if (watchSeconds > 0) {
            updateLog.setStudySeconds(safeAdd(dbLog.getStudySeconds(), watchSeconds));
        }
        courseStudyLogService.updateCourseStudyLogBySessionId(updateLog, sessionId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void flushStudyProgressCacheToDb() {
        flushCachedLessonProgressToDb();
        flushCachedCourseProgressToDb();
    }

    private UserInfo getCurrentStudent() {
        LoginUserVO loginUser = LoginUserContextHolder.get();
        if (loginUser == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_901);
        }
        if (!UserRoleTypeEnum.STUDENT.getCode().equals(loginUser.getRoleType())) {
            throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "请使用学生账号查看课程");
        }
        UserInfo userInfo = userInfoService.getUserInfoByUserId(loginUser.getUserId());
        if (userInfo == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "当前学生信息不存在");
        }
        return userInfo;
    }

    private List<Integer> parseClassIds(String classIdText) {
        if (StringTools.isEmpty(classIdText)) {
            return List.of();
        }
        LinkedHashSet<Integer> classIdSet = new LinkedHashSet<>();
        for (String item : classIdText.split(",")) {
            String value = StringTools.trim(item);
            if (StringTools.isEmpty(value)) {
                continue;
            }
            try {
                classIdSet.add(Integer.valueOf(value));
            } catch (NumberFormatException ignore) {
                // Ignore invalid class ids to avoid breaking the whole page.
            }
        }
        return new ArrayList<>(classIdSet);
    }

    private Map<String, List<CourseClass>> loadMatchedCourseClassMap(List<Integer> classIdList) {
        Map<String, List<CourseClass>> result = new HashMap<>();
        for (Integer classId : classIdList) {
            if (classId == null) {
                continue;
            }
            CourseClassQuery query = new CourseClassQuery();
            query.setClassId(classId);
            query.setOrderBy("c.course_id asc");
            List<CourseClass> relationList = courseClassService.findListByParam(query);
            for (CourseClass relation : relationList) {
                if (StringTools.isEmpty(relation.getCourseId())) {
                    continue;
                }
                result.computeIfAbsent(relation.getCourseId(), key -> new ArrayList<>()).add(relation);
            }
        }
        return result;
    }

    private List<CourseInfo> loadEnabledCourses(Set<String> courseIdSet) {
        if (courseIdSet.isEmpty()) {
            return List.of();
        }
        CourseInfoQuery query = new CourseInfoQuery();
        query.setStatus(1);
        query.setRecordStatus(1);
        query.setOrderBy(COURSE_ORDER_BY);
        return courseInfoService.findListByParam(query).stream()
                .filter(item -> courseIdSet.contains(item.getCourseId()))
                .sorted(Comparator.comparing(CourseInfo::getUpdateTime, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(CourseInfo::getCreateTime, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    private List<CourseListItemVO> buildCourseList(Integer userId,
                                                   List<CourseInfo> courseList,
                                                   Map<String, List<CourseClass>> matchedCourseClassMap,
                                                   Set<String> collectedCourseIdSet) {
        if (courseList == null || courseList.isEmpty()) {
            return List.of();
        }
        Set<String> courseIdSet = courseList.stream()
                .map(CourseInfo::getCourseId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<String, List<CourseChapter>> chapterMap = loadCourseChapterMap(courseIdSet);
        Map<String, List<CourseChapterLesson>> lessonMap = loadCourseLessonMap(courseIdSet);
        // 聚合学生在所有课程中的学习进度（数据库 + Redis 缓存合并），用于填充列表的进度与时长字段
        Map<String, CourseStudyProgress> courseProgressMap = loadMergedCourseStudyProgressMap(userId, courseIdSet);
        Map<String, Map<String, CourseStudyLessonProgress>> lessonProgressMap =
                loadMergedLessonStudyProgressMapByCourse(userId, courseIdSet, lessonMap);

        Set<Integer> teacherIdSet = courseList.stream()
                .map(CourseInfo::getTeacherId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Set<Integer> coverIdSet = courseList.stream()
                .map(CourseInfo::getCoverResourceId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Set<Integer> classIdSet = matchedCourseClassMap.values().stream()
                .flatMap(Collection::stream)
                .map(CourseClass::getClassId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Integer, UserInfo> teacherMap = loadTeacherMap(teacherIdSet);
        Map<Integer, ResourceInfo> coverMap = loadResourceMap(coverIdSet);
        Map<Integer, ClassInfo> classInfoMap = loadClassInfoMap(classIdSet);

        List<CourseListItemVO> result = new ArrayList<>();
        for (CourseInfo item : courseList) {
            CourseListItemVO vo = new CourseListItemVO();
            vo.setCourseId(item.getCourseId());
            vo.setCourseName(item.getCourseName());
            vo.setCoverResourceId(item.getCoverResourceId());
            vo.setCoverPath(resolveCoverPath(item.getCoverResourceId(), coverMap));
            vo.setTeacherId(item.getTeacherId());
            vo.setTeacherName(resolveTeacherName(item.getTeacherId(), teacherMap));
            vo.setDescription(item.getDescription());
            vo.setRecordStatus(item.getRecordStatus());
            vo.setStatus(item.getStatus());
            vo.setCreateTime(item.getCreateTime());
            vo.setUpdateTime(item.getUpdateTime());

            List<CourseClass> classList = matchedCourseClassMap.getOrDefault(item.getCourseId(), List.of());
            List<Integer> classIds = classList.stream()
                    .map(CourseClass::getClassId)
                    .filter(Objects::nonNull)
                    .distinct()
                    .toList();
            vo.setClassCount(classIds.size());
            vo.setClassNames(joinClassNames(classIds, classInfoMap));

            List<CourseChapter> courseChapterList = chapterMap.getOrDefault(item.getCourseId(), List.of());
            List<CourseChapterLesson> courseLessonList = lessonMap.getOrDefault(item.getCourseId(), List.of());
            vo.setChapterCount(courseChapterList.size());
            vo.setLessonCount(courseLessonList.size());
            vo.setIsCollected(collectedCourseIdSet.contains(item.getCourseId()) ? 1 : 0);

            // 根据课程进度与各课时进度填充学习数据，避免前端再用静态规则伪造
            fillStudyProgressFields(vo, courseChapterList, courseLessonList,
                    courseProgressMap.get(item.getCourseId()),
                    lessonProgressMap.getOrDefault(item.getCourseId(), Map.of()));
            result.add(vo);
        }
        return result;
    }

    private void fillStudyProgressFields(CourseListItemVO vo,
                                         List<CourseChapter> chapterList,
                                         List<CourseChapterLesson> lessonList,
                                         CourseStudyProgress courseProgress,
                                         Map<String, CourseStudyLessonProgress> lessonProgressMap) {
        int totalLessonCount = lessonList == null ? 0 : lessonList.size();
        int completedLessonCount = countCompletedLesson(lessonProgressMap);
        int progress = totalLessonCount > 0
                ? Math.min(100, (int) Math.round(completedLessonCount * 100.0D / totalLessonCount))
                : 0;
        vo.setCompletedLessonCount(completedLessonCount);
        vo.setProgress(progress);

        int studySeconds = courseProgress == null ? 0 : safeInt(courseProgress.getStudySeconds());
        vo.setStudySeconds(studySeconds);
        vo.setStudyStatus(courseProgress == null ? STUDY_STATUS_NOT_STARTED : safeInt(courseProgress.getStatus()));
        vo.setLastStudyTime(courseProgress == null ? null : courseProgress.getLastStudyTime());
        vo.setCurrentChapterIndex(resolveCurrentChapterIndex(chapterList, courseProgress));
    }

    private Integer resolveCurrentChapterIndex(List<CourseChapter> chapterList, CourseStudyProgress courseProgress) {
        if (chapterList == null || chapterList.isEmpty() || courseProgress == null
                || StringTools.isEmpty(courseProgress.getCurrentChapterId())) {
            return 0;
        }
        for (int index = 0; index < chapterList.size(); index++) {
            if (Objects.equals(chapterList.get(index).getChapterId(), courseProgress.getCurrentChapterId())) {
                return index + 1;
            }
        }
        return 0;
    }

    private Map<String, CourseStudyProgress> loadMergedCourseStudyProgressMap(Integer userId, Set<String> courseIdSet) {
        if (userId == null || courseIdSet == null || courseIdSet.isEmpty()) {
            return Map.of();
        }
        // 先从数据库批量取，再用 Redis 中的最新进度覆盖（reportStudyProgress 写缓存后还未刷库的场景）
        CourseStudyProgressQuery query = new CourseStudyProgressQuery();
        query.setUserId(userId);
        Map<String, CourseStudyProgress> result = courseStudyProgressService.findListByParam(query).stream()
                .filter(item -> courseIdSet.contains(item.getCourseId()))
                .collect(Collectors.toMap(CourseStudyProgress::getCourseId, item -> item, (item1, item2) -> item1, HashMap::new));
        for (String courseId : courseIdSet) {
            CourseStudyProgress cached = courseStudyProgressRedisComponent.getCourseProgress(userId, courseId);
            if (cached != null) {
                result.put(courseId, cached);
            }
        }
        return result;
    }

    private Map<String, Map<String, CourseStudyLessonProgress>> loadMergedLessonStudyProgressMapByCourse(
            Integer userId, Set<String> courseIdSet, Map<String, List<CourseChapterLesson>> lessonMap) {
        if (userId == null || courseIdSet == null || courseIdSet.isEmpty()) {
            return Map.of();
        }
        // 一次性按 userId 拉全部课时进度记录，避免多门课分别查询带来的 N 次数据库往返
        CourseStudyLessonProgressQuery query = new CourseStudyLessonProgressQuery();
        query.setUserId(userId);
        Map<String, Map<String, CourseStudyLessonProgress>> result = new HashMap<>();
        for (CourseStudyLessonProgress dbProgress : courseStudyLessonProgressService.findListByParam(query)) {
            if (!courseIdSet.contains(dbProgress.getCourseId())) {
                continue;
            }
            result.computeIfAbsent(dbProgress.getCourseId(), key -> new HashMap<>())
                    .put(dbProgress.getLessonId(), dbProgress);
        }
        // 再合并 Redis 中可能存在的最新进度，覆盖同 lessonId 数据库快照
        for (String courseId : courseIdSet) {
            List<CourseChapterLesson> courseLessonList = lessonMap.getOrDefault(courseId, List.of());
            if (courseLessonList.isEmpty()) {
                continue;
            }
            Map<String, CourseStudyLessonProgress> courseLessonProgressMap =
                    result.computeIfAbsent(courseId, key -> new HashMap<>());
            for (CourseChapterLesson lesson : courseLessonList) {
                CourseStudyLessonProgress cached = courseStudyProgressRedisComponent.getLessonProgress(userId, lesson.getLessonId());
                if (cached != null) {
                    courseLessonProgressMap.put(lesson.getLessonId(), cached);
                }
            }
        }
        return result;
    }

    private CourseDetailVO buildCourseDetail(CourseInfo courseInfo, List<CourseClass> matchedClassList, Integer userId) {
        CourseDetailVO vo = new CourseDetailVO();
        vo.setCourseId(courseInfo.getCourseId());
        vo.setCourseName(courseInfo.getCourseName());
        vo.setCoverResourceId(courseInfo.getCoverResourceId());
        vo.setDescription(courseInfo.getDescription());
        vo.setTeacherId(courseInfo.getTeacherId());
        vo.setRecordStatus(courseInfo.getRecordStatus());
        vo.setStatus(courseInfo.getStatus());
        vo.setCreateTime(courseInfo.getCreateTime());
        vo.setUpdateTime(courseInfo.getUpdateTime());

        Map<Integer, UserInfo> teacherMap = courseInfo.getTeacherId() == null
                ? Map.of()
                : loadTeacherMap(Set.of(courseInfo.getTeacherId()));
        Map<Integer, ResourceInfo> coverMap = courseInfo.getCoverResourceId() == null
                ? Map.of()
                : loadResourceMap(Set.of(courseInfo.getCoverResourceId()));
        vo.setTeacherName(resolveTeacherName(courseInfo.getTeacherId(), teacherMap));
        vo.setCoverPath(resolveCoverPath(courseInfo.getCoverResourceId(), coverMap));

        CourseStudyProgress studyProgress = getMergedCourseStudyProgress(userId, courseInfo.getCourseId());
        if (studyProgress != null) {
            vo.setCurrentChapterId(studyProgress.getCurrentChapterId());
            vo.setCurrentLessonId(studyProgress.getCurrentLessonId());
            vo.setStudySeconds(studyProgress.getStudySeconds());
            vo.setLastStudyTime(studyProgress.getLastStudyTime());
            vo.setStudyStatus(studyProgress.getStatus());
        } else {
            vo.setStudySeconds(0);
            vo.setStudyStatus(STUDY_STATUS_NOT_STARTED);
        }

        List<Integer> classIds = matchedClassList == null ? List.of() : matchedClassList.stream()
                .map(CourseClass::getClassId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        vo.setClassIdList(classIds);
        vo.setClassNames(joinClassNames(classIds, loadClassInfoMap(new LinkedHashSet<>(classIds))));
        vo.setIsCollected(isCourseCollected(userId, courseInfo.getCourseId()) ? 1 : 0);

        CourseChapterQuery chapterQuery = new CourseChapterQuery();
        chapterQuery.setCourseId(courseInfo.getCourseId());
        chapterQuery.setOrderBy(CHAPTER_ORDER_BY);
        List<CourseChapter> chapterList = courseChapterService.findListByParam(chapterQuery);

        CourseChapterLessonQuery lessonQuery = new CourseChapterLessonQuery();
        lessonQuery.setCourseId(courseInfo.getCourseId());
        lessonQuery.setOrderBy(LESSON_ORDER_BY);
        List<CourseChapterLesson> lessonList = courseChapterLessonService.findListByParam(lessonQuery);
        Map<String, CourseStudyLessonProgress> lessonStudyProgressMap = loadMergedLessonStudyProgressMap(userId, courseInfo.getCourseId());
        Map<String, List<CourseChapterLesson>> lessonMap = lessonList.stream()
                .collect(Collectors.groupingBy(CourseChapterLesson::getChapterId));
        Map<String, List<CourseChapterLessonResource>> lessonResourceMap = loadLessonResourceMap(lessonList);

        Set<Integer> lessonResourceIdSet = lessonResourceMap.values().stream()
                .flatMap(Collection::stream)
                .filter(resource -> Objects.equals(resource.getResourceType(), LESSON_RESOURCE_ROLE_VIDEO)
                        || Objects.equals(resource.getResourceType(), LESSON_RESOURCE_ROLE_COURSEWARE))
                .map(CourseChapterLessonResource::getResourceId)
                .map(this::parseResourceId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Integer, ResourceInfo> lessonResourceInfoMap = loadResourceMap(lessonResourceIdSet);

        Set<String> paperIdSet = lessonResourceMap.values().stream()
                .flatMap(Collection::stream)
                .filter(resource -> Objects.equals(resource.getResourceType(), LESSON_RESOURCE_ROLE_PAPER))
                .map(CourseChapterLessonResource::getResourceId)
                .map(StringTools::trim)
                .filter(resourceId -> !StringTools.isEmpty(resourceId))
                .collect(Collectors.toSet());
        Map<String, PaperInfo> lessonPaperMap = loadPaperMap(paperIdSet);

        List<CourseChapterDetailVO> chapterVOList = new ArrayList<>();
        for (CourseChapter chapter : chapterList) {
            CourseChapterDetailVO chapterVO = new CourseChapterDetailVO();
            chapterVO.setChapterId(chapter.getChapterId());
            chapterVO.setCourseId(chapter.getCourseId());
            chapterVO.setChapterName(chapter.getChapterName());
            chapterVO.setDescription(chapter.getDescription());
            chapterVO.setSortOrder(chapter.getSortOrder());

            List<CourseLessonDetailVO> lessonVOList = lessonMap.getOrDefault(chapter.getChapterId(), List.of())
                    .stream()
                    .sorted(Comparator.comparing(CourseChapterLesson::getSortOrder, Comparator.nullsLast(Integer::compareTo))
                            .thenComparing(CourseChapterLesson::getLessonId, Comparator.nullsLast(String::compareTo)))
                    .map(lesson -> buildLessonVO(lesson, lessonResourceMap, lessonResourceInfoMap, lessonPaperMap, lessonStudyProgressMap))
                    .toList();
            chapterVO.setLessonList(lessonVOList);
            chapterVOList.add(chapterVO);
        }
        vo.setChapterList(chapterVOList);
        return vo;
    }

    private CourseLessonDetailVO buildLessonVO(CourseChapterLesson lesson,
                                               Map<String, List<CourseChapterLessonResource>> lessonResourceMap,
                                               Map<Integer, ResourceInfo> resourceMap,
                                               Map<String, PaperInfo> paperMap,
                                               Map<String, CourseStudyLessonProgress> lessonStudyProgressMap) {
        CourseLessonDetailVO vo = new CourseLessonDetailVO();
        vo.setLessonId(lesson.getLessonId());
        vo.setLessonName(lesson.getLessonName());
        vo.setSortOrder(lesson.getSortOrder());
        List<CourseChapterLessonResource> resourceList = lessonResourceMap.getOrDefault(lesson.getLessonId(), List.of());
        List<CourseLessonResourceVO> coursewareList = new ArrayList<>();
        for (CourseChapterLessonResource lessonResource : resourceList) {
            if (Objects.equals(lessonResource.getResourceType(), LESSON_RESOURCE_ROLE_PAPER)) {
                String paperId = StringTools.trim(lessonResource.getResourceId());
                PaperInfo paperInfo = paperMap.get(paperId);
                vo.setPaperId(paperId);
                if (paperInfo != null) {
                    vo.setPaperName(paperInfo.getPaperName());
                    vo.setPaperType(paperInfo.getPaperType());
                    vo.setPaperTypeText(resolvePaperTypeText(paperInfo.getPaperType()));
                }
                continue;
            }

            Integer resourceId = parseResourceId(lessonResource.getResourceId());
            if (resourceId == null) {
                continue;
            }
            ResourceInfo resourceInfo = resourceMap.get(resourceId);
            if (resourceInfo == null) {
                continue;
            }
            if (Objects.equals(lessonResource.getResourceType(), LESSON_RESOURCE_ROLE_VIDEO)) {
                vo.setVideoResourceId(resourceInfo.getResourceId());
                vo.setVideoResourceName(resourceInfo.getResourceName());
                vo.setVideoFilePath(resourceInfo.getFilePath());
                vo.setVideoCoverPath(resourceInfo.getCoverPath());
                continue;
            }
            coursewareList.add(buildLessonResourceVO(resourceInfo));
        }
        vo.setCoursewareList(coursewareList);
        CourseStudyLessonProgress lessonProgress = lessonStudyProgressMap.get(lesson.getLessonId());
        if (lessonProgress != null) {
            vo.setStudySeconds(lessonProgress.getStudySeconds());
            vo.setLastPositionSeconds(lessonProgress.getLastPositionSeconds());
            vo.setMaxPositionSeconds(lessonProgress.getMaxPositionSeconds());
            vo.setVideoDurationSeconds(lessonProgress.getVideoDurationSeconds());
            vo.setIsCompleted(lessonProgress.getIsCompleted());
            vo.setLastStudyTime(lessonProgress.getLastStudyTime());
        } else {
            vo.setStudySeconds(0);
            vo.setLastPositionSeconds(0);
            vo.setMaxPositionSeconds(0);
            vo.setVideoDurationSeconds(0);
            vo.setIsCompleted(STUDY_STATUS_NOT_STARTED);
        }
        return vo;
    }

    private CourseLessonResourceVO buildLessonResourceVO(ResourceInfo resourceInfo) {
        CourseLessonResourceVO vo = new CourseLessonResourceVO();
        vo.setResourceId(resourceInfo.getResourceId());
        vo.setResourceType(resourceInfo.getResourceType());
        vo.setResourceName(resourceInfo.getResourceName());
        vo.setFilePath(resourceInfo.getFilePath());
        vo.setCoverPath(resourceInfo.getCoverPath());
        vo.setFileName(resourceInfo.getFileName());
        vo.setFileSuffix(resourceInfo.getFileSuffix());
        vo.setFileSize(resourceInfo.getFileSize());
        return vo;
    }

    private Map<String, List<CourseChapter>> loadCourseChapterMap(Set<String> courseIdSet) {
        if (courseIdSet.isEmpty()) {
            return Map.of();
        }
        CourseChapterQuery query = new CourseChapterQuery();
        query.setOrderBy(CHAPTER_ORDER_BY);
        return courseChapterService.findListByParam(query).stream()
                .filter(item -> courseIdSet.contains(item.getCourseId()))
                .collect(Collectors.groupingBy(CourseChapter::getCourseId));
    }

    private Set<String> loadCollectedCourseIdSet(Integer userId) {
        if (userId == null) {
            return Set.of();
        }
        CourseUserCollectionQuery query = new CourseUserCollectionQuery();
        query.setUserId(userId);
        return courseUserCollectionService.findListByParam(query).stream()
                .map(CourseUserCollection::getCourseId)
                .filter(courseId -> !StringTools.isEmpty(courseId))
                .collect(Collectors.toSet());
    }

    private boolean isCourseCollected(Integer userId, String courseId) {
        if (userId == null || StringTools.isEmpty(courseId)) {
            return false;
        }
        return courseUserCollectionService.getCourseUserCollectionByCourseIdAndUserId(courseId, userId) != null;
    }

    private Map<String, List<CourseChapterLesson>> loadCourseLessonMap(Set<String> courseIdSet) {
        if (courseIdSet.isEmpty()) {
            return Map.of();
        }
        CourseChapterLessonQuery query = new CourseChapterLessonQuery();
        query.setOrderBy(LESSON_ORDER_BY);
        return courseChapterLessonService.findListByParam(query).stream()
                .filter(item -> courseIdSet.contains(item.getCourseId()))
                .collect(Collectors.groupingBy(CourseChapterLesson::getCourseId));
    }

    private Map<String, List<CourseChapterLessonResource>> loadLessonResourceMap(List<CourseChapterLesson> lessonList) {
        if (lessonList == null || lessonList.isEmpty()) {
            return Map.of();
        }
        Map<String, List<CourseChapterLessonResource>> result = new HashMap<>();
        for (CourseChapterLesson lesson : lessonList) {
            if (StringTools.isEmpty(lesson.getLessonId())) {
                continue;
            }
            CourseChapterLessonResourceQuery query = new CourseChapterLessonResourceQuery();
            query.setLessonId(lesson.getLessonId());
            query.setOrderBy(LESSON_RESOURCE_ORDER_BY);
            List<CourseChapterLessonResource> resourceList = courseChapterLessonResourceService.findListByParam(query);
            if (resourceList != null && !resourceList.isEmpty()) {
                result.put(lesson.getLessonId(), resourceList);
            }
        }
        return result;
    }

    private Map<String, CourseStudyLessonProgress> loadLessonStudyProgressMap(Integer userId, String courseId) {
        if (userId == null || StringTools.isEmpty(courseId)) {
            return Map.of();
        }
        CourseStudyLessonProgressQuery query = new CourseStudyLessonProgressQuery();
        query.setUserId(userId);
        query.setCourseId(courseId);
        return courseStudyLessonProgressService.findListByParam(query).stream()
                .collect(Collectors.toMap(CourseStudyLessonProgress::getLessonId, item -> item, (item1, item2) -> item1, HashMap::new));
    }

    private Map<String, CourseStudyLessonProgress> loadMergedLessonStudyProgressMap(Integer userId, String courseId) {
        Map<String, CourseStudyLessonProgress> result = new HashMap<>(loadLessonStudyProgressMap(userId, courseId));
        Set<String> lessonIdSet = result.keySet().stream().collect(Collectors.toSet());
        CourseChapterLessonQuery lessonQuery = new CourseChapterLessonQuery();
        lessonQuery.setCourseId(courseId);
        List<CourseChapterLesson> lessonList = courseChapterLessonService.findListByParam(lessonQuery);
        for (CourseChapterLesson lesson : lessonList) {
            lessonIdSet.add(lesson.getLessonId());
        }
        for (String lessonId : lessonIdSet) {
            CourseStudyLessonProgress cachedProgress = courseStudyProgressRedisComponent.getLessonProgress(userId, lessonId);
            if (cachedProgress != null) {
                result.put(lessonId, cachedProgress);
            }
        }
        return result;
    }

    private Map<Integer, UserInfo> loadTeacherMap(Set<Integer> teacherIdSet) {
        if (teacherIdSet == null || teacherIdSet.isEmpty()) {
            return Map.of();
        }
        return userInfoService.getUserInfoByUserIdList(new ArrayList<>(teacherIdSet)).stream()
                .collect(Collectors.toMap(UserInfo::getUserId, item -> item, (item1, item2) -> item1, HashMap::new));
    }

    private Map<Integer, ClassInfo> loadClassInfoMap(Set<Integer> classIdSet) {
        if (classIdSet == null || classIdSet.isEmpty()) {
            return Map.of();
        }
        return classInfoService.getClassInfoByClassIdList(new ArrayList<>(classIdSet)).stream()
                .collect(Collectors.toMap(ClassInfo::getClassId, item -> item, (item1, item2) -> item1, HashMap::new));
    }

    private Map<Integer, ResourceInfo> loadResourceMap(Set<Integer> resourceIdSet) {
        if (resourceIdSet == null || resourceIdSet.isEmpty()) {
            return Map.of();
        }
        List<String> resourceIdList = resourceIdSet.stream()
                .filter(Objects::nonNull)
                .map(String::valueOf)
                .toList();
        return resourceInfoService.getResourceInfoByResourceIdList(resourceIdList).stream()
                .collect(Collectors.toMap(ResourceInfo::getResourceId, item -> item, (item1, item2) -> item1, HashMap::new));
    }

    private Map<String, PaperInfo> loadPaperMap(Set<String> paperIdSet) {
        if (paperIdSet == null || paperIdSet.isEmpty()) {
            return Map.of();
        }
        Map<String, PaperInfo> result = new HashMap<>();
        for (String paperId : paperIdSet) {
            PaperInfo paperInfo = paperInfoService.getPaperInfoByPaperId(paperId);
            if (paperInfo != null) {
                result.put(paperId, paperInfo);
            }
        }
        return result;
    }

    private Integer parseResourceId(String resourceId) {
        String value = StringTools.trim(resourceId);
        if (StringTools.isEmpty(value)) {
            return null;
        }
        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private String resolveTeacherName(Integer teacherId, Map<Integer, UserInfo> teacherMap) {
        UserInfo teacher = teacherMap.get(teacherId);
        return teacher == null ? "-" : teacher.getRealName();
    }

    private CourseStudyProgress getMergedCourseStudyProgress(Integer userId, String courseId) {
        if (userId == null || StringTools.isEmpty(courseId)) {
            return null;
        }
        CourseStudyProgress cachedProgress = courseStudyProgressRedisComponent.getCourseProgress(userId, courseId);
        if (cachedProgress != null) {
            return cachedProgress;
        }
        return courseStudyProgressService.getCourseStudyProgressByUserIdAndCourseId(userId, courseId);
    }

    private CourseStudyLessonProgress getMergedLessonStudyProgress(Integer userId, String lessonId) {
        if (userId == null || StringTools.isEmpty(lessonId)) {
            return null;
        }
        CourseStudyLessonProgress cachedProgress = courseStudyProgressRedisComponent.getLessonProgress(userId, lessonId);
        if (cachedProgress != null) {
            return cachedProgress;
        }
        return courseStudyLessonProgressService.getCourseStudyLessonProgressByUserIdAndLessonId(userId, lessonId);
    }

    private List<CourseClass> ensureStudentCanAccessCourse(UserInfo currentStudent, String courseId) {
        List<Integer> classIdList = parseClassIds(currentStudent.getClassId());
        if (classIdList.isEmpty()) {
            throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "当前学生尚未分配班级");
        }
        Map<String, List<CourseClass>> matchedCourseClassMap = loadMatchedCourseClassMap(classIdList);
        List<CourseClass> matchedClassList = matchedCourseClassMap.get(courseId);
        if (matchedClassList == null || matchedClassList.isEmpty()) {
            throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "当前课程不在学生可学习范围内");
        }
        return matchedClassList;
    }

    private int countCourseLesson(String courseId) {
        CourseChapterLessonQuery query = new CourseChapterLessonQuery();
        query.setCourseId(courseId);
        return courseChapterLessonService.findCountByParam(query);
    }

    private int countCompletedLesson(Map<String, CourseStudyLessonProgress> lessonProgressMap) {
        return (int) lessonProgressMap.values().stream()
                .filter(item -> Objects.equals(item.getIsCompleted(), BOOLEAN_YES))
                .count();
    }

    private Integer resolveCourseStudyStatus(int totalLessonCount, int completedLessonCount, Integer studySeconds) {
        if (totalLessonCount > 0 && completedLessonCount >= totalLessonCount) {
            return STUDY_STATUS_COMPLETED;
        }
        if (safeInt(studySeconds) > 0 || completedLessonCount > 0) {
            return STUDY_STATUS_STUDYING;
        }
        return STUDY_STATUS_NOT_STARTED;
    }

    private boolean shouldMarkLessonCompleted(boolean forceComplete, int maxPositionSeconds, int durationSeconds, Integer existingCompleted) {
        if (Objects.equals(existingCompleted, BOOLEAN_YES) || forceComplete) {
            return true;
        }
        if (durationSeconds <= 0) {
            return false;
        }
        return maxPositionSeconds >= Math.round(durationSeconds * LESSON_COMPLETION_THRESHOLD);
    }

    private int safeAdd(Integer value, int increment) {
        return Math.max(0, safeInt(value) + Math.max(0, increment));
    }

    private int safeInt(Integer value) {
        return value == null ? 0 : value;
    }

    private CourseStudyReportVO buildCourseStudyReportVO(CourseStudyProgress courseProgress, CourseStudyLessonProgress lessonProgress) {
        CourseStudyReportVO vo = new CourseStudyReportVO();
        if (courseProgress != null) {
            vo.setCourseId(courseProgress.getCourseId());
            vo.setCourseStudySeconds(courseProgress.getStudySeconds());
            vo.setCourseStatus(courseProgress.getStatus());
            vo.setCurrentChapterId(courseProgress.getCurrentChapterId());
            vo.setCurrentLessonId(courseProgress.getCurrentLessonId());
            vo.setCourseLastStudyTime(courseProgress.getLastStudyTime());
        }
        if (lessonProgress != null) {
            vo.setChapterId(lessonProgress.getChapterId());
            vo.setLessonId(lessonProgress.getLessonId());
            vo.setLessonStudySeconds(lessonProgress.getStudySeconds());
            vo.setLastPositionSeconds(lessonProgress.getLastPositionSeconds());
            vo.setMaxPositionSeconds(lessonProgress.getMaxPositionSeconds());
            vo.setVideoDurationSeconds(lessonProgress.getVideoDurationSeconds());
            vo.setIsCompleted(lessonProgress.getIsCompleted());
            vo.setLessonLastStudyTime(lessonProgress.getLastStudyTime());
        }
        return vo;
    }

    private void flushCachedLessonProgressToDb() {
        for (String member : courseStudyProgressRedisComponent.getDirtyLessonMembers()) {
            String[] values = splitDirtyMember(member);
            if (values == null) {
                courseStudyProgressRedisComponent.removeDirtyLessonMember(member);
                continue;
            }
            Integer userId = parseInteger(values[0]);
            String lessonId = values[1];
            if (userId == null || StringTools.isEmpty(lessonId)) {
                courseStudyProgressRedisComponent.removeDirtyLessonMember(member);
                continue;
            }
            CourseStudyLessonProgress cachedProgress = courseStudyProgressRedisComponent.getLessonProgress(userId, lessonId);
            if (cachedProgress == null) {
                courseStudyProgressRedisComponent.removeDirtyLessonMember(member);
                continue;
            }
            if (!validateLessonProgressForFlush(userId, cachedProgress)) {
                courseStudyProgressRedisComponent.removeDirtyLessonMember(member);
                continue;
            }
            CourseStudyLessonProgress dbProgress = courseStudyLessonProgressService.getCourseStudyLessonProgressByUserIdAndLessonId(userId, lessonId);
            if (dbProgress == null) {
                courseStudyLessonProgressService.add(cachedProgress);
            } else {
                courseStudyLessonProgressService.updateCourseStudyLessonProgressByUserIdAndLessonId(cachedProgress, userId, lessonId);
            }
            courseStudyProgressRedisComponent.removeDirtyLessonMember(member);
        }
    }

    private void flushCachedCourseProgressToDb() {
        for (String member : courseStudyProgressRedisComponent.getDirtyCourseMembers()) {
            String[] values = splitDirtyMember(member);
            if (values == null) {
                courseStudyProgressRedisComponent.removeDirtyCourseMember(member);
                continue;
            }
            Integer userId = parseInteger(values[0]);
            String courseId = values[1];
            if (userId == null || StringTools.isEmpty(courseId)) {
                courseStudyProgressRedisComponent.removeDirtyCourseMember(member);
                continue;
            }
            CourseStudyProgress cachedProgress = courseStudyProgressRedisComponent.getCourseProgress(userId, courseId);
            if (cachedProgress == null) {
                courseStudyProgressRedisComponent.removeDirtyCourseMember(member);
                continue;
            }
            if (!validateCourseProgressForFlush(userId, cachedProgress)) {
                courseStudyProgressRedisComponent.removeDirtyCourseMember(member);
                continue;
            }
            CourseStudyProgress dbProgress = courseStudyProgressService.getCourseStudyProgressByUserIdAndCourseId(userId, courseId);
            if (dbProgress == null) {
                courseStudyProgressService.add(cachedProgress);
            } else {
                courseStudyProgressService.updateCourseStudyProgressByUserIdAndCourseId(cachedProgress, userId, courseId);
            }
            courseStudyProgressRedisComponent.removeDirtyCourseMember(member);
        }
    }

    private String[] splitDirtyMember(String member) {
        if (StringTools.isEmpty(member) || !member.contains(":")) {
            return null;
        }
        String[] values = member.split(":", 2);
        return values.length == 2 ? values : null;
    }

    private Integer parseInteger(String value) {
        try {
            return Integer.valueOf(StringTools.trim(value));
        } catch (Exception exception) {
            return null;
        }
    }

    private boolean validateLessonProgressForFlush(Integer userId, CourseStudyLessonProgress cachedProgress) {
        if (userId == null || cachedProgress == null || StringTools.isEmpty(cachedProgress.getCourseId())
                || StringTools.isEmpty(cachedProgress.getChapterId()) || StringTools.isEmpty(cachedProgress.getLessonId())) {
            return false;
        }
        UserInfo userInfo = userInfoService.getUserInfoByUserId(userId);
        if (userInfo == null) {
            return false;
        }
        List<CourseClass> matchedClassList;
        try {
            matchedClassList = ensureStudentCanAccessCourse(userInfo, cachedProgress.getCourseId());
        } catch (BusinessException exception) {
            return false;
        }
        if (matchedClassList.isEmpty()) {
            return false;
        }
        CourseChapter chapter = courseChapterService.getCourseChapterByChapterId(cachedProgress.getChapterId());
        if (chapter == null || !cachedProgress.getCourseId().equals(chapter.getCourseId())) {
            return false;
        }
        CourseChapterLesson lesson = courseChapterLessonService.getCourseChapterLessonByLessonId(cachedProgress.getLessonId());
        return lesson != null
                && cachedProgress.getCourseId().equals(lesson.getCourseId())
                && cachedProgress.getChapterId().equals(lesson.getChapterId());
    }

    private boolean validateCourseProgressForFlush(Integer userId, CourseStudyProgress cachedProgress) {
        if (userId == null || cachedProgress == null || StringTools.isEmpty(cachedProgress.getCourseId())) {
            return false;
        }
        UserInfo userInfo = userInfoService.getUserInfoByUserId(userId);
        if (userInfo == null) {
            return false;
        }
        try {
            ensureStudentCanAccessCourse(userInfo, cachedProgress.getCourseId());
        } catch (BusinessException exception) {
            return false;
        }
        if (!StringTools.isEmpty(cachedProgress.getCurrentChapterId())) {
            CourseChapter chapter = courseChapterService.getCourseChapterByChapterId(cachedProgress.getCurrentChapterId());
            if (chapter == null || !cachedProgress.getCourseId().equals(chapter.getCourseId())) {
                return false;
            }
        }
        if (!StringTools.isEmpty(cachedProgress.getCurrentLessonId())) {
            CourseChapterLesson lesson = courseChapterLessonService.getCourseChapterLessonByLessonId(cachedProgress.getCurrentLessonId());
            if (lesson == null || !cachedProgress.getCourseId().equals(lesson.getCourseId())) {
                return false;
            }
        }
        return true;
    }

    private String resolveCoverPath(Integer coverResourceId, Map<Integer, ResourceInfo> coverMap) {
        if (coverResourceId == null) {
            return "";
        }
        ResourceInfo resourceInfo = coverMap.get(coverResourceId);
        if (resourceInfo == null) {
            return "";
        }
        return StringTools.isEmpty(resourceInfo.getCoverPath()) ? resourceInfo.getFilePath() : resourceInfo.getCoverPath();
    }

    private String joinClassNames(List<Integer> classIds, Map<Integer, ClassInfo> classInfoMap) {
        return classIds.stream()
                .map(classInfoMap::get)
                .filter(Objects::nonNull)
                .map(ClassInfo::getClassName)
                .filter(name -> !StringTools.isEmpty(name))
                .collect(Collectors.joining("、"));
    }

    private String resolvePaperTypeText(Integer paperType) {
        return switch (paperType == null ? 0 : paperType) {
            case PAPER_TYPE_HOMEWORK -> "课后习题";
            case PAPER_TYPE_EXAM -> "考试试卷";
            default -> "未知";
        };
    }
}
