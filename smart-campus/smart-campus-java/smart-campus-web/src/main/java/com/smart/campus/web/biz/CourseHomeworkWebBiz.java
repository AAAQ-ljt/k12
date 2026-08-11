package com.smart.campus.web.biz;

import com.alibaba.fastjson2.JSON;
import com.smart.campus.entity.dto.CourseHomeworkAnswerSaveDTO;
import com.smart.campus.entity.dto.CourseHomeworkDraftSaveDTO;
import com.smart.campus.entity.dto.CourseHomeworkStartDTO;
import com.smart.campus.entity.dto.CourseHomeworkSubmitDTO;
import com.smart.campus.entity.enums.ResponseCodeEnum;
import com.smart.campus.entity.enums.UserRoleTypeEnum;
import com.smart.campus.entity.po.*;
import com.smart.campus.entity.query.CourseAssessmentSubmitQuestionQuery;
import com.smart.campus.entity.query.CourseChapterLessonResourceQuery;
import com.smart.campus.entity.query.CourseClassQuery;
import com.smart.campus.entity.vo.*;
import com.smart.campus.exception.BusinessException;
import com.smart.campus.service.*;
import com.smart.campus.utils.LoginUserContextHolder;
import com.smart.campus.utils.StringTools;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.CRC32;

@Service
public class CourseHomeworkWebBiz {

    private static final int USER_ROLE_STUDENT = UserRoleTypeEnum.STUDENT.getCode();
    private static final int LESSON_RESOURCE_ROLE_PAPER = 3;
    private static final int TASK_TYPE_HOMEWORK = 1;
    private static final int SUBMIT_STATUS_PENDING = 0;
    private static final int SUBMIT_STATUS_ANSWERING = 1;
    private static final int SUBMIT_STATUS_DRAFT = 2;
    private static final int SUBMIT_STATUS_SUBMITTED = 3;
    private static final int JUDGE_STATUS_NOT_STARTED = 0;
    private static final int JUDGE_STATUS_AUTO_DONE = 1;
    private static final int JUDGE_STATUS_WAIT_MANUAL = 2;
    private static final int JUDGE_STATUS_MANUAL_DONE = 3;
    private static final int PAPER_SECTION_TYPE_SECTION = 1;
    private static final int QUESTION_TYPE_SINGLE = 1;
    private static final int QUESTION_TYPE_MULTI = 2;
    private static final int QUESTION_TYPE_JUDGE = 3;
    private static final int QUESTION_TYPE_SUBJECTIVE = 4;
    private static final String PAPER_QUESTION_ORDER_BY = "p.sort_order asc,p.id asc";
    private static final String LESSON_RESOURCE_ORDER_BY = "c.is_primary desc,c.sort_order asc,c.id asc";

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
    private PaperInfoService paperInfoService;

    @Resource
    private PaperQuestionService paperQuestionService;

    @Resource
    private QuestionInfoService questionInfoService;

    @Resource
    private CourseAssessmentSubmitService courseAssessmentSubmitService;

    @Resource
    private CourseAssessmentSubmitQuestionService courseAssessmentSubmitQuestionService;

    public CourseHomeworkDetailVO getHomeworkDetail(String courseId, String lessonId) {
        HomeworkContext context = loadHomeworkContext(courseId, lessonId);
        CourseAssessmentSubmit submit = getCurrentSubmit(context.lesson.getLessonId(), context.paperInfo.getPaperId(), context.currentStudent.getUserId());
        return buildHomeworkDetail(context, submit);
    }

    @Transactional(rollbackFor = Exception.class)
    public CourseHomeworkSubmitVO startHomework(CourseHomeworkStartDTO dto) {
        HomeworkContext context = loadHomeworkContext(dto.getCourseId(), dto.getLessonId());
        CourseAssessmentSubmit submit = getCurrentSubmit(context.lesson.getLessonId(), context.paperInfo.getPaperId(), context.currentStudent.getUserId());
        if (submit == null) {
            submit = new CourseAssessmentSubmit();
            submit.setTaskId(context.lesson.getLessonId());
            submit.setTaskType(TASK_TYPE_HOMEWORK);
            submit.setPaperId(context.paperInfo.getPaperId());
            submit.setUserId(context.currentStudent.getUserId());
            submit.setSubmitStatus(SUBMIT_STATUS_PENDING);
            submit.setJudgeStatus(JUDGE_STATUS_NOT_STARTED);
            submit.setUsedSeconds(0);
            courseAssessmentSubmitService.add(submit);
        }
        return buildSubmitVO(submit);
    }

    @Transactional(rollbackFor = Exception.class)
    public CourseHomeworkSubmitVO saveAnswer(CourseHomeworkAnswerSaveDTO dto) {
        CourseAssessmentSubmit submit = getEditableSubmit(dto.getSubmitId());
        PaperQuestion paperQuestion = paperQuestionService.getPaperQuestionById(dto.getPaperQuestionId());
        if (paperQuestion == null || !Objects.equals(paperQuestion.getQuestionId(), dto.getQuestionId())
                || !Objects.equals(paperQuestion.getPaperId(), submit.getPaperId())) {
            throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "题目信息不存在或不匹配");
        }

        Date now = new Date();
        CourseAssessmentSubmit submitUpdate = new CourseAssessmentSubmit();
        submitUpdate.setSubmitStatus(SUBMIT_STATUS_ANSWERING);
        if (submit.getStartedTime() == null) {
            submitUpdate.setStartedTime(now);
            submit.setStartedTime(now);
        }
        submit.setSubmitStatus(SUBMIT_STATUS_ANSWERING);
        courseAssessmentSubmitService.updateCourseAssessmentSubmitBySubmitId(submitUpdate, submit.getSubmitId());

        CourseAssessmentSubmitQuestion question = new CourseAssessmentSubmitQuestion();
        question.setSubmitId(submit.getSubmitId());
        question.setTaskId(toQuestionTaskId(submit.getTaskId()));
        question.setPaperId(dto.getPaperQuestionId());
        question.setQuestionId(dto.getQuestionId());
        question.setAnswerContent(normalizeStoredAnswer(dto.getAnswerContent()));
        question.setFinalScore(BigDecimal.ZERO);
        question.setJudgeStatus(JUDGE_STATUS_NOT_STARTED);
        courseAssessmentSubmitQuestionService.addOrUpdateBatch(List.of(question));
        return buildSubmitVO(submit);
    }

    @Transactional(rollbackFor = Exception.class)
    public CourseHomeworkSubmitVO saveDraft(CourseHomeworkDraftSaveDTO dto) {
        CourseAssessmentSubmit submit = getEditableSubmit(dto.getSubmitId());
        CourseAssessmentSubmit update = new CourseAssessmentSubmit();
        update.setSubmitStatus(SUBMIT_STATUS_DRAFT);
        update.setSubmitContent(StringTools.trim(dto.getSubmitContent()));
        update.setUsedSeconds(safeInt(dto.getUsedSeconds()));
        if (submit.getStartedTime() == null) {
            Date now = new Date();
            update.setStartedTime(now);
            submit.setStartedTime(now);
        }
        submit.setSubmitStatus(SUBMIT_STATUS_DRAFT);
        submit.setSubmitContent(update.getSubmitContent());
        submit.setUsedSeconds(update.getUsedSeconds());
        courseAssessmentSubmitService.updateCourseAssessmentSubmitBySubmitId(update, submit.getSubmitId());
        return buildSubmitVO(submit);
    }

    @Transactional(rollbackFor = Exception.class)
    public CourseHomeworkSubmitVO submitHomework(CourseHomeworkSubmitDTO dto) {
        CourseAssessmentSubmit submit = getEditableSubmit(dto.getSubmitId());
        HomeworkContext context = loadHomeworkContextByTaskId(submit.getTaskId());
        List<PaperQuestion> actualQuestionList = context.paperQuestionList.stream()
                .filter(item -> !Objects.equals(item.getSectionType(), PAPER_SECTION_TYPE_SECTION))
                .filter(item -> item.getQuestionId() != null)
                .toList();
        Map<Integer, QuestionInfo> questionInfoMap = loadQuestionInfoMap(actualQuestionList);
        Map<Integer, CourseAssessmentSubmitQuestion> answerMap = loadSubmitQuestionMap(submit.getSubmitId());

        BigDecimal objectiveScore = BigDecimal.ZERO;
        BigDecimal subjectiveScore = BigDecimal.ZERO;
        boolean hasSubjective = false;
        List<CourseAssessmentSubmitQuestion> questionUpdateList = new ArrayList<>();
        for (PaperQuestion paperQuestion : actualQuestionList) {
            CourseAssessmentSubmitQuestion answer = answerMap.get(paperQuestion.getQuestionId());
            if (answer == null) {
                answer = new CourseAssessmentSubmitQuestion();
                answer.setSubmitId(submit.getSubmitId());
                answer.setTaskId(toQuestionTaskId(submit.getTaskId()));
                answer.setPaperId(paperQuestion.getId());
                answer.setQuestionId(paperQuestion.getQuestionId());
            }
            QuestionInfo questionInfo = questionInfoMap.get(paperQuestion.getQuestionId());
            BigDecimal score = defaultScore(paperQuestion.getQuestionScore());
            int questionType = safeInt(paperQuestion.getQuestionType());
            if (questionType == QUESTION_TYPE_SUBJECTIVE) {
                answer.setFinalScore(BigDecimal.ZERO);
                answer.setJudgeStatus(JUDGE_STATUS_WAIT_MANUAL);
                hasSubjective = true;
            } else {
                boolean correct = isObjectiveAnswerCorrect(questionType, questionInfo, answer.getAnswerContent());
                BigDecimal finalScore = correct ? score : BigDecimal.ZERO;
                answer.setFinalScore(finalScore);
                answer.setJudgeStatus(JUDGE_STATUS_AUTO_DONE);
                objectiveScore = objectiveScore.add(finalScore);
            }
            questionUpdateList.add(answer);
        }
        if (!questionUpdateList.isEmpty()) {
            courseAssessmentSubmitQuestionService.addOrUpdateBatch(questionUpdateList);
        }

        Date now = new Date();
        CourseAssessmentSubmit update = new CourseAssessmentSubmit();
        update.setSubmitStatus(SUBMIT_STATUS_SUBMITTED);
        update.setJudgeStatus(hasSubjective ? JUDGE_STATUS_WAIT_MANUAL : JUDGE_STATUS_AUTO_DONE);
        update.setSubmitTime(now);
        update.setSubmitContent(StringTools.trim(dto.getSubmitContent()));
        update.setUsedSeconds(safeInt(dto.getUsedSeconds()));
        update.setObjectiveScore(scaleScore(objectiveScore));
        update.setSubjectiveScore(scaleScore(subjectiveScore));
        if (submit.getStartedTime() == null) {
            update.setStartedTime(now);
            submit.setStartedTime(now);
        }
        courseAssessmentSubmitService.updateCourseAssessmentSubmitBySubmitId(update, submit.getSubmitId());

        submit.setSubmitStatus(SUBMIT_STATUS_SUBMITTED);
        submit.setJudgeStatus(update.getJudgeStatus());
        submit.setSubmitTime(now);
        submit.setSubmitContent(update.getSubmitContent());
        submit.setUsedSeconds(update.getUsedSeconds());
        submit.setObjectiveScore(update.getObjectiveScore());
        submit.setSubjectiveScore(update.getSubjectiveScore());
        return buildSubmitVO(submit);
    }

    private HomeworkContext loadHomeworkContext(String courseId, String lessonId) {
        String normalizedCourseId = StringTools.trim(courseId);
        String normalizedLessonId = StringTools.trim(lessonId);
        if (StringTools.isEmpty(normalizedCourseId) || StringTools.isEmpty(normalizedLessonId)) {
            throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "作业参数不能为空");
        }
        UserInfo currentStudent = getCurrentStudent();
        ensureStudentCanAccessCourse(currentStudent, normalizedCourseId);

        CourseInfo courseInfo = courseInfoService.getCourseInfoByCourseId(normalizedCourseId);
        if (courseInfo == null || !Objects.equals(courseInfo.getStatus(), 1) || !Objects.equals(courseInfo.getRecordStatus(), 1)) {
            throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "课程不存在、未录制完成或已下线");
        }

        CourseChapterLesson lesson = courseChapterLessonService.getCourseChapterLessonByLessonId(normalizedLessonId);
        if (lesson == null || !Objects.equals(lesson.getCourseId(), normalizedCourseId)) {
            throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "课时不存在");
        }

        CourseChapter chapter = courseChapterService.getCourseChapterByChapterId(lesson.getChapterId());
        if (chapter == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "章节信息不存在");
        }

        String paperId = loadHomeworkPaperId(lesson.getLessonId());
        PaperInfo paperInfo = paperInfoService.getPaperInfoByPaperId(paperId);
        if (paperInfo == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "课后作业试卷不存在");
        }

        List<PaperQuestion> paperQuestionList = loadPaperQuestionList(paperId);
        HomeworkContext context = new HomeworkContext();
        context.currentStudent = currentStudent;
        context.courseInfo = courseInfo;
        context.chapter = chapter;
        context.lesson = lesson;
        context.paperInfo = paperInfo;
        context.paperQuestionList = paperQuestionList;
        return context;
    }

    private HomeworkContext loadHomeworkContextByTaskId(String taskId) {
        CourseChapterLesson lesson = courseChapterLessonService.getCourseChapterLessonByLessonId(taskId);
        if (lesson == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "作业课时不存在");
        }
        return loadHomeworkContext(lesson.getCourseId(), lesson.getLessonId());
    }

    private CourseHomeworkDetailVO buildHomeworkDetail(HomeworkContext context, CourseAssessmentSubmit submit) {
        CourseHomeworkDetailVO vo = new CourseHomeworkDetailVO();
        vo.setCourseId(context.courseInfo.getCourseId());
        vo.setCourseName(context.courseInfo.getCourseName());
        vo.setChapterId(context.chapter.getChapterId());
        vo.setChapterName(context.chapter.getChapterName());
        vo.setLessonId(context.lesson.getLessonId());
        vo.setLessonName(context.lesson.getLessonName());
        vo.setPaperId(context.paperInfo.getPaperId());
        vo.setPaperName(context.paperInfo.getPaperName());
        vo.setPaperType(context.paperInfo.getPaperType());
        vo.setPaperTypeText(resolvePaperTypeText(context.paperInfo.getPaperType()));
        vo.setTotalScore(defaultScore(context.paperInfo.getTotalScore()));

        Map<Integer, CourseAssessmentSubmitQuestion> submitQuestionMap = submit == null
                ? Map.of()
                : loadSubmitQuestionMap(submit.getSubmitId());
        Map<Integer, QuestionInfo> questionInfoMap = loadQuestionInfoMap(context.paperQuestionList);

        LinkedHashMap<Integer, CourseHomeworkSectionVO> sectionMap = new LinkedHashMap<>();
        int answeredCount = 0;
        int questionCount = 0;
        for (PaperQuestion item : context.paperQuestionList) {
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
            sectionVO.setTotalScore(sectionVO.getTotalScore().add(defaultScore(item.getQuestionScore())));
            sectionVO.getQuestionList().add(questionVO);
        }

        vo.setQuestionCount(questionCount);
        vo.setAnsweredCount(answeredCount);
        if (submit != null) {
            vo.setSubmitId(submit.getSubmitId());
            vo.setSubmitStatus(submit.getSubmitStatus());
            vo.setSubmitStatusText(resolveSubmitStatusText(submit.getSubmitStatus()));
            vo.setJudgeStatus(submit.getJudgeStatus());
            vo.setJudgeStatusText(resolveJudgeStatusText(submit.getJudgeStatus()));
            vo.setStartedTime(submit.getStartedTime());
            vo.setSubmitTime(submit.getSubmitTime());
            vo.setUsedSeconds(safeInt(submit.getUsedSeconds()));
            vo.setSubmitContent(submit.getSubmitContent());
            vo.setTeacherComment(submit.getTeacherComment());
            vo.setStarted(true);
            vo.setSubmitted(Objects.equals(submit.getSubmitStatus(), SUBMIT_STATUS_SUBMITTED));
            vo.setEditable(!Objects.equals(submit.getSubmitStatus(), SUBMIT_STATUS_SUBMITTED));
        } else {
            vo.setSubmitStatus(SUBMIT_STATUS_PENDING);
            vo.setSubmitStatusText(resolveSubmitStatusText(SUBMIT_STATUS_PENDING));
            vo.setJudgeStatus(JUDGE_STATUS_NOT_STARTED);
            vo.setJudgeStatusText(resolveJudgeStatusText(JUDGE_STATUS_NOT_STARTED));
            vo.setUsedSeconds(0);
            vo.setStarted(false);
            vo.setSubmitted(false);
            vo.setEditable(true);
        }
        vo.setSectionList(new ArrayList<>(sectionMap.values()));
        return vo;
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
        if (submitId == null) {
            return Map.of();
        }
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

    private List<PaperQuestion> loadPaperQuestionList(String paperId) {
        com.smart.campus.entity.query.PaperQuestionQuery query = new com.smart.campus.entity.query.PaperQuestionQuery();
        query.setPaperId(paperId);
        query.setOrderBy(PAPER_QUESTION_ORDER_BY);
        return paperQuestionService.findListByParam(query);
    }

    private String loadHomeworkPaperId(String lessonId) {
        CourseChapterLessonResourceQuery query = new CourseChapterLessonResourceQuery();
        query.setLessonId(lessonId);
        query.setResourceType(LESSON_RESOURCE_ROLE_PAPER);
        query.setOrderBy(LESSON_RESOURCE_ORDER_BY);
        List<CourseChapterLessonResource> resourceList = courseChapterLessonResourceService.findListByParam(query);
        for (CourseChapterLessonResource resource : resourceList) {
            String resourceId = StringTools.trim(resource.getResourceId());
            if (!StringTools.isEmpty(resourceId)) {
                return resourceId;
            }
        }
        throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "当前课时暂未配置课后作业");
    }

    private CourseAssessmentSubmit getCurrentSubmit(String taskId, String paperId, Integer userId) {
        return courseAssessmentSubmitService.getCourseAssessmentSubmitByTaskIdAndUserIdAndPaperId(taskId, userId, paperId);
    }

    private CourseAssessmentSubmit getEditableSubmit(Long submitId) {
        CourseAssessmentSubmit submit = getOwnedSubmit(submitId);
        if (Objects.equals(submit.getSubmitStatus(), SUBMIT_STATUS_SUBMITTED)) {
            throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "作业已提交，不能再修改");
        }
        return submit;
    }

    private CourseAssessmentSubmit getOwnedSubmit(Long submitId) {
        UserInfo currentStudent = getCurrentStudent();
        CourseAssessmentSubmit submit = courseAssessmentSubmitService.getCourseAssessmentSubmitBySubmitId(submitId);
        if (submit == null || !Objects.equals(submit.getUserId(), currentStudent.getUserId())) {
            throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "作业提交记录不存在");
        }
        return submit;
    }

    private UserInfo getCurrentStudent() {
        LoginUserVO loginUser = LoginUserContextHolder.get();
        if (loginUser == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_901);
        }
        if (!Objects.equals(loginUser.getRoleType(), USER_ROLE_STUDENT)) {
            throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "请使用学生账号提交作业");
        }
        UserInfo currentStudent = userInfoService.getUserInfoByUserId(loginUser.getUserId());
        if (currentStudent == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "学生信息不存在");
        }
        return currentStudent;
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
                // ignore
            }
        }
        return result;
    }

    private boolean isObjectiveAnswerCorrect(int questionType, QuestionInfo questionInfo, String answerContent) {
        if (questionInfo == null) {
            return false;
        }
        Set<String> correctTokens = normalizeAnswerTokenSet(questionInfo.getCorrectAnswer());
        Set<String> submitTokens = normalizeAnswerTokenSet(answerContent);
        if (correctTokens.isEmpty() || submitTokens.isEmpty()) {
            return false;
        }
        if (questionType == QUESTION_TYPE_MULTI) {
            return correctTokens.equals(submitTokens);
        }
        return correctTokens.size() == 1 && submitTokens.size() == 1
                && Objects.equals(correctTokens.iterator().next(), submitTokens.iterator().next());
    }

    private Set<String> normalizeAnswerTokenSet(String answerContent) {
        LinkedHashSet<String> result = new LinkedHashSet<>();
        if (StringTools.isEmpty(answerContent)) {
            return result;
        }
        String normalized = answerContent.replace("[", "")
                .replace("]", "")
                .replace("\"", "")
                .replace("'", "")
                .replace("，", ",")
                .trim();
        if (StringTools.isEmpty(normalized)) {
            return result;
        }
        for (String item : normalized.split(",")) {
            String token = normalizeAnswerToken(item);
            if (!StringTools.isEmpty(token)) {
                result.add(token);
            }
        }
        if (result.isEmpty()) {
            String token = normalizeAnswerToken(normalized);
            if (!StringTools.isEmpty(token)) {
                result.add(token);
            }
        }
        return result;
    }

    private String normalizeAnswerToken(String token) {
        String value = StringTools.trim(token);
        if (StringTools.isEmpty(value)) {
            return "";
        }
        return value.toUpperCase();
    }

    private String normalizeStoredAnswer(String answerContent) {
        String value = StringTools.trim(answerContent);
        return StringTools.isEmpty(value) ? null : value;
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

    private CourseHomeworkSubmitVO buildSubmitVO(CourseAssessmentSubmit submit) {
        CourseHomeworkSubmitVO vo = new CourseHomeworkSubmitVO();
        vo.setSubmitId(submit.getSubmitId());
        vo.setSubmitStatus(submit.getSubmitStatus());
        vo.setSubmitStatusText(resolveSubmitStatusText(submit.getSubmitStatus()));
        vo.setJudgeStatus(submit.getJudgeStatus());
        vo.setJudgeStatusText(resolveJudgeStatusText(submit.getJudgeStatus()));
        vo.setObjectiveScore(scaleScore(submit.getObjectiveScore()));
        vo.setSubjectiveScore(scaleScore(submit.getSubjectiveScore()));
        vo.setTotalScore(scaleScore(defaultScore(submit.getObjectiveScore()).add(defaultScore(submit.getSubjectiveScore()))));
        return vo;
    }

    private String resolvePaperTypeText(Integer paperType) {
        if (Objects.equals(paperType, 1)) {
            return "课后作业";
        }
        if (Objects.equals(paperType, 2)) {
            return "考试试卷";
        }
        return "试卷";
    }

    private String resolveSubmitStatusText(Integer submitStatus) {
        if (Objects.equals(submitStatus, SUBMIT_STATUS_PENDING)) {
            return "待开始";
        }
        if (Objects.equals(submitStatus, SUBMIT_STATUS_ANSWERING)) {
            return "作答中";
        }
        if (Objects.equals(submitStatus, SUBMIT_STATUS_DRAFT)) {
            return "草稿";
        }
        if (Objects.equals(submitStatus, SUBMIT_STATUS_SUBMITTED)) {
            return "已提交";
        }
        return "未知状态";
    }

    private String resolveJudgeStatusText(Integer judgeStatus) {
        if (Objects.equals(judgeStatus, JUDGE_STATUS_NOT_STARTED)) {
            return "未批改";
        }
        if (Objects.equals(judgeStatus, JUDGE_STATUS_AUTO_DONE)) {
            return "自动判分完成";
        }
        if (Objects.equals(judgeStatus, JUDGE_STATUS_WAIT_MANUAL)) {
            return "待人工批改";
        }
        if (Objects.equals(judgeStatus, JUDGE_STATUS_MANUAL_DONE)) {
            return "人工批改完成";
        }
        return "未知状态";
    }

    private static class HomeworkContext {
        private UserInfo currentStudent;
        private CourseInfo courseInfo;
        private CourseChapter chapter;
        private CourseChapterLesson lesson;
        private PaperInfo paperInfo;
        private List<PaperQuestion> paperQuestionList = List.of();
    }
}
