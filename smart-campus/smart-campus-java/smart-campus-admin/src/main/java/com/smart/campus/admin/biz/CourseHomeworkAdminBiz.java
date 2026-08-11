package com.smart.campus.admin.biz;

import com.alibaba.fastjson2.JSON;
import com.smart.campus.admin.biz.support.MessagePublishAdminSupport;
import com.smart.campus.entity.dto.CourseHomeworkJudgeDTO;
import com.smart.campus.entity.dto.CourseHomeworkJudgeQuestionDTO;
import com.smart.campus.entity.enums.ResponseCodeEnum;
import com.smart.campus.entity.enums.UserRoleTypeEnum;
import com.smart.campus.entity.po.ClassInfo;
import com.smart.campus.entity.po.CourseAssessmentSubmit;
import com.smart.campus.entity.po.CourseAssessmentSubmitQuestion;
import com.smart.campus.entity.po.CourseChapterLesson;
import com.smart.campus.entity.po.CourseInfo;
import com.smart.campus.entity.po.PaperQuestion;
import com.smart.campus.entity.po.PaperInfo;
import com.smart.campus.entity.po.QuestionInfo;
import com.smart.campus.entity.po.UserInfo;
import com.smart.campus.entity.query.CourseAssessmentSubmitQuestionQuery;
import com.smart.campus.entity.query.CourseHomeworkSubmitManageQuery;
import com.smart.campus.entity.query.PaperQuestionQuery;
import com.smart.campus.entity.query.SimplePage;
import com.smart.campus.entity.query.UserInfoQuery;
import com.smart.campus.entity.vo.CourseChapterDetailVO;
import com.smart.campus.entity.vo.CourseDetailVO;
import com.smart.campus.entity.vo.CourseHomeworkQuestionVO;
import com.smart.campus.entity.vo.CourseHomeworkSectionVO;
import com.smart.campus.entity.vo.CourseHomeworkSubmitManageDetailVO;
import com.smart.campus.entity.vo.CourseHomeworkSubmitManageItemVO;
import com.smart.campus.entity.vo.CourseLessonDetailVO;
import com.smart.campus.entity.vo.LoginUserVO;
import com.smart.campus.entity.vo.PaginationResultVO;
import com.smart.campus.exception.BusinessException;
import com.smart.campus.mappers.CourseAssessmentSubmitMapper;
import com.smart.campus.service.ClassInfoService;
import com.smart.campus.service.CourseAssessmentSubmitQuestionService;
import com.smart.campus.service.CourseAssessmentSubmitService;
import com.smart.campus.service.CourseChapterLessonService;
import com.smart.campus.service.CourseInfoService;
import com.smart.campus.service.PaperQuestionService;
import com.smart.campus.service.PaperInfoService;
import com.smart.campus.service.QuestionInfoService;
import com.smart.campus.service.UserInfoService;
import com.smart.campus.utils.LoginUserContextHolder;
import com.smart.campus.utils.StringTools;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.CRC32;

@Service
public class CourseHomeworkAdminBiz {

    private static final int DEFAULT_PAGE_NO = 1;
    private static final int DEFAULT_PAGE_SIZE = 15;
    private static final int USER_ROLE_ADMIN = UserRoleTypeEnum.ADMIN.getCode();
    private static final int USER_ROLE_TEACHER = UserRoleTypeEnum.TEACHER.getCode();
    private static final int USER_ROLE_STUDENT = UserRoleTypeEnum.STUDENT.getCode();
    private static final int TASK_TYPE_HOMEWORK = 1;
    private static final int PAPER_TYPE_HOMEWORK = 1;
    private static final int PAPER_SECTION_TYPE_SECTION = 1;
    private static final int QUESTION_TYPE_SUBJECTIVE = 4;
    private static final int SUBMIT_STATUS_PENDING = 0;
    private static final int SUBMIT_STATUS_ANSWERING = 1;
    private static final int SUBMIT_STATUS_DRAFT = 2;
    private static final int SUBMIT_STATUS_SUBMITTED = 3;
    private static final int JUDGE_STATUS_NOT_STARTED = 0;
    private static final int JUDGE_STATUS_AUTO_DONE = 1;
    private static final int JUDGE_STATUS_WAIT_MANUAL = 2;
    private static final int JUDGE_STATUS_MANUAL_DONE = 3;
    private static final String PAPER_QUESTION_ORDER_BY = "p.sort_order asc,p.id asc";
    private static final String USER_ORDER_BY = "u.user_no asc,u.user_id asc";

    @Resource
    private CourseAdminBiz courseAdminBiz;

    @Resource
    private CourseInfoService courseInfoService;

    @Resource
    private CourseChapterLessonService courseChapterLessonService;

    @Resource
    private UserInfoService userInfoService;

    @Resource
    private ClassInfoService classInfoService;

    @Resource
    private PaperQuestionService paperQuestionService;

    @Resource
    private PaperInfoService paperInfoService;

    @Resource
    private QuestionInfoService questionInfoService;

    @Resource
    private CourseAssessmentSubmitService courseAssessmentSubmitService;

    @Resource
    private CourseAssessmentSubmitQuestionService courseAssessmentSubmitQuestionService;

    @Resource
    private CourseAssessmentSubmitMapper<CourseAssessmentSubmit, com.smart.campus.entity.query.CourseAssessmentSubmitQuery> courseAssessmentSubmitMapper;

    @Resource
    private MessagePublishAdminSupport messagePublishAdminSupport;

    public PaginationResultVO<CourseHomeworkSubmitManageItemVO> loadHomeworkSubmitList(CourseHomeworkSubmitManageQuery query) {
        CourseHomeworkSubmitManageQuery request = normalizeQuery(query);
        if (StringTools.isEmpty(request.getCourseId()) || StringTools.isEmpty(request.getLessonId())) {
            return emptyPage(request);
        }

        CourseDetailVO courseDetail = loadCourseDetailWithPermission(request.getCourseId());
        Integer classId = resolveSelectedClassId(courseDetail, request.getClassId());
        LessonSelection lessonSelection = findLessonSelection(courseDetail, request.getLessonId());
        if (classId == null || lessonSelection == null) {
            return emptyPage(request);
        }

        ClassInfo classInfo = classInfoService.getClassInfoByClassId(classId);
        CourseChapterDetailVO chapter = lessonSelection.chapter();
        CourseLessonDetailVO lesson = lessonSelection.lesson();
        if (lesson == null || StringTools.isEmpty(lesson.getPaperId()) || !Objects.equals(lesson.getPaperType(), PAPER_TYPE_HOMEWORK)) {
            return emptyPage(request);
        }

        List<UserInfo> studentList = loadStudentList(classId, request.getKeyword());
        if (studentList.isEmpty()) {
            return emptyPage(request);
        }

        List<CourseLessonDetailVO> homeworkLessonList = List.of(lesson);
        Map<String, CourseAssessmentSubmit> submitMap = loadSubmitMap(homeworkLessonList, studentList);
        List<CourseHomeworkSubmitManageItemVO> rowList = new ArrayList<>();
        for (UserInfo student : studentList) {
            CourseAssessmentSubmit submit = submitMap.get(buildSubmitKey(lesson.getLessonId(), student.getUserId()));
            CourseHomeworkSubmitManageItemVO item = buildManageItem(courseDetail, chapter, lesson, classId, classInfo, student, submit);
            if (matchStatusFilter(item, request)) {
                rowList.add(item);
            }
        }

        int totalCount = rowList.size();
        SimplePage simplePage = new SimplePage(request.getPageNo(), totalCount, request.getPageSize());
        int start = simplePage.getStart();
        int end = Math.min(start + simplePage.getPageSize(), totalCount);
        List<CourseHomeworkSubmitManageItemVO> currentList =
                start >= totalCount ? List.of() : rowList.subList(start, end);
        return new PaginationResultVO<>(
                totalCount,
                simplePage.getPageSize(),
                simplePage.getPageNo(),
                simplePage.getPageTotal(),
                currentList
        );
    }

    public CourseHomeworkSubmitManageDetailVO getHomeworkSubmitDetail(String courseId, String lessonId, Integer studentId) {
        HomeworkAdminContext context = loadHomeworkContext(courseId, lessonId, studentId);
        CourseAssessmentSubmit submit = getLatestHomeworkSubmit(
                context.lesson.getLessonId(),
                context.student.getUserId()
        );
        return buildManageDetail(context, submit);
    }

    @Transactional(rollbackFor = Exception.class)
    public void judgeHomeworkSubmit(CourseHomeworkJudgeDTO dto) {
        CourseAssessmentSubmit submit = courseAssessmentSubmitService.getCourseAssessmentSubmitBySubmitId(dto.getSubmitId());
        if (submit == null || !Objects.equals(submit.getTaskType(), TASK_TYPE_HOMEWORK)) {
            throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "作业提交记录不存在");
        }
        if (!Objects.equals(submit.getSubmitStatus(), SUBMIT_STATUS_SUBMITTED)) {
            throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "学生尚未提交作业，暂不能批改");
        }

        HomeworkAdminContext context = loadHomeworkContextBySubmit(submit);
        Map<Integer, CourseAssessmentSubmitQuestion> answerMap = loadSubmitQuestionMap(submit.getSubmitId());
        Map<Integer, CourseHomeworkJudgeQuestionDTO> scoreMap = (dto.getQuestionScoreList() == null ? List.<CourseHomeworkJudgeQuestionDTO>of() : dto.getQuestionScoreList())
                .stream()
                .filter(item -> item.getQuestionId() != null)
                .collect(Collectors.toMap(
                        CourseHomeworkJudgeQuestionDTO::getQuestionId,
                        item -> item,
                        (left, right) -> right,
                        LinkedHashMap::new
                ));

        BigDecimal subjectiveScore = BigDecimal.ZERO;
        List<CourseAssessmentSubmitQuestion> updateList = new ArrayList<>();
        List<PaperQuestion> paperQuestionList = loadPaperQuestionList(resolveEffectivePaperId(context, submit));
        for (PaperQuestion paperQuestion : paperQuestionList) {
            if (Objects.equals(paperQuestion.getSectionType(), PAPER_SECTION_TYPE_SECTION)
                    || paperQuestion.getQuestionId() == null
                    || !Objects.equals(paperQuestion.getQuestionType(), QUESTION_TYPE_SUBJECTIVE)) {
                continue;
            }
            CourseAssessmentSubmitQuestion answer = answerMap.get(paperQuestion.getQuestionId());
            if (answer == null) {
                answer = new CourseAssessmentSubmitQuestion();
                answer.setSubmitId(submit.getSubmitId());
                answer.setTaskId(toQuestionTaskId(submit.getTaskId()));
                answer.setPaperId(paperQuestion.getId());
                answer.setQuestionId(paperQuestion.getQuestionId());
                answer.setAnswerContent(null);
            }
            BigDecimal score = normalizeJudgeScore(
                    scoreMap.get(paperQuestion.getQuestionId()) == null ? null : scoreMap.get(paperQuestion.getQuestionId()).getScore(),
                    paperQuestion.getQuestionScore()
            );
            answer.setFinalScore(score);
            answer.setJudgeStatus(JUDGE_STATUS_MANUAL_DONE);
            subjectiveScore = subjectiveScore.add(score);
            updateList.add(answer);
        }
        if (!updateList.isEmpty()) {
            courseAssessmentSubmitQuestionService.addOrUpdateBatch(updateList);
        }

        CourseAssessmentSubmit update = new CourseAssessmentSubmit();
        update.setTeacherComment(StringTools.trim(dto.getTeacherComment()));
        update.setSubjectiveScore(scaleScore(subjectiveScore));
        update.setJudgeTime(new Date());
        update.setJudgeStatus(JUDGE_STATUS_MANUAL_DONE);
        courseAssessmentSubmitService.updateCourseAssessmentSubmitBySubmitId(update, submit.getSubmitId());
        LoginUserVO loginUser = LoginUserContextHolder.get();
        if (loginUser != null) {
            messagePublishAdminSupport.sendHomeworkJudgedSystemMessage(
                    courseInfoService.getCourseInfoByCourseId(context.courseDetail.getCourseId()),
                    context.lesson.getLessonId(),
                    context.lesson.getLessonName(),
                    context.student.getUserId(),
                    context.student.getRealName(),
                    loginUser,
                    scaleScore(defaultScore(submit.getObjectiveScore()).add(update.getSubjectiveScore())).toPlainString()
            );
        }
    }

    private CourseHomeworkSubmitManageQuery normalizeQuery(CourseHomeworkSubmitManageQuery query) {
        CourseHomeworkSubmitManageQuery request = query == null ? new CourseHomeworkSubmitManageQuery() : query;
        if (request.getPageNo() == null || request.getPageNo() < 1) {
            request.setPageNo(DEFAULT_PAGE_NO);
        }
        if (request.getPageSize() == null || request.getPageSize() < 1) {
            request.setPageSize(DEFAULT_PAGE_SIZE);
        }
        request.setCourseId(StringTools.trim(request.getCourseId()));
        request.setChapterId(StringTools.trim(request.getChapterId()));
        request.setLessonId(StringTools.trim(request.getLessonId()));
        request.setKeyword(StringTools.trim(request.getKeyword()));
        return request;
    }

    private PaginationResultVO<CourseHomeworkSubmitManageItemVO> emptyPage(CourseHomeworkSubmitManageQuery query) {
        int pageSize = query == null || query.getPageSize() == null ? DEFAULT_PAGE_SIZE : query.getPageSize();
        int pageNo = query == null || query.getPageNo() == null ? DEFAULT_PAGE_NO : query.getPageNo();
        return new PaginationResultVO<>(0, pageSize, pageNo, 0, List.of());
    }

    private CourseDetailVO loadCourseDetailWithPermission(String courseId) {
        CourseInfo courseInfo = courseInfoService.getCourseInfoByCourseId(courseId);
        if (courseInfo == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "课程信息不存在");
        }
        checkCoursePermission(courseInfo);
        return courseAdminBiz.getCourseInfoById(courseId);
    }

    private void checkCoursePermission(CourseInfo courseInfo) {
        LoginUserVO loginUser = LoginUserContextHolder.get();
        if (loginUser == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_901);
        }
        if (Objects.equals(loginUser.getRoleType(), USER_ROLE_ADMIN)) {
            return;
        }
        if (Objects.equals(loginUser.getRoleType(), USER_ROLE_TEACHER)
                && Objects.equals(loginUser.getUserId(), courseInfo.getTeacherId())) {
            return;
        }
        throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "无权查看该课程作业");
    }

    private Integer resolveSelectedClassId(CourseDetailVO courseDetail, Integer classId) {
        List<Integer> classIdList = courseDetail.getClassIdList() == null ? List.of() : courseDetail.getClassIdList();
        if (classId != null && classIdList.contains(classId)) {
            return classId;
        }
        return classIdList.isEmpty() ? null : classIdList.get(0);
    }

    private LessonSelection findLessonSelection(CourseDetailVO courseDetail, String lessonId) {
        if (courseDetail.getChapterList() == null || StringTools.isEmpty(lessonId)) {
            return null;
        }
        for (CourseChapterDetailVO chapter : courseDetail.getChapterList()) {
            for (CourseLessonDetailVO lesson : chapter.getLessonList() == null ? List.<CourseLessonDetailVO>of() : chapter.getLessonList()) {
                if (Objects.equals(lesson.getLessonId(), lessonId)) {
                    return new LessonSelection(chapter, lesson);
                }
            }
        }
        return null;
    }

    private List<UserInfo> loadStudentList(Integer classId, String keyword) {
        UserInfoQuery query = new UserInfoQuery();
        query.setClassId(String.valueOf(classId));
        query.setRoleType(USER_ROLE_STUDENT);
        query.setOrderBy(USER_ORDER_BY);
        List<UserInfo> rawList = userInfoService.findListByParam(query);
        if (StringTools.isEmpty(keyword)) {
            return rawList;
        }
        String normalizedKeyword = keyword.toLowerCase();
        return rawList.stream()
                .filter(item -> String.valueOf(item.getUserNo()).toLowerCase().contains(normalizedKeyword)
                        || String.valueOf(item.getRealName()).toLowerCase().contains(normalizedKeyword))
                .toList();
    }

    private Map<String, CourseAssessmentSubmit> loadSubmitMap(List<CourseLessonDetailVO> lessonList, List<UserInfo> studentList) {
        List<String> taskIdList = lessonList.stream()
                .map(CourseLessonDetailVO::getLessonId)
                .filter(item -> !StringTools.isEmpty(item))
                .distinct()
                .toList();
        List<Integer> userIdList = studentList.stream()
                .map(UserInfo::getUserId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (taskIdList.isEmpty() || userIdList.isEmpty()) {
            return Map.of();
        }
        List<CourseAssessmentSubmit> submitList = courseAssessmentSubmitMapper.selectByTaskIdListAndUserIdList(
                taskIdList,
                userIdList,
                TASK_TYPE_HOMEWORK
        );
        Map<String, CourseAssessmentSubmit> result = new HashMap<>();
        for (CourseAssessmentSubmit item : submitList) {
            String key = buildSubmitKey(item.getTaskId(), item.getUserId());
            CourseAssessmentSubmit current = result.get(key);
            if (current == null || compareSubmitPriority(item, current) > 0) {
                result.put(key, item);
            }
        }
        return result;
    }

    private CourseHomeworkSubmitManageItemVO buildManageItem(CourseDetailVO courseDetail,
                                                             CourseChapterDetailVO chapter,
                                                             CourseLessonDetailVO lesson,
                                                             Integer classId,
                                                             ClassInfo classInfo,
                                                             UserInfo student,
                                                             CourseAssessmentSubmit submit) {
        CourseHomeworkSubmitManageItemVO item = new CourseHomeworkSubmitManageItemVO();
        item.setStudentId(student.getUserId());
        item.setStudentNo(student.getUserNo());
        item.setStudentName(student.getRealName());
        item.setClassId(classId);
        item.setClassName(classInfo == null ? "" : classInfo.getClassName());
        item.setChapterId(chapter.getChapterId());
        item.setChapterName(chapter.getChapterName());
        item.setLessonId(lesson.getLessonId());
        item.setLessonName(lesson.getLessonName());
        item.setPaperId(lesson.getPaperId());
        item.setPaperName(lesson.getPaperName());
        item.setTotalScore(BigDecimal.ZERO);

        if (submit == null) {
            item.setSubmitStatus(SUBMIT_STATUS_PENDING);
            item.setSubmitStatusText(resolveSubmitStatusText(SUBMIT_STATUS_PENDING));
            item.setJudgeStatus(JUDGE_STATUS_NOT_STARTED);
            item.setJudgeStatusText(resolveJudgeStatusText(JUDGE_STATUS_NOT_STARTED));
            item.setUsedSeconds(0);
            item.setObjectiveScore(BigDecimal.ZERO);
            item.setSubjectiveScore(BigDecimal.ZERO);
            item.setFinalScore(BigDecimal.ZERO);
            return item;
        }

        item.setSubmitId(submit.getSubmitId());
        item.setSubmitStatus(submit.getSubmitStatus());
        item.setSubmitStatusText(resolveSubmitStatusText(submit.getSubmitStatus()));
        item.setJudgeStatus(submit.getJudgeStatus());
        item.setJudgeStatusText(resolveJudgeStatusText(submit.getJudgeStatus()));
        item.setUsedSeconds(safeInt(submit.getUsedSeconds()));
        item.setObjectiveScore(scaleScore(submit.getObjectiveScore()));
        item.setSubjectiveScore(scaleScore(submit.getSubjectiveScore()));
        item.setFinalScore(scaleScore(defaultScore(submit.getObjectiveScore()).add(defaultScore(submit.getSubjectiveScore()))));
        item.setStartedTime(submit.getStartedTime());
        item.setSubmitTime(submit.getSubmitTime());
        item.setJudgeTime(submit.getJudgeTime());
        return item;
    }

    private boolean matchStatusFilter(CourseHomeworkSubmitManageItemVO item, CourseHomeworkSubmitManageQuery query) {
        if (query.getSubmitStatus() != null && !Objects.equals(item.getSubmitStatus(), query.getSubmitStatus())) {
            return false;
        }
        if (query.getJudgeStatus() != null && !Objects.equals(item.getJudgeStatus(), query.getJudgeStatus())) {
            return false;
        }
        return true;
    }

    private HomeworkAdminContext loadHomeworkContext(String courseId, String lessonId, Integer studentId) {
        String normalizedCourseId = StringTools.trim(courseId);
        String normalizedLessonId = StringTools.trim(lessonId);
        if (StringTools.isEmpty(normalizedCourseId) || StringTools.isEmpty(normalizedLessonId) || studentId == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "作业详情参数不能为空");
        }

        CourseDetailVO courseDetail = loadCourseDetailWithPermission(normalizedCourseId);
        CourseChapterDetailVO chapter = null;
        CourseLessonDetailVO lesson = null;
        for (CourseChapterDetailVO chapterItem : courseDetail.getChapterList()) {
            for (CourseLessonDetailVO lessonItem : chapterItem.getLessonList()) {
                if (Objects.equals(lessonItem.getLessonId(), normalizedLessonId)) {
                    chapter = chapterItem;
                    lesson = lessonItem;
                    break;
                }
            }
            if (lesson != null) {
                break;
            }
        }
        if (chapter == null || lesson == null || StringTools.isEmpty(lesson.getPaperId())
                || !Objects.equals(lesson.getPaperType(), PAPER_TYPE_HOMEWORK)) {
            throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "当前课时未配置课后作业");
        }

        UserInfo student = userInfoService.getUserInfoByUserId(studentId);
        if (student == null || !Objects.equals(student.getRoleType(), USER_ROLE_STUDENT)) {
            throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "学生信息不存在");
        }
        Integer studentClassId = resolveStudentClassId(student);
        if (studentClassId == null || !courseDetail.getClassIdList().contains(studentClassId)) {
            throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "该学生不在当前课程授课班级中");
        }
        ClassInfo classInfo = classInfoService.getClassInfoByClassId(studentClassId);

        HomeworkAdminContext context = new HomeworkAdminContext();
        context.courseDetail = courseDetail;
        context.chapter = chapter;
        context.lesson = lesson;
        context.student = student;
        context.classInfo = classInfo;
        context.defaultPaperId = lesson.getPaperId();
        return context;
    }

    private HomeworkAdminContext loadHomeworkContextBySubmit(CourseAssessmentSubmit submit) {
        CourseChapterLesson lesson = courseChapterLessonService.getCourseChapterLessonByLessonId(submit.getTaskId());
        if (lesson == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "作业课时不存在");
        }
        return loadHomeworkContext(lesson.getCourseId(), lesson.getLessonId(), submit.getUserId());
    }

    private CourseHomeworkSubmitManageDetailVO buildManageDetail(HomeworkAdminContext context, CourseAssessmentSubmit submit) {
        CourseHomeworkSubmitManageDetailVO vo = new CourseHomeworkSubmitManageDetailVO();
        vo.setStudentId(context.student.getUserId());
        vo.setStudentNo(context.student.getUserNo());
        vo.setStudentName(context.student.getRealName());
        vo.setClassId(resolveStudentClassId(context.student));
        vo.setClassName(context.classInfo == null ? "" : context.classInfo.getClassName());
        vo.setCourseId(context.courseDetail.getCourseId());
        vo.setCourseName(context.courseDetail.getCourseName());
        vo.setChapterId(context.chapter.getChapterId());
        vo.setChapterName(context.chapter.getChapterName());
        vo.setLessonId(context.lesson.getLessonId());
        vo.setLessonName(context.lesson.getLessonName());
        String effectivePaperId = resolveEffectivePaperId(context, submit);
        PaperInfo effectivePaperInfo = loadPaperInfo(effectivePaperId);
        vo.setPaperId(effectivePaperId);
        vo.setPaperName(effectivePaperInfo == null ? context.lesson.getPaperName() : effectivePaperInfo.getPaperName());

        Map<Integer, CourseAssessmentSubmitQuestion> submitQuestionMap = submit == null
                ? Map.of()
                : loadSubmitQuestionMap(submit.getSubmitId());
        List<PaperQuestion> paperQuestionList = loadPaperQuestionList(effectivePaperId);
        Map<Integer, QuestionInfo> questionInfoMap = loadQuestionInfoMap(paperQuestionList);

        LinkedHashMap<Integer, CourseHomeworkSectionVO> sectionMap = new LinkedHashMap<>();
        int answeredCount = 0;
        int questionCount = 0;
        BigDecimal totalScore = BigDecimal.ZERO;
        for (PaperQuestion item : paperQuestionList) {
            if (Objects.equals(item.getSectionType(), PAPER_SECTION_TYPE_SECTION)) {
                CourseHomeworkSectionVO sectionVO = new CourseHomeworkSectionVO();
                sectionVO.setSectionId(item.getId());
                sectionVO.setSectionName(item.getSectionName());
                sectionVO.setSortOrder(item.getSortOrder());
                sectionVO.setTotalScore(BigDecimal.ZERO);
                sectionMap.put(item.getId(), sectionVO);
                continue;
            }
            if (item.getQuestionId() == null) {
                continue;
            }
            questionCount++;
            CourseHomeworkSectionVO sectionVO = sectionMap.computeIfAbsent(item.getParentId(), key -> {
                CourseHomeworkSectionVO defaultSection = new CourseHomeworkSectionVO();
                defaultSection.setSectionId(key);
                defaultSection.setSectionName(StringTools.isEmpty(item.getSectionName()) ? "题目列表" : item.getSectionName());
                defaultSection.setSortOrder(item.getSortOrder());
                defaultSection.setTotalScore(BigDecimal.ZERO);
                return defaultSection;
            });
            CourseHomeworkQuestionVO questionVO = parseQuestionSnapshot(item.getQuestionSnapshot());
            if (questionVO == null) {
                questionVO = buildQuestionVOFromQuestionInfo(questionInfoMap.get(item.getQuestionId()));
            }
            questionVO.setPaperQuestionId(item.getId());
            questionVO.setQuestionId(item.getQuestionId());
            questionVO.setQuestionScore(defaultScore(item.getQuestionScore()));
            questionVO.setSortOrder(item.getSortOrder());
            CourseAssessmentSubmitQuestion submitQuestion = submitQuestionMap.get(item.getQuestionId());
            if (submitQuestion != null) {
                questionVO.setAnswerContent(submitQuestion.getAnswerContent());
                questionVO.setFinalScore(defaultScore(submitQuestion.getFinalScore()));
                questionVO.setJudgeStatus(submitQuestion.getJudgeStatus());
            } else {
                questionVO.setFinalScore(BigDecimal.ZERO);
                questionVO.setJudgeStatus(JUDGE_STATUS_NOT_STARTED);
            }
            boolean answered = !StringTools.isEmpty(questionVO.getAnswerContent());
            questionVO.setAnswered(answered);
            if (answered) {
                answeredCount++;
            }
            totalScore = totalScore.add(defaultScore(item.getQuestionScore()));
            sectionVO.setTotalScore(sectionVO.getTotalScore().add(defaultScore(item.getQuestionScore())));
            sectionVO.getQuestionList().add(questionVO);
        }

        vo.setQuestionCount(questionCount);
        vo.setAnsweredCount(answeredCount);
        vo.setTotalScore(scaleScore(totalScore));
        if (submit == null) {
            vo.setSubmitStatus(SUBMIT_STATUS_PENDING);
            vo.setSubmitStatusText(resolveSubmitStatusText(SUBMIT_STATUS_PENDING));
            vo.setJudgeStatus(JUDGE_STATUS_NOT_STARTED);
            vo.setJudgeStatusText(resolveJudgeStatusText(JUDGE_STATUS_NOT_STARTED));
            vo.setUsedSeconds(0);
            vo.setObjectiveScore(BigDecimal.ZERO);
            vo.setSubjectiveScore(BigDecimal.ZERO);
            vo.setFinalScore(BigDecimal.ZERO);
            vo.setCanJudge(false);
        } else {
            vo.setSubmitId(submit.getSubmitId());
            vo.setSubmitStatus(submit.getSubmitStatus());
            vo.setSubmitStatusText(resolveSubmitStatusText(submit.getSubmitStatus()));
            vo.setJudgeStatus(submit.getJudgeStatus());
            vo.setJudgeStatusText(resolveJudgeStatusText(submit.getJudgeStatus()));
            vo.setUsedSeconds(safeInt(submit.getUsedSeconds()));
            vo.setSubmitContent(submit.getSubmitContent());
            vo.setTeacherComment(submit.getTeacherComment());
            vo.setObjectiveScore(scaleScore(submit.getObjectiveScore()));
            vo.setSubjectiveScore(scaleScore(submit.getSubjectiveScore()));
            vo.setFinalScore(scaleScore(defaultScore(submit.getObjectiveScore()).add(defaultScore(submit.getSubjectiveScore()))));
            vo.setStartedTime(submit.getStartedTime());
            vo.setSubmitTime(submit.getSubmitTime());
            vo.setJudgeTime(submit.getJudgeTime());
            vo.setCanJudge(Objects.equals(submit.getSubmitStatus(), SUBMIT_STATUS_SUBMITTED));
        }
        vo.setSectionList(new ArrayList<>(sectionMap.values()));
        return vo;
    }

    private List<PaperQuestion> loadPaperQuestionList(String paperId) {
        PaperQuestionQuery query = new PaperQuestionQuery();
        query.setPaperId(paperId);
        query.setOrderBy(PAPER_QUESTION_ORDER_BY);
        return paperQuestionService.findListByParam(query);
    }

    private Map<Integer, QuestionInfo> loadQuestionInfoMap(Collection<PaperQuestion> paperQuestionList) {
        Map<Integer, QuestionInfo> result = new HashMap<>();
        if (paperQuestionList == null || paperQuestionList.isEmpty()) {
            return result;
        }
        Set<Integer> questionIdSet = new LinkedHashSet<>();
        for (PaperQuestion item : paperQuestionList) {
            if (item.getQuestionId() != null) {
                questionIdSet.add(item.getQuestionId());
            }
        }
        for (Integer questionId : questionIdSet) {
            QuestionInfo questionInfo = questionInfoService.getQuestionInfoByQuestionId(questionId);
            if (questionInfo != null) {
                result.put(questionId, questionInfo);
            }
        }
        return result;
    }

    private Map<Integer, CourseAssessmentSubmitQuestion> loadSubmitQuestionMap(Long submitId) {
        CourseAssessmentSubmitQuestionQuery query = new CourseAssessmentSubmitQuestionQuery();
        query.setSubmitId(submitId);
        List<CourseAssessmentSubmitQuestion> list = courseAssessmentSubmitQuestionService.findListByParam(query);
        Map<Integer, CourseAssessmentSubmitQuestion> result = new HashMap<>();
        for (CourseAssessmentSubmitQuestion item : list) {
            if (item.getQuestionId() != null) {
                result.put(item.getQuestionId(), item);
            }
        }
        return result;
    }

    private CourseHomeworkQuestionVO parseQuestionSnapshot(String snapshotText) {
        if (StringTools.isEmpty(snapshotText)) {
            return null;
        }
        try {
            return JSON.parseObject(snapshotText, CourseHomeworkQuestionVO.class);
        } catch (Exception e) {
            return null;
        }
    }

    private CourseHomeworkQuestionVO buildQuestionVOFromQuestionInfo(QuestionInfo questionInfo) {
        CourseHomeworkQuestionVO vo = new CourseHomeworkQuestionVO();
        if (questionInfo == null) {
            return vo;
        }
        vo.setQuestionId(questionInfo.getQuestionId());
        vo.setQuestionType(questionInfo.getQuestionType());
        vo.setQuestionTitle(questionInfo.getQuestionTitle());
        vo.setDifficultyLevel(questionInfo.getDifficultyLevel());
        vo.setAnswerAnalysis(questionInfo.getAnswerAnalysis());
        vo.setCorrectAnswerText(questionInfo.getCorrectAnswer());
        return vo;
    }

    private Integer resolveStudentClassId(UserInfo student) {
        if (student == null || StringTools.isEmpty(student.getClassId())) {
            return null;
        }
        for (String item : student.getClassId().split(",")) {
            String value = StringTools.trim(item);
            if (StringTools.isEmpty(value)) {
                continue;
            }
            try {
                return Integer.valueOf(value);
            } catch (NumberFormatException ignore) {
                // ignore
            }
        }
        return null;
    }

    private BigDecimal normalizeJudgeScore(BigDecimal score, BigDecimal maxScore) {
        BigDecimal normalized = score == null ? BigDecimal.ZERO : score.max(BigDecimal.ZERO);
        BigDecimal max = defaultScore(maxScore);
        if (normalized.compareTo(max) > 0) {
            normalized = max;
        }
        return scaleScore(normalized);
    }

    private BigDecimal defaultScore(BigDecimal score) {
        return score == null ? BigDecimal.ZERO : score;
    }

    private BigDecimal scaleScore(BigDecimal score) {
        return defaultScore(score).setScale(2, RoundingMode.HALF_UP);
    }

    private int safeInt(Integer value) {
        return value == null ? 0 : Math.max(value, 0);
    }

    private Long toQuestionTaskId(String taskId) {
        if (StringTools.isEmpty(taskId)) {
            return 0L;
        }
        try {
            return Long.valueOf(taskId);
        } catch (NumberFormatException e) {
            CRC32 crc32 = new CRC32();
            crc32.update(taskId.getBytes(StandardCharsets.UTF_8));
            return crc32.getValue();
        }
    }

    private String buildSubmitKey(String lessonId, Integer studentId) {
        return String.format("%s_%s",
                StringTools.trim(lessonId),
                studentId == null ? "" : studentId);
    }

    private CourseAssessmentSubmit getLatestHomeworkSubmit(String lessonId, Integer studentId) {
        if (StringTools.isEmpty(lessonId) || studentId == null) {
            return null;
        }
        com.smart.campus.entity.query.CourseAssessmentSubmitQuery query =
                new com.smart.campus.entity.query.CourseAssessmentSubmitQuery();
        query.setTaskId(lessonId);
        query.setUserId(studentId);
        query.setTaskType(TASK_TYPE_HOMEWORK);
        query.setOrderBy("c.submit_id desc");
        List<CourseAssessmentSubmit> list = courseAssessmentSubmitService.findListByParam(query);
        return list.isEmpty() ? null : list.get(0);
    }

    private int compareSubmitPriority(CourseAssessmentSubmit left, CourseAssessmentSubmit right) {
        long leftId = left == null || left.getSubmitId() == null ? 0L : left.getSubmitId();
        long rightId = right == null || right.getSubmitId() == null ? 0L : right.getSubmitId();
        return Long.compare(leftId, rightId);
    }

    private String resolveEffectivePaperId(HomeworkAdminContext context, CourseAssessmentSubmit submit) {
        if (submit != null && !StringTools.isEmpty(submit.getPaperId())) {
            return submit.getPaperId();
        }
        return context.defaultPaperId;
    }

    private PaperInfo loadPaperInfo(String paperId) {
        if (StringTools.isEmpty(paperId)) {
            return null;
        }
        return paperInfoService.getPaperInfoByPaperId(paperId);
    }

    private String resolveSubmitStatusText(Integer submitStatus) {
        if (Objects.equals(submitStatus, SUBMIT_STATUS_ANSWERING)) {
            return "作答中";
        }
        if (Objects.equals(submitStatus, SUBMIT_STATUS_DRAFT)) {
            return "草稿";
        }
        if (Objects.equals(submitStatus, SUBMIT_STATUS_SUBMITTED)) {
            return "已提交";
        }
        return "待开始";
    }

    private String resolveJudgeStatusText(Integer judgeStatus) {
        if (Objects.equals(judgeStatus, JUDGE_STATUS_AUTO_DONE)) {
            return "自动判分完成";
        }
        if (Objects.equals(judgeStatus, JUDGE_STATUS_WAIT_MANUAL)) {
            return "待人工批改";
        }
        if (Objects.equals(judgeStatus, JUDGE_STATUS_MANUAL_DONE)) {
            return "人工批改完成";
        }
        return "未批改";
    }

    private static class HomeworkAdminContext {
        private CourseDetailVO courseDetail;
        private CourseChapterDetailVO chapter;
        private CourseLessonDetailVO lesson;
        private UserInfo student;
        private ClassInfo classInfo;
        private String defaultPaperId;
    }

    private record LessonSelection(CourseChapterDetailVO chapter, CourseLessonDetailVO lesson) {}
}
