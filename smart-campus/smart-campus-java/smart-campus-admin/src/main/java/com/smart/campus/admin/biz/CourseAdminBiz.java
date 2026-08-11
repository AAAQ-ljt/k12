package com.smart.campus.admin.biz;

import com.smart.campus.admin.biz.support.MessagePublishAdminSupport;
import com.smart.campus.entity.dto.CourseChapterSaveDTO;
import com.smart.campus.entity.dto.CourseLessonSaveDTO;
import com.smart.campus.entity.dto.CourseSaveDTO;
import com.smart.campus.entity.dto.CourseStructureSaveDTO;
import com.smart.campus.entity.enums.ResponseCodeEnum;
import com.smart.campus.entity.enums.UserRoleTypeEnum;
import com.smart.campus.entity.po.*;
import com.smart.campus.entity.query.*;
import com.smart.campus.entity.vo.*;
import com.smart.campus.exception.BusinessException;
import com.smart.campus.service.*;
import com.smart.campus.utils.LoginUserContextHolder;
import com.smart.campus.utils.StringTools;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CourseAdminBiz {

    private static final int DEFAULT_PAGE_NO = 1;
    private static final int DEFAULT_PAGE_SIZE = 15;
    private static final int DEFAULT_RECORD_STATUS = 0;
    private static final int DEFAULT_STATUS = 1;
    private static final String COURSE_ORDER_BY = "c.update_time desc,c.create_time desc";
    private static final String CHAPTER_ORDER_BY = "c.sort_order asc,c.chapter_id asc";
    private static final String LESSON_ORDER_BY = "c.sort_order asc,c.lesson_id asc";
    private static final String LESSON_RESOURCE_ORDER_BY = "c.is_primary desc,c.sort_order asc,c.id asc";
    private static final int LESSON_RESOURCE_ROLE_VIDEO = 1;
    private static final int LESSON_RESOURCE_ROLE_COURSEWARE = 2;
    private static final int LESSON_RESOURCE_ROLE_PAPER = 3;
    private static final int PAPER_TYPE_HOMEWORK = 1;
    private static final int PAPER_TYPE_EXAM = 2;

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
    private UserInfoService userInfoService;

    @Resource
    private ClassInfoService classInfoService;

    @Resource
    private ResourceInfoService resourceInfoService;

    @Resource
    private PaperInfoService paperInfoService;

    @Resource
    private MessagePublishAdminSupport messagePublishAdminSupport;

    public PaginationResultVO<CourseListItemVO> loadDataList(CourseInfoQuery query) {
        CourseInfoQuery request = buildPageQuery(query);
        LoginUserVO loginUser = LoginUserContextHolder.get();
        if (loginUser != null && UserRoleTypeEnum.TEACHER.getCode().equals(loginUser.getRoleType())) {
            request.setTeacherId(loginUser.getUserId());
        }
        PaginationResultVO<CourseInfo> pageResult = courseInfoService.findListByPage(request);
        List<CourseListItemVO> list = buildCourseList(pageResult.getList());
        return new PaginationResultVO<>(
                pageResult.getTotalCount(),
                pageResult.getPageSize(),
                pageResult.getPageNo(),
                pageResult.getPageTotal(),
                list
        );
    }

    public CourseDetailVO getCourseInfoById(String courseId) {
        CourseInfo courseInfo = courseInfoService.getCourseInfoByCourseId(courseId);
        if (courseInfo == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "课程信息不存在");
        }
        return buildCourseDetail(courseInfo);
    }

    @Transactional(rollbackFor = Exception.class)
    public CourseDetailVO add(CourseSaveDTO dto) {
        CourseSaveDTO request = normalizeSaveDTO(dto);
        LoginUserVO loginUser = getCurrentTeacherLoginUser();
        request.setTeacherId(loginUser.getUserId());
        validateCoverResource(request.getCoverResourceId());
        List<Integer> classIdList = normalizeClassIds(request.getClassIdList());
        validateClasses(classIdList);

        CourseInfo courseInfo = buildCourseInfo(request, generateStringId(), null);
        courseInfoService.add(courseInfo);
        syncCourseClasses(courseInfo.getCourseId(), classIdList);
        return buildCourseDetail(courseInfoService.getCourseInfoByCourseId(courseInfo.getCourseId()));
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateCourseInfoById(CourseSaveDTO dto) {
        CourseSaveDTO request = normalizeSaveDTO(dto);
        CourseInfo original = courseInfoService.getCourseInfoByCourseId(request.getCourseId());
        if (original == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "课程信息不存在");
        }
        LoginUserVO loginUser = getCurrentTeacherLoginUser();
        checkTeacherOwnership(original, loginUser.getUserId());
        request.setTeacherId(loginUser.getUserId());
        validateCoverResource(request.getCoverResourceId());
        List<Integer> classIdList = normalizeClassIds(request.getClassIdList());
        validateClasses(classIdList);

        CourseInfo courseInfo = buildCourseInfo(request, original.getCourseId(), original);
        courseInfoService.updateCourseInfoByCourseId(courseInfo, original.getCourseId());
        syncCourseClasses(original.getCourseId(), classIdList);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteCourseInfoById(String courseId) {
        CourseInfo original = courseInfoService.getCourseInfoByCourseId(courseId);
        if (original == null) {
            return;
        }
        LoginUserVO loginUser = LoginUserContextHolder.get();
        if (loginUser != null && UserRoleTypeEnum.TEACHER.getCode().equals(loginUser.getRoleType())) {
            checkTeacherOwnership(original, loginUser.getUserId());
        }
        deleteCourseRelations(courseId);
        courseInfoService.deleteCourseInfoByCourseId(courseId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteBatch(String ids) {
        List<String> courseIdList = parseStringIds(ids);
        for (String courseId : courseIdList) {
            deleteCourseInfoById(courseId);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void finishRecord(String courseId) {
        String normalizedCourseId = StringTools.trim(courseId);
        if (StringTools.isEmpty(normalizedCourseId)) {
            throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "课程ID不能为空");
        }
        CourseInfo original = courseInfoService.getCourseInfoByCourseId(normalizedCourseId);
        if (original == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "课程信息不存在");
        }
        LoginUserVO loginUser = getCurrentTeacherLoginUser();
        checkTeacherOwnership(original, loginUser.getUserId());
        if (Objects.equals(original.getRecordStatus(), 1)) {
            return;
        }
        CourseInfo update = new CourseInfo();
        update.setRecordStatus(1);
        courseInfoService.updateCourseInfoByCourseId(update, normalizedCourseId);
        messagePublishAdminSupport.sendCoursePublishMessage(original, loadCourseClassIdList(normalizedCourseId), loginUser);
    }

    @Transactional(rollbackFor = Exception.class)
    public void saveCourseStructure(CourseStructureSaveDTO dto) {
        String courseId = StringTools.trim(dto.getCourseId());
        CourseInfo original = courseInfoService.getCourseInfoByCourseId(courseId);
        if (original == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "课程信息不存在");
        }
        LoginUserVO loginUser = getCurrentTeacherLoginUser();
        checkTeacherOwnership(original, loginUser.getUserId());
        validateCourseEditable(original);
        syncCourseStructure(courseId, dto.getChapterList());
    }

    private PaginationResultVO<CourseListItemVO> emptyPage(PaginationResultVO<CourseInfo> pageResult) {
        return new PaginationResultVO<>(
                pageResult.getTotalCount(),
                pageResult.getPageSize(),
                pageResult.getPageNo(),
                pageResult.getPageTotal(),
                List.of()
        );
    }

    private CourseInfoQuery buildPageQuery(CourseInfoQuery query) {
        CourseInfoQuery request = query == null ? new CourseInfoQuery() : query;
        if (request.getPageNo() == null || request.getPageNo() < 1) {
            request.setPageNo(DEFAULT_PAGE_NO);
        }
        if (request.getPageSize() == null || request.getPageSize() < 1) {
            request.setPageSize(DEFAULT_PAGE_SIZE);
        }
        request.setOrderBy(COURSE_ORDER_BY);
        return request;
    }

    private List<CourseListItemVO> buildCourseList(List<CourseInfo> courseList) {
        if (courseList == null || courseList.isEmpty()) {
            return List.of();
        }
        Set<String> courseIdSet = courseList.stream()
                .map(CourseInfo::getCourseId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<String, List<CourseClass>> courseClassMap = loadCourseClassMap(courseIdSet);
        Map<String, List<CourseChapter>> chapterMap = loadCourseChapterMap(courseIdSet);
        Map<String, List<CourseChapterLesson>> lessonMap = loadCourseLessonMap(courseIdSet);

        Set<Integer> teacherIdSet = courseList.stream()
                .map(CourseInfo::getTeacherId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Set<Integer> coverIdSet = courseList.stream()
                .map(CourseInfo::getCoverResourceId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Set<Integer> classIdSet = courseClassMap.values().stream()
                .flatMap(Collection::stream)
                .map(CourseClass::getClassId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Integer, UserInfo> teacherMap = loadTeacherMap(teacherIdSet);
        Map<Integer, ResourceInfo> coverMap = loadCoverResourceMap(coverIdSet);
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

            List<CourseClass> classList = courseClassMap.getOrDefault(item.getCourseId(), List.of());
            List<Integer> classIds = classList.stream()
                    .map(CourseClass::getClassId)
                    .filter(Objects::nonNull)
                    .toList();
            vo.setClassCount(classIds.size());
            vo.setClassNames(classIds.stream()
                    .map(classInfoMap::get)
                    .filter(Objects::nonNull)
                    .map(ClassInfo::getClassName)
                    .filter(name -> !StringTools.isEmpty(name))
                    .collect(Collectors.joining("、")));

            List<CourseChapter> chapterList = chapterMap.getOrDefault(item.getCourseId(), List.of());
            List<CourseChapterLesson> lessonList = lessonMap.getOrDefault(item.getCourseId(), List.of());
            vo.setChapterCount(chapterList.size());
            vo.setLessonCount(lessonList.size());
            result.add(vo);
        }
        return result;
    }

    private CourseDetailVO buildCourseDetail(CourseInfo courseInfo) {
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
                : loadCoverResourceMap(Set.of(courseInfo.getCoverResourceId()));
        vo.setTeacherName(resolveTeacherName(courseInfo.getTeacherId(), teacherMap));
        vo.setCoverPath(resolveCoverPath(courseInfo.getCoverResourceId(), coverMap));

        CourseClassQuery classQuery = new CourseClassQuery();
        classQuery.setCourseId(courseInfo.getCourseId());
        classQuery.setOrderBy("c.class_id asc");
        List<CourseClass> classList = courseClassService.findListByParam(classQuery);
        vo.setClassIdList(classList.stream()
                .map(CourseClass::getClassId)
                .filter(Objects::nonNull)
                .toList());

        CourseChapterQuery chapterQuery = new CourseChapterQuery();
        chapterQuery.setCourseId(courseInfo.getCourseId());
        chapterQuery.setOrderBy(CHAPTER_ORDER_BY);
        List<CourseChapter> chapterList = courseChapterService.findListByParam(chapterQuery);

        CourseChapterLessonQuery lessonQuery = new CourseChapterLessonQuery();
        lessonQuery.setCourseId(courseInfo.getCourseId());
        lessonQuery.setOrderBy(LESSON_ORDER_BY);
        List<CourseChapterLesson> lessonList = courseChapterLessonService.findListByParam(lessonQuery);
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
                    .map(lesson -> buildLessonVO(lesson, lessonResourceMap, lessonResourceInfoMap, lessonPaperMap))
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
                                               Map<String, PaperInfo> paperMap) {
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
        return vo;
    }

    private CourseLessonResourceVO buildLessonResourceVO(ResourceInfo resourceInfo) {
        CourseLessonResourceVO vo = new CourseLessonResourceVO();
        vo.setResourceId(resourceInfo.getResourceId());
        vo.setResourceType(resourceInfo.getResourceType());
        vo.setResourceName(resourceInfo.getResourceName());
        vo.setFilePath(resourceInfo.getFilePath());
        vo.setCoverPath(resourceInfo.getCoverPath());
        return vo;
    }

    private void syncCourseClasses(String courseId, List<Integer> classIdList) {
        CourseClassQuery deleteQuery = new CourseClassQuery();
        deleteQuery.setCourseId(courseId);
        courseClassService.deleteByParam(deleteQuery);
        if (classIdList.isEmpty()) {
            return;
        }
        List<CourseClass> saveList = classIdList.stream().map(classId -> {
            CourseClass item = new CourseClass();
            item.setCourseId(courseId);
            item.setClassId(classId);
            return item;
        }).toList();
        courseClassService.addBatch(saveList);
    }

    private List<Integer> loadCourseClassIdList(String courseId) {
        CourseClassQuery classQuery = new CourseClassQuery();
        classQuery.setCourseId(courseId);
        classQuery.setOrderBy("c.class_id asc");
        return courseClassService.findListByParam(classQuery).stream()
                .map(CourseClass::getClassId)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private void syncLessonResources(String lessonId,
                                     Integer videoResourceId,
                                     List<Integer> coursewareResourceIdList,
                                     String paperId) {
        deleteLessonResources(lessonId);
        ResourceInfo videoResource = validateVideoResource(videoResourceId);
        CourseChapterLessonResource videoRelation = new CourseChapterLessonResource();
        videoRelation.setLessonId(lessonId);
        videoRelation.setResourceId(String.valueOf(videoResource.getResourceId()));
        videoRelation.setResourceType(LESSON_RESOURCE_ROLE_VIDEO);
        videoRelation.setIsPrimary(1);
        videoRelation.setSortOrder(1);
        courseChapterLessonResourceService.add(videoRelation);

        List<Integer> normalizedCoursewareIdList = normalizeResourceIds(coursewareResourceIdList);
        for (int index = 0; index < normalizedCoursewareIdList.size(); index++) {
            Integer coursewareId = normalizedCoursewareIdList.get(index);
            ResourceInfo coursewareResource = validateCoursewareResource(coursewareId);
            CourseChapterLessonResource coursewareRelation = new CourseChapterLessonResource();
            coursewareRelation.setLessonId(lessonId);
            coursewareRelation.setResourceId(String.valueOf(coursewareResource.getResourceId()));
            coursewareRelation.setResourceType(LESSON_RESOURCE_ROLE_COURSEWARE);
            coursewareRelation.setIsPrimary(0);
            coursewareRelation.setSortOrder(index + 1);
            courseChapterLessonResourceService.add(coursewareRelation);
        }

        PaperInfo paperInfo = validatePaper(paperId);
        if (paperInfo != null) {
            CourseChapterLessonResource paperRelation = new CourseChapterLessonResource();
            paperRelation.setLessonId(lessonId);
            paperRelation.setResourceId(paperInfo.getPaperId());
            paperRelation.setResourceType(LESSON_RESOURCE_ROLE_PAPER);
            paperRelation.setIsPrimary(0);
            paperRelation.setSortOrder(normalizedCoursewareIdList.size() + 1);
            courseChapterLessonResourceService.add(paperRelation);
        }
    }

    private void deleteLessonResources(String lessonId) {
        if (StringTools.isEmpty(lessonId)) {
            return;
        }
        CourseChapterLessonResourceQuery query = new CourseChapterLessonResourceQuery();
        query.setLessonId(lessonId);
        courseChapterLessonResourceService.deleteByParam(query);
    }

    private void syncCourseStructure(String courseId, List<CourseChapterSaveDTO> chapterList) {
        List<CourseChapterSaveDTO> normalizedChapterList = normalizeChapterList(chapterList);

        CourseChapterQuery chapterQuery = new CourseChapterQuery();
        chapterQuery.setCourseId(courseId);
        List<CourseChapter> existingChapterList = courseChapterService.findListByParam(chapterQuery);
        Map<String, CourseChapter> existingChapterMap = existingChapterList.stream()
                .collect(Collectors.toMap(CourseChapter::getChapterId, item -> item));

        CourseChapterLessonQuery lessonQuery = new CourseChapterLessonQuery();
        lessonQuery.setCourseId(courseId);
        List<CourseChapterLesson> existingLessonList = courseChapterLessonService.findListByParam(lessonQuery);
        Map<String, CourseChapterLesson> existingLessonMap = existingLessonList.stream()
                .collect(Collectors.toMap(CourseChapterLesson::getLessonId, item -> item));

        Set<String> submittedChapterIds = normalizedChapterList.stream()
                .map(CourseChapterSaveDTO::getChapterId)
                .filter(id -> !StringTools.isEmpty(id))
                .collect(Collectors.toSet());
        Set<String> submittedLessonIds = normalizedChapterList.stream()
                .flatMap(chapter -> chapter.getLessonList().stream())
                .map(CourseLessonSaveDTO::getLessonId)
                .filter(id -> !StringTools.isEmpty(id))
                .collect(Collectors.toSet());

        for (CourseChapterLesson item : existingLessonList) {
            if (!submittedLessonIds.contains(item.getLessonId())) {
                deleteLessonResources(item.getLessonId());
                courseChapterLessonService.deleteCourseChapterLessonByLessonId(item.getLessonId());
            }
        }

        for (CourseChapter item : existingChapterList) {
            if (!submittedChapterIds.contains(item.getChapterId())) {
                CourseChapterLessonQuery deleteLessonQuery = new CourseChapterLessonQuery();
                deleteLessonQuery.setChapterId(item.getChapterId());
                List<CourseChapterLesson> chapterLessonList = courseChapterLessonService.findListByParam(deleteLessonQuery);
                for (CourseChapterLesson lesson : chapterLessonList) {
                    deleteLessonResources(lesson.getLessonId());
                }
                courseChapterLessonService.deleteByParam(deleteLessonQuery);
                courseChapterService.deleteCourseChapterByChapterId(item.getChapterId());
            }
        }

        for (CourseChapterSaveDTO chapterDTO : normalizedChapterList) {
            if (StringTools.isEmpty(chapterDTO.getChapterId())) {
                chapterDTO.setChapterId(generateStringId());
            }
            CourseChapter chapter = new CourseChapter();
            chapter.setChapterId(chapterDTO.getChapterId());
            chapter.setCourseId(courseId);
            chapter.setChapterName(chapterDTO.getChapterName());
            chapter.setDescription(chapterDTO.getDescription());
            chapter.setSortOrder(chapterDTO.getSortOrder());
            if (existingChapterMap.containsKey(chapterDTO.getChapterId())) {
                courseChapterService.updateCourseChapterByChapterId(chapter, chapterDTO.getChapterId());
            } else {
                courseChapterService.add(chapter);
            }

            for (CourseLessonSaveDTO lessonDTO : chapterDTO.getLessonList()) {
                if (StringTools.isEmpty(lessonDTO.getLessonId())) {
                    lessonDTO.setLessonId(generateStringId());
                }
                CourseChapterLesson lesson = new CourseChapterLesson();
                lesson.setLessonId(lessonDTO.getLessonId());
                lesson.setCourseId(courseId);
                lesson.setChapterId(chapterDTO.getChapterId());
                lesson.setLessonName(lessonDTO.getLessonName());
                lesson.setSortOrder(lessonDTO.getSortOrder());
                if (existingLessonMap.containsKey(lessonDTO.getLessonId())) {
                    courseChapterLessonService.updateCourseChapterLessonByLessonId(lesson, lessonDTO.getLessonId());
                } else {
                    courseChapterLessonService.add(lesson);
                }
                syncLessonResources(
                        lessonDTO.getLessonId(),
                        lessonDTO.getVideoResourceId(),
                        lessonDTO.getCoursewareResourceIdList(),
                        lessonDTO.getPaperId()
                );
            }
        }
    }

    private void deleteCourseRelations(String courseId) {
        CourseClassQuery classQuery = new CourseClassQuery();
        classQuery.setCourseId(courseId);
        courseClassService.deleteByParam(classQuery);

        CourseChapterLessonQuery lessonQuery = new CourseChapterLessonQuery();
        lessonQuery.setCourseId(courseId);
        List<CourseChapterLesson> lessonList = courseChapterLessonService.findListByParam(lessonQuery);
        for (CourseChapterLesson lesson : lessonList) {
            deleteLessonResources(lesson.getLessonId());
        }
        courseChapterLessonService.deleteByParam(lessonQuery);

        CourseChapterQuery chapterQuery = new CourseChapterQuery();
        chapterQuery.setCourseId(courseId);
        courseChapterService.deleteByParam(chapterQuery);
    }

    private CourseSaveDTO normalizeSaveDTO(CourseSaveDTO dto) {
        CourseSaveDTO request = dto == null ? new CourseSaveDTO() : dto;
        request.setCourseId(StringTools.trim(request.getCourseId()));
        request.setCourseName(StringTools.trim(request.getCourseName()));
        request.setDescription(StringTools.trim(request.getDescription()));
        if (request.getRecordStatus() == null) {
            request.setRecordStatus(DEFAULT_RECORD_STATUS);
        }
        if (request.getStatus() == null) {
            request.setStatus(DEFAULT_STATUS);
        }
        if (request.getClassIdList() == null) {
            request.setClassIdList(new ArrayList<>());
        }
        if (request.getChapterList() == null) {
            request.setChapterList(new ArrayList<>());
        }
        return request;
    }

    private List<CourseChapterSaveDTO> normalizeChapterList(List<CourseChapterSaveDTO> chapterList) {
        if (chapterList == null || chapterList.isEmpty()) {
            return List.of();
        }
        List<CourseChapterSaveDTO> result = new ArrayList<>();
        for (int chapterIndex = 0; chapterIndex < chapterList.size(); chapterIndex++) {
            CourseChapterSaveDTO chapter = chapterList.get(chapterIndex);
            if (chapter == null) {
                continue;
            }
            chapter.setChapterId(StringTools.trim(chapter.getChapterId()));
            chapter.setChapterName(StringTools.trim(chapter.getChapterName()));
            chapter.setDescription(StringTools.trim(chapter.getDescription()));
            chapter.setSortOrder(chapter.getSortOrder() == null ? chapterIndex + 1 : chapter.getSortOrder());
            if (StringTools.isEmpty(chapter.getChapterName())) {
                throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "章节名称不能为空");
            }

            List<CourseLessonSaveDTO> lessonList = chapter.getLessonList() == null ? new ArrayList<>() : chapter.getLessonList();
            List<CourseLessonSaveDTO> normalizedLessonList = new ArrayList<>();
            for (int lessonIndex = 0; lessonIndex < lessonList.size(); lessonIndex++) {
                CourseLessonSaveDTO lesson = lessonList.get(lessonIndex);
                if (lesson == null) {
                    continue;
                }
                lesson.setLessonId(StringTools.trim(lesson.getLessonId()));
                lesson.setLessonName(StringTools.trim(lesson.getLessonName()));
                lesson.setSortOrder(lesson.getSortOrder() == null ? lessonIndex + 1 : lesson.getSortOrder());
                lesson.setPaperId(StringTools.trim(lesson.getPaperId()));
                if (StringTools.isEmpty(lesson.getLessonName())) {
                    throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "课时名称不能为空");
                }
                lesson.setVideoResourceId(lesson.getVideoResourceId());
                lesson.setCoursewareResourceIdList(normalizeResourceIds(lesson.getCoursewareResourceIdList()));
                validateVideoResource(lesson.getVideoResourceId());
                for (Integer coursewareId : lesson.getCoursewareResourceIdList()) {
                    validateCoursewareResource(coursewareId);
                }
                validatePaper(lesson.getPaperId());
                normalizedLessonList.add(lesson);
            }
            chapter.setLessonList(normalizedLessonList);
            result.add(chapter);
        }
        return result;
    }

    private List<Integer> normalizeClassIds(List<Integer> classIdList) {
        if (classIdList == null || classIdList.isEmpty()) {
            return List.of();
        }
        LinkedHashSet<Integer> uniqueIds = classIdList.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        return new ArrayList<>(uniqueIds);
    }

    private void validateTeacher(Integer teacherId) {
        UserInfo teacher = userInfoService.getUserInfoByUserId(teacherId);
        if (teacher == null || !Objects.equals(teacher.getRoleType(), UserRoleTypeEnum.TEACHER.getCode())) {
            throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "授课教师不存在");
        }
    }

    private LoginUserVO getCurrentTeacherLoginUser() {
        LoginUserVO loginUser = LoginUserContextHolder.get();
        if (loginUser == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_901);
        }
        if (!UserRoleTypeEnum.TEACHER.getCode().equals(loginUser.getRoleType())) {
            throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "请使用教师账号操作课程");
        }
        validateTeacher(loginUser.getUserId());
        return loginUser;
    }

    private void checkTeacherOwnership(CourseInfo courseInfo, Integer teacherId) {
        if (!Objects.equals(courseInfo.getTeacherId(), teacherId)) {
            throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "只能操作当前教师自己的课程");
        }
    }

    private void validateCourseEditable(CourseInfo courseInfo) {
        if (Objects.equals(courseInfo.getRecordStatus(), 1)) {
            throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "课程录制完成后不允许再修改课程内容");
        }
    }

    private void validateCoverResource(Integer coverResourceId) {
        if (coverResourceId == null) {
            return;
        }
        ResourceInfo resourceInfo = resourceInfoService.getResourceInfoByResourceId(coverResourceId);
        if (resourceInfo == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "课程封面资源不存在");
        }
    }

    private ResourceInfo validateLessonResource(Integer resourceId) {
        ResourceInfo resourceInfo = resourceInfoService.getResourceInfoByResourceId(resourceId);
        if (resourceInfo == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "课时资源不存在");
        }
        return resourceInfo;
    }

    private ResourceInfo validateVideoResource(Integer resourceId) {
        if (resourceId == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "请为课时选择视频资源");
        }
        ResourceInfo resourceInfo = validateLessonResource(resourceId);
        if (!Objects.equals(resourceInfo.getResourceType(), 1)) {
            throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "课时视频必须选择视频类型资源");
        }
        return resourceInfo;
    }

    private ResourceInfo validateCoursewareResource(Integer resourceId) {
        return validateLessonResource(resourceId);
    }

    private PaperInfo validatePaper(String paperId) {
        String value = StringTools.trim(paperId);
        if (StringTools.isEmpty(value)) {
            return null;
        }
        PaperInfo paperInfo = paperInfoService.getPaperInfoByPaperId(value);
        if (paperInfo == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "课时试卷不存在");
        }
        if (!Objects.equals(paperInfo.getPaperType(), PAPER_TYPE_HOMEWORK)) {
            throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "课时作业只能选择课后习题试卷");
        }
        return paperInfo;
    }

    private void validateClasses(List<Integer> classIdList) {
        if (classIdList.isEmpty()) {
            return;
        }
        List<ClassInfo> classList = classInfoService.getClassInfoByClassIdList(classIdList);
        if (classList.size() != classIdList.size()) {
            throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "存在无效的班级信息");
        }
    }

    private CourseInfo buildCourseInfo(CourseSaveDTO dto, String courseId, CourseInfo original) {
        CourseInfo bean = new CourseInfo();
        bean.setCourseId(courseId);
        bean.setCourseName(dto.getCourseName());
        bean.setCoverResourceId(dto.getCoverResourceId());
        bean.setTeacherId(dto.getTeacherId());
        bean.setDescription(dto.getDescription());
        bean.setRecordStatus(original == null ? dto.getRecordStatus() : original.getRecordStatus());
        bean.setStatus(dto.getStatus());
        if (original != null) {
            bean.setCreateTime(original.getCreateTime());
        }
        return bean;
    }

    private Map<String, List<CourseClass>> loadCourseClassMap(Set<String> courseIdSet) {
        if (courseIdSet.isEmpty()) {
            return Map.of();
        }
        CourseClassQuery query = new CourseClassQuery();
        query.setOrderBy("c.class_id asc");
        return courseClassService.findListByParam(query).stream()
                .filter(item -> courseIdSet.contains(item.getCourseId()))
                .collect(Collectors.groupingBy(CourseClass::getCourseId));
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

    private Map<Integer, ResourceInfo> loadCoverResourceMap(Set<Integer> coverIdSet) {
        return loadResourceMap(coverIdSet);
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

    private List<Integer> normalizeResourceIds(List<Integer> resourceIdList) {
        if (resourceIdList == null || resourceIdList.isEmpty()) {
            return List.of();
        }
        LinkedHashSet<Integer> uniqueIds = resourceIdList.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        return new ArrayList<>(uniqueIds);
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

    private String resolvePaperTypeText(Integer paperType) {
        return switch (paperType == null ? 0 : paperType) {
            case PAPER_TYPE_HOMEWORK -> "课后习题";
            case PAPER_TYPE_EXAM -> "考试试卷";
            default -> "未知";
        };
    }

    private List<String> parseStringIds(String ids) {
        if (StringTools.isEmpty(ids)) {
            return List.of();
        }
        return List.of(ids.split(",")).stream()
                .map(StringTools::trim)
                .filter(value -> !StringTools.isEmpty(value))
                .distinct()
                .toList();
    }

    private String generateStringId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();
    }
}
