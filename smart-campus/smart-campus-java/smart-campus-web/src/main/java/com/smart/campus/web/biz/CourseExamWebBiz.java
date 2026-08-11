package com.smart.campus.web.biz;

import com.alibaba.fastjson2.JSON;
import com.smart.campus.entity.dto.CourseExamAnswerSaveDTO;
import com.smart.campus.entity.dto.CourseExamDraftSaveDTO;
import com.smart.campus.entity.dto.CourseExamStartDTO;
import com.smart.campus.entity.dto.CourseExamSubmitDTO;
import com.smart.campus.entity.enums.ResponseCodeEnum;
import com.smart.campus.entity.enums.UserRoleTypeEnum;
import com.smart.campus.entity.po.CourseAssessmentSubmit;
import com.smart.campus.entity.po.CourseAssessmentSubmitQuestion;
import com.smart.campus.entity.po.CourseInfo;
import com.smart.campus.entity.po.ExamClass;
import com.smart.campus.entity.po.ExamInfo;
import com.smart.campus.entity.po.PaperInfo;
import com.smart.campus.entity.po.PaperQuestion;
import com.smart.campus.entity.po.QuestionInfo;
import com.smart.campus.entity.po.QuestionOption;
import com.smart.campus.entity.po.UserInfo;
import com.smart.campus.entity.query.CourseAssessmentSubmitQuestionQuery;
import com.smart.campus.entity.query.ExamClassQuery;
import com.smart.campus.entity.query.ExamInfoQuery;
import com.smart.campus.entity.query.QuestionOptionQuery;
import com.smart.campus.entity.vo.CourseAssessmentQuestionOptionVO;
import com.smart.campus.entity.vo.CourseExamDetailVO;
import com.smart.campus.entity.vo.CourseExamListItemVO;
import com.smart.campus.entity.vo.CourseHomeworkQuestionVO;
import com.smart.campus.entity.vo.CourseHomeworkSectionVO;
import com.smart.campus.entity.vo.CourseHomeworkSubmitVO;
import com.smart.campus.entity.vo.LoginUserVO;
import com.smart.campus.exception.BusinessException;
import com.smart.campus.service.CourseAssessmentSubmitQuestionService;
import com.smart.campus.service.CourseAssessmentSubmitService;
import com.smart.campus.service.CourseInfoService;
import com.smart.campus.service.ExamClassService;
import com.smart.campus.service.ExamInfoService;
import com.smart.campus.service.PaperInfoService;
import com.smart.campus.service.PaperQuestionService;
import com.smart.campus.service.QuestionInfoService;
import com.smart.campus.service.QuestionOptionService;
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
import java.util.Comparator;
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
public class CourseExamWebBiz {

    private static final int USER_ROLE_STUDENT = UserRoleTypeEnum.STUDENT.getCode();
    private static final int TASK_TYPE_EXAM = 2;
    private static final int SUBMIT_STATUS_PENDING = 0;
    private static final int SUBMIT_STATUS_ANSWERING = 1;
    private static final int SUBMIT_STATUS_DRAFT = 2;
    private static final int SUBMIT_STATUS_SUBMITTED = 3;
    private static final int JUDGE_STATUS_NOT_STARTED = 0;
    private static final int JUDGE_STATUS_AUTO_DONE = 1;
    private static final int JUDGE_STATUS_WAIT_MANUAL = 2;
    private static final int JUDGE_STATUS_MANUAL_DONE = 3;
    private static final int PAPER_SECTION_TYPE_SECTION = 1;
    private static final int QUESTION_TYPE_MULTI = 2;
    private static final int QUESTION_TYPE_SUBJECTIVE = 4;
    private static final String PAPER_QUESTION_ORDER_BY = "p.sort_order asc,p.id asc";
    private static final String QUESTION_OPTION_ORDER_BY = "q.sort_order asc,q.option_id asc";

    @Resource
    private UserInfoService userInfoService;

    @Resource
    private ExamInfoService examInfoService;

    @Resource
    private ExamClassService examClassService;

    @Resource
    private CourseInfoService courseInfoService;

    @Resource
    private PaperInfoService paperInfoService;

    @Resource
    private PaperQuestionService paperQuestionService;

    @Resource
    private QuestionInfoService questionInfoService;

    @Resource
    private QuestionOptionService questionOptionService;

    @Resource
    private CourseAssessmentSubmitService courseAssessmentSubmitService;

    @Resource
    private CourseAssessmentSubmitQuestionService courseAssessmentSubmitQuestionService;

    public List<CourseExamListItemVO> loadMyExamList() {
        UserInfo currentStudent = getCurrentStudent();
        List<Integer> classIdList = parseClassIds(currentStudent.getClassId());
        if (classIdList.isEmpty()) {
            return List.of();
        }
        ExamClassQuery examClassQuery = new ExamClassQuery();
        List<ExamClass> examClassList = examClassService.findListByParam(examClassQuery).stream()
                .filter(item -> classIdList.contains(item.getClassId()))
                .toList();
        if (examClassList.isEmpty()) {
            return List.of();
        }
        Set<String> examIdSet = examClassList.stream().map(ExamClass::getExamId).filter(Objects::nonNull).collect(Collectors.toSet());
        ExamInfoQuery examQuery = new ExamInfoQuery();
        examQuery.setStatus(1);
        examQuery.setOrderBy("e.start_time desc,e.create_time desc");
        List<ExamInfo> examList = examInfoService.findListByParam(examQuery).stream()
                .filter(item -> examIdSet.contains(item.getExamId()))
                .sorted(Comparator.comparing(ExamInfo::getStartTime, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();

        Set<String> courseIdSet = examList.stream().map(ExamInfo::getCourseId).filter(Objects::nonNull).collect(Collectors.toSet());
        Set<String> paperIdSet = examList.stream().map(ExamInfo::getPaperId).filter(Objects::nonNull).collect(Collectors.toSet());
        Set<Integer> teacherIdSet = examList.stream().map(ExamInfo::getTeacherId).filter(Objects::nonNull).collect(Collectors.toSet());

        Map<String, CourseInfo> courseMap = loadCourseMap(courseIdSet);
        Map<String, PaperInfo> paperMap = loadPaperMap(paperIdSet);
        Map<Integer, UserInfo> teacherMap = loadUserMap(teacherIdSet);

        List<CourseExamListItemVO> result = new ArrayList<>();
        Date now = new Date();
        for (ExamInfo examInfo : examList) {
            CourseAssessmentSubmit submit = courseAssessmentSubmitService
                    .getCourseAssessmentSubmitByTaskIdAndUserIdAndPaperId(examInfo.getExamId(), currentStudent.getUserId(), examInfo.getPaperId());
            CourseExamListItemVO vo = new CourseExamListItemVO();
            vo.setExamId(examInfo.getExamId());
            vo.setExamName(examInfo.getExamName());
            vo.setCourseId(examInfo.getCourseId());
            vo.setCourseName(defaultString(courseMap.get(examInfo.getCourseId()) == null ? null : courseMap.get(examInfo.getCourseId()).getCourseName()));
            vo.setPaperId(examInfo.getPaperId());
            vo.setPaperName(defaultString(paperMap.get(examInfo.getPaperId()) == null ? null : paperMap.get(examInfo.getPaperId()).getPaperName()));
            vo.setStartTime(examInfo.getStartTime());
            vo.setEndTime(examInfo.getEndTime());
            vo.setTotalScore(defaultScore(paperMap.get(examInfo.getPaperId()) == null ? null : paperMap.get(examInfo.getPaperId()).getTotalScore()));
            if (submit != null) {
                vo.setSubmitStatus(submit.getSubmitStatus());
                vo.setSubmitStatusText(resolveSubmitStatusText(submit.getSubmitStatus()));
                vo.setJudgeStatus(submit.getJudgeStatus());
                vo.setJudgeStatusText(resolveJudgeStatusText(submit.getJudgeStatus()));
                vo.setStarted(!Objects.equals(submit.getSubmitStatus(), SUBMIT_STATUS_PENDING));
                vo.setSubmitted(Objects.equals(submit.getSubmitStatus(), SUBMIT_STATUS_SUBMITTED));
                vo.setFinalScore(scaleScore(defaultScore(submit.getObjectiveScore()).add(defaultScore(submit.getSubjectiveScore()))));
            } else {
                vo.setSubmitStatus(SUBMIT_STATUS_PENDING);
                vo.setSubmitStatusText(resolveSubmitStatusText(SUBMIT_STATUS_PENDING));
                vo.setJudgeStatus(JUDGE_STATUS_NOT_STARTED);
                vo.setJudgeStatusText(resolveJudgeStatusText(JUDGE_STATUS_NOT_STARTED));
                vo.setStarted(false);
                vo.setSubmitted(false);
                vo.setFinalScore(BigDecimal.ZERO);
            }
            vo.setExamStatusText(resolveExamStatusText(examInfo, submit, now));
            result.add(vo);
        }
        return result;
    }

    public CourseExamDetailVO getExamDetail(String examId) {
        ExamContext context = loadExamContext(examId);
        CourseAssessmentSubmit submit = getCurrentSubmit(context.examInfo.getExamId(), context.paperInfo.getPaperId(), context.currentStudent.getUserId());
        return buildExamDetail(context, submit);
    }

    @Transactional(rollbackFor = Exception.class)
    public CourseHomeworkSubmitVO startExam(CourseExamStartDTO dto) {
        ExamContext context = loadExamContext(dto.getExamId());
        validateExamEditable(context.examInfo, null);
        CourseAssessmentSubmit submit = getCurrentSubmit(context.examInfo.getExamId(), context.paperInfo.getPaperId(), context.currentStudent.getUserId());
        if (submit == null) {
            submit = new CourseAssessmentSubmit();
            submit.setTaskId(context.examInfo.getExamId());
            submit.setTaskType(TASK_TYPE_EXAM);
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
    public CourseHomeworkSubmitVO saveAnswer(CourseExamAnswerSaveDTO dto) {
        CourseAssessmentSubmit submit = getEditableSubmit(dto.getSubmitId());
        ExamContext context = loadExamContext(submit.getTaskId());
        validateExamEditable(context.examInfo, submit);
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
    public CourseHomeworkSubmitVO saveDraft(CourseExamDraftSaveDTO dto) {
        CourseAssessmentSubmit submit = getEditableSubmit(dto.getSubmitId());
        ExamContext context = loadExamContext(submit.getTaskId());
        validateExamEditable(context.examInfo, submit);
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
    public CourseHomeworkSubmitVO submitExam(CourseExamSubmitDTO dto) {
        CourseAssessmentSubmit submit = getEditableSubmit(dto.getSubmitId());
        ExamContext context = loadExamContext(submit.getTaskId());
        validateExamEditable(context.examInfo, submit);
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

    private ExamContext loadExamContext(String examId) {
        String normalizedExamId = StringTools.trim(examId);
        if (StringTools.isEmpty(normalizedExamId)) {
            throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "考试参数不能为空");
        }
        UserInfo currentStudent = getCurrentStudent();
        ExamInfo examInfo = examInfoService.getExamInfoByExamId(normalizedExamId);
        if (examInfo == null || !Objects.equals(examInfo.getStatus(), 1)) {
            throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "考试不存在或未发布");
        }
        ensureStudentCanAccessExam(currentStudent, normalizedExamId);
        CourseInfo courseInfo = courseInfoService.getCourseInfoByCourseId(examInfo.getCourseId());
        if (courseInfo == null || !Objects.equals(courseInfo.getStatus(), 1) || !Objects.equals(courseInfo.getRecordStatus(), 1)) {
            throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "课程不存在、未录制完成或已下线");
        }
        PaperInfo paperInfo = paperInfoService.getPaperInfoByPaperId(examInfo.getPaperId());
        if (paperInfo == null || !Objects.equals(paperInfo.getPaperType(), 2)) {
            throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "考试试卷不存在");
        }
        ExamContext context = new ExamContext();
        context.currentStudent = currentStudent;
        context.examInfo = examInfo;
        context.courseInfo = courseInfo;
        context.paperInfo = paperInfo;
        context.paperQuestionList = loadPaperQuestionList(paperInfo.getPaperId());
        return context;
    }

    private void ensureStudentCanAccessExam(UserInfo student, String examId) {
        List<Integer> classIdList = parseClassIds(student.getClassId());
        if (classIdList.isEmpty()) {
            throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "当前学生未分配班级");
        }
        ExamClassQuery query = new ExamClassQuery();
        query.setExamId(examId);
        List<ExamClass> examClassList = examClassService.findListByParam(query);
        for (ExamClass examClass : examClassList) {
            if (classIdList.contains(examClass.getClassId())) {
                return;
            }
        }
        throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "当前学生无权参加该考试");
    }

    private CourseExamDetailVO buildExamDetail(ExamContext context, CourseAssessmentSubmit submit) {
        CourseExamDetailVO vo = new CourseExamDetailVO();
        vo.setExamId(context.examInfo.getExamId());
        vo.setExamName(context.examInfo.getExamName());
        vo.setCourseId(context.courseInfo.getCourseId());
        vo.setCourseName(context.courseInfo.getCourseName());
        vo.setPaperId(context.paperInfo.getPaperId());
        vo.setPaperName(context.paperInfo.getPaperName());
        vo.setStartTime(context.examInfo.getStartTime());
        vo.setEndTime(context.examInfo.getEndTime());
        vo.setTotalScore(defaultScore(context.paperInfo.getTotalScore()));
        Date now = new Date();

        List<PaperQuestion> actualQuestionList = context.paperQuestionList.stream()
                .filter(item -> !Objects.equals(item.getSectionType(), PAPER_SECTION_TYPE_SECTION))
                .filter(item -> item.getQuestionId() != null)
                .toList();
        vo.setQuestionCount(actualQuestionList.size());
        Map<Integer, QuestionInfo> questionInfoMap = loadQuestionInfoMap(actualQuestionList);
        Map<Integer, List<QuestionOption>> optionMap = loadQuestionOptionMap(questionInfoMap.keySet());
        Map<Integer, CourseAssessmentSubmitQuestion> answerMap = submit == null ? Map.of() : loadSubmitQuestionMap(submit.getSubmitId());
        List<CourseHomeworkSectionVO> sectionList = buildSectionList(context.paperQuestionList, questionInfoMap, optionMap, answerMap);
        vo.setSectionList(sectionList);
        vo.setAnsweredCount(countAnswered(answerMap));

        if (submit != null) {
            vo.setSubmitId(submit.getSubmitId());
            vo.setSubmitStatus(submit.getSubmitStatus());
            vo.setSubmitStatusText(resolveSubmitStatusText(submit.getSubmitStatus()));
            vo.setJudgeStatus(submit.getJudgeStatus());
            vo.setJudgeStatusText(resolveJudgeStatusText(submit.getJudgeStatus()));
            vo.setUsedSeconds(safeInt(submit.getUsedSeconds()));
            vo.setSubmitContent(submit.getSubmitContent());
            vo.setTeacherComment(submit.getTeacherComment());
            vo.setStartedTime(submit.getStartedTime());
            vo.setSubmitTime(submit.getSubmitTime());
            vo.setStarted(!Objects.equals(submit.getSubmitStatus(), SUBMIT_STATUS_PENDING));
            vo.setSubmitted(Objects.equals(submit.getSubmitStatus(), SUBMIT_STATUS_SUBMITTED));
        } else {
            vo.setSubmitStatus(SUBMIT_STATUS_PENDING);
            vo.setSubmitStatusText(resolveSubmitStatusText(SUBMIT_STATUS_PENDING));
            vo.setJudgeStatus(JUDGE_STATUS_NOT_STARTED);
            vo.setJudgeStatusText(resolveJudgeStatusText(JUDGE_STATUS_NOT_STARTED));
            vo.setUsedSeconds(0);
            vo.setStarted(false);
            vo.setSubmitted(false);
        }

        vo.setExamStatusText(resolveExamStatusText(context.examInfo, submit, now));
        vo.setEditable(canEditExam(context.examInfo, submit, now));
        vo.setRemainingSeconds(calculateRemainingSeconds(context.examInfo, submit, now));
        return vo;
    }

    private List<CourseHomeworkSectionVO> buildSectionList(List<PaperQuestion> paperQuestionList,
                                                           Map<Integer, QuestionInfo> questionInfoMap,
                                                           Map<Integer, List<QuestionOption>> optionMap,
                                                           Map<Integer, CourseAssessmentSubmitQuestion> answerMap) {
        List<CourseHomeworkSectionVO> sectionList = new ArrayList<>();
        Map<Integer, List<PaperQuestion>> childMap = buildPaperQuestionChildMap(paperQuestionList);
        for (PaperQuestion item : childMap.getOrDefault(0, List.of())) {
            if (Objects.equals(item.getSectionType(), PAPER_SECTION_TYPE_SECTION)) {
                CourseHomeworkSectionVO sectionVO = new CourseHomeworkSectionVO();
                sectionVO.setSectionId(item.getId());
                sectionVO.setSectionName(defaultString(item.getSectionName()));
                sectionVO.setSortOrder(item.getSortOrder());
                sectionVO.setTotalScore(BigDecimal.ZERO);
                appendSectionQuestions(sectionVO, item.getId(), childMap, questionInfoMap, optionMap, answerMap);
                sectionList.add(sectionVO);
                continue;
            }
            CourseHomeworkSectionVO fallbackSection = ensureFallbackSection(sectionList, item);
            appendQuestionToSection(
                    fallbackSection,
                    item,
                    questionInfoMap,
                    optionMap,
                    answerMap
            );
        }
        return sectionList;
    }

    private CourseHomeworkQuestionVO buildQuestionVO(PaperQuestion paperQuestion,
                                                     QuestionInfo questionInfo,
                                                     List<QuestionOption> optionList,
                                                     CourseAssessmentSubmitQuestion submitQuestion) {
        CourseHomeworkQuestionVO questionVO = new CourseHomeworkQuestionVO();
        questionVO.setPaperQuestionId(paperQuestion.getId());
        questionVO.setQuestionId(paperQuestion.getQuestionId());
        questionVO.setQuestionScore(defaultScore(paperQuestion.getQuestionScore()));
        questionVO.setSortOrder(paperQuestion.getSortOrder());
        PaperQuestionSnapshotVO snapshot = parseSnapshot(paperQuestion.getQuestionSnapshot());
        questionVO.setQuestionType(snapshot.questionType);
        questionVO.setQuestionTypeText(snapshot.questionTypeText);
        questionVO.setQuestionTitle(snapshot.questionTitle);
        questionVO.setDifficultyLevel(snapshot.difficultyLevel);
        questionVO.setDifficultyLevelText(snapshot.difficultyLevelText);
        questionVO.setCorrectAnswerText(snapshot.correctAnswerText);
        questionVO.setAnswerAnalysis(snapshot.answerAnalysis);
        questionVO.setQuestionImageResourceIdList(snapshot.questionImageResourceIdList);
        questionVO.setOptionList(snapshot.optionList);
        fillQuestionDetailFromQuestionInfo(questionVO, questionInfo, optionList);
        if (submitQuestion != null) {
            questionVO.setAnswerContent(defaultString(submitQuestion.getAnswerContent()));
            questionVO.setFinalScore(defaultScore(submitQuestion.getFinalScore()));
            questionVO.setJudgeStatus(submitQuestion.getJudgeStatus());
            questionVO.setAnswered(!StringTools.isEmpty(submitQuestion.getAnswerContent()));
        } else {
            questionVO.setAnswerContent("");
            questionVO.setFinalScore(BigDecimal.ZERO);
            questionVO.setJudgeStatus(JUDGE_STATUS_NOT_STARTED);
            questionVO.setAnswered(false);
        }
        return questionVO;
    }

    private PaperQuestionSnapshotVO parseSnapshot(String snapshotText) {
        if (StringTools.isEmpty(snapshotText)) {
            return new PaperQuestionSnapshotVO();
        }
        try {
            return JSON.parseObject(snapshotText, PaperQuestionSnapshotVO.class);
        } catch (Exception e) {
            return new PaperQuestionSnapshotVO();
        }
    }

    private Map<Integer, QuestionInfo> loadQuestionInfoMap(List<PaperQuestion> questionList) {
        Map<Integer, QuestionInfo> result = new HashMap<>();
        Set<Integer> questionIdSet = questionList.stream()
                .map(PaperQuestion::getQuestionId)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        for (Integer questionId : questionIdSet) {
            QuestionInfo questionInfo = questionInfoService.getQuestionInfoByQuestionId(questionId);
            if (questionInfo != null) {
                result.put(questionId, questionInfo);
            }
        }
        return result;
    }

    private Map<Integer, List<QuestionOption>> loadQuestionOptionMap(Set<Integer> questionIdSet) {
        Map<Integer, List<QuestionOption>> result = new HashMap<>();
        for (Integer questionId : questionIdSet) {
            if (questionId == null) {
                continue;
            }
            QuestionOptionQuery query = new QuestionOptionQuery();
            query.setQuestionId(questionId);
            query.setOrderBy(QUESTION_OPTION_ORDER_BY);
            List<QuestionOption> optionList = questionOptionService.findListByParam(query);
            result.put(questionId, optionList == null ? List.of() : optionList);
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

    private Map<Integer, List<PaperQuestion>> buildPaperQuestionChildMap(List<PaperQuestion> paperQuestionList) {
        Map<Integer, List<PaperQuestion>> childMap = new LinkedHashMap<>();
        Comparator<PaperQuestion> comparator = Comparator
                .comparing(PaperQuestion::getSortOrder, Comparator.nullsLast(Integer::compareTo))
                .thenComparing(PaperQuestion::getId, Comparator.nullsLast(Integer::compareTo));
        for (PaperQuestion item : paperQuestionList) {
            Integer parentId = item.getParentId() == null ? 0 : item.getParentId();
            childMap.computeIfAbsent(parentId, key -> new ArrayList<>()).add(item);
        }
        childMap.values().forEach(list -> list.sort(comparator));
        return childMap;
    }

    private void appendSectionQuestions(CourseHomeworkSectionVO sectionVO,
                                        Integer parentId,
                                        Map<Integer, List<PaperQuestion>> childMap,
                                        Map<Integer, QuestionInfo> questionInfoMap,
                                        Map<Integer, List<QuestionOption>> optionMap,
                                        Map<Integer, CourseAssessmentSubmitQuestion> answerMap) {
        for (PaperQuestion item : childMap.getOrDefault(parentId == null ? 0 : parentId, List.of())) {
            if (Objects.equals(item.getSectionType(), PAPER_SECTION_TYPE_SECTION)) {
                appendSectionQuestions(sectionVO, item.getId(), childMap, questionInfoMap, optionMap, answerMap);
                continue;
            }
            appendQuestionToSection(sectionVO, item, questionInfoMap, optionMap, answerMap);
        }
    }

    private void appendQuestionToSection(CourseHomeworkSectionVO sectionVO,
                                         PaperQuestion paperQuestion,
                                         Map<Integer, QuestionInfo> questionInfoMap,
                                         Map<Integer, List<QuestionOption>> optionMap,
                                         Map<Integer, CourseAssessmentSubmitQuestion> answerMap) {
        if (sectionVO == null || paperQuestion == null || paperQuestion.getQuestionId() == null) {
            return;
        }
        CourseHomeworkQuestionVO questionVO = buildQuestionVO(
                paperQuestion,
                questionInfoMap.get(paperQuestion.getQuestionId()),
                optionMap.getOrDefault(paperQuestion.getQuestionId(), List.of()),
                answerMap.get(paperQuestion.getQuestionId())
        );
        sectionVO.getQuestionList().add(questionVO);
        sectionVO.setTotalScore(defaultScore(sectionVO.getTotalScore()).add(defaultScore(questionVO.getQuestionScore())));
    }

    private CourseHomeworkSectionVO ensureFallbackSection(List<CourseHomeworkSectionVO> sectionList, PaperQuestion paperQuestion) {
        if (!sectionList.isEmpty()) {
            return sectionList.get(sectionList.size() - 1);
        }
        CourseHomeworkSectionVO fallbackSection = new CourseHomeworkSectionVO();
        fallbackSection.setSectionId(paperQuestion.getParentId());
        fallbackSection.setSectionName(defaultString(paperQuestion.getSectionName()));
        fallbackSection.setSortOrder(paperQuestion.getSortOrder());
        fallbackSection.setTotalScore(BigDecimal.ZERO);
        sectionList.add(fallbackSection);
        return fallbackSection;
    }

    private CourseAssessmentSubmit getCurrentSubmit(String taskId, String paperId, Integer userId) {
        return courseAssessmentSubmitService.getCourseAssessmentSubmitByTaskIdAndUserIdAndPaperId(taskId, userId, paperId);
    }

    private CourseAssessmentSubmit getEditableSubmit(Long submitId) {
        CourseAssessmentSubmit submit = getOwnedSubmit(submitId);
        if (Objects.equals(submit.getSubmitStatus(), SUBMIT_STATUS_SUBMITTED)) {
            throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "考试已提交，不能再修改");
        }
        return submit;
    }

    private CourseAssessmentSubmit getOwnedSubmit(Long submitId) {
        UserInfo currentStudent = getCurrentStudent();
        CourseAssessmentSubmit submit = courseAssessmentSubmitService.getCourseAssessmentSubmitBySubmitId(submitId);
        if (submit == null || !Objects.equals(submit.getUserId(), currentStudent.getUserId())) {
            throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "考试提交记录不存在");
        }
        return submit;
    }

    private UserInfo getCurrentStudent() {
        LoginUserVO loginUser = LoginUserContextHolder.get();
        if (loginUser == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_901);
        }
        if (!Objects.equals(loginUser.getRoleType(), USER_ROLE_STUDENT)) {
            throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "请使用学生账号参加考试");
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

    private int countAnswered(Map<Integer, CourseAssessmentSubmitQuestion> answerMap) {
        return (int) answerMap.values().stream()
                .filter(item -> !StringTools.isEmpty(item.getAnswerContent()))
                .count();
    }

    private boolean canEditExam(ExamInfo examInfo, CourseAssessmentSubmit submit, Date now) {
        if (submit != null && Objects.equals(submit.getSubmitStatus(), SUBMIT_STATUS_SUBMITTED)) {
            return false;
        }
        return !now.before(examInfo.getStartTime()) && !now.after(examInfo.getEndTime())
                && calculateRemainingSeconds(examInfo, submit, now) > 0;
    }

    private void validateExamEditable(ExamInfo examInfo, CourseAssessmentSubmit submit) {
        Date now = new Date();
        if (now.before(examInfo.getStartTime())) {
            throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "考试尚未开始");
        }
        if (now.after(examInfo.getEndTime())) {
            throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "考试已结束");
        }
        if (calculateRemainingSeconds(examInfo, submit, now) <= 0) {
            throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "考试作答时间已到");
        }
    }

    private int calculateRemainingSeconds(ExamInfo examInfo, CourseAssessmentSubmit submit, Date now) {
        long endRemain = Math.max(0L, (examInfo.getEndTime().getTime() - now.getTime()) / 1000);
        int usedSeconds = submit == null ? 0 : safeInt(submit.getUsedSeconds());
        if (submit != null && submit.getStartedTime() != null && !Objects.equals(submit.getSubmitStatus(), SUBMIT_STATUS_SUBMITTED)) {
            usedSeconds = Math.max(usedSeconds, (int) ((now.getTime() - submit.getStartedTime().getTime()) / 1000));
        }
        return (int) Math.max(0L, endRemain);
    }

    private String resolveExamStatusText(ExamInfo examInfo, CourseAssessmentSubmit submit, Date now) {
        if (submit != null && Objects.equals(submit.getSubmitStatus(), SUBMIT_STATUS_SUBMITTED)) {
            return "已交卷";
        }
        if (now.before(examInfo.getStartTime())) {
            return "未开始";
        }
        if (now.after(examInfo.getEndTime()) || calculateRemainingSeconds(examInfo, submit, now) <= 0) {
            return "已结束";
        }
        if (submit != null && (Objects.equals(submit.getSubmitStatus(), SUBMIT_STATUS_ANSWERING) || Objects.equals(submit.getSubmitStatus(), SUBMIT_STATUS_DRAFT))) {
            return "考试中";
        }
        return "待作答";
    }

    private Map<String, CourseInfo> loadCourseMap(Set<String> courseIdSet) {
        if (courseIdSet.isEmpty()) {
            return Map.of();
        }
        com.smart.campus.entity.query.CourseInfoQuery query = new com.smart.campus.entity.query.CourseInfoQuery();
        return courseInfoService.findListByParam(query).stream()
                .filter(item -> courseIdSet.contains(item.getCourseId()))
                .collect(Collectors.toMap(CourseInfo::getCourseId, item -> item, (left, right) -> left));
    }

    private Map<String, PaperInfo> loadPaperMap(Set<String> paperIdSet) {
        if (paperIdSet.isEmpty()) {
            return Map.of();
        }
        com.smart.campus.entity.query.PaperInfoQuery query = new com.smart.campus.entity.query.PaperInfoQuery();
        return paperInfoService.findListByParam(query).stream()
                .filter(item -> paperIdSet.contains(item.getPaperId()))
                .collect(Collectors.toMap(PaperInfo::getPaperId, item -> item, (left, right) -> left));
    }

    private Map<Integer, UserInfo> loadUserMap(Set<Integer> userIdSet) {
        if (userIdSet.isEmpty()) {
            return Map.of();
        }
        return userInfoService.getUserInfoByUserIdList(new ArrayList<>(userIdSet)).stream()
                .collect(Collectors.toMap(UserInfo::getUserId, item -> item, (left, right) -> left));
    }

    private void fillQuestionDetailFromQuestionInfo(CourseHomeworkQuestionVO questionVO,
                                                    QuestionInfo questionInfo,
                                                    List<QuestionOption> optionList) {
        if (questionVO == null || questionInfo == null) {
            return;
        }
        if (questionVO.getQuestionType() == null) {
            questionVO.setQuestionType(questionInfo.getQuestionType());
        }
        if (StringTools.isEmpty(questionVO.getQuestionTypeText())) {
            questionVO.setQuestionTypeText(resolveQuestionTypeText(questionVO.getQuestionType()));
        }
        if (StringTools.isEmpty(questionVO.getQuestionTitle())) {
            questionVO.setQuestionTitle(defaultString(questionInfo.getQuestionTitle()));
        }
        if (questionVO.getDifficultyLevel() == null) {
            questionVO.setDifficultyLevel(questionInfo.getDifficultyLevel());
        }
        if (StringTools.isEmpty(questionVO.getDifficultyLevelText())) {
            questionVO.setDifficultyLevelText(resolveDifficultyLevelText(questionVO.getDifficultyLevel()));
        }
        if (StringTools.isEmpty(questionVO.getAnswerAnalysis())) {
            questionVO.setAnswerAnalysis(defaultString(questionInfo.getAnswerAnalysis()));
        }
        if ((questionVO.getQuestionImageResourceIdList() == null || questionVO.getQuestionImageResourceIdList().isEmpty())
                && !StringTools.isEmpty(questionInfo.getQuestionImage())) {
            questionVO.setQuestionImageResourceIdList(parseIntegerIds(questionInfo.getQuestionImage()));
        }
        if (questionVO.getOptionList() == null || questionVO.getOptionList().isEmpty()) {
            questionVO.setOptionList(buildOptionVOList(optionList));
        }
        if (StringTools.isEmpty(questionVO.getCorrectAnswerText())) {
            questionVO.setCorrectAnswerText(resolveCorrectAnswerDisplay(questionInfo, optionList));
        }
    }

    private List<CourseAssessmentQuestionOptionVO> buildOptionVOList(List<QuestionOption> optionList) {
        if (optionList == null || optionList.isEmpty()) {
            return List.of();
        }
        List<CourseAssessmentQuestionOptionVO> result = new ArrayList<>();
        for (int index = 0; index < optionList.size(); index++) {
            QuestionOption option = optionList.get(index);
            CourseAssessmentQuestionOptionVO optionVO = new CourseAssessmentQuestionOptionVO();
            optionVO.setOptionId(option.getOptionId());
            optionVO.setOptionKey(resolveOptionKey(index));
            optionVO.setOptionContent(option.getOptionContent());
            optionVO.setSortOrder(option.getSortOrder());
            result.add(optionVO);
        }
        return result;
    }

    private String resolveCorrectAnswerDisplay(QuestionInfo questionInfo, List<QuestionOption> optionList) {
        if (questionInfo == null) {
            return "";
        }
        if (!usesOptionAnswer(questionInfo.getQuestionType())) {
            return defaultString(questionInfo.getCorrectAnswer());
        }
        Map<Integer, String> optionKeyMap = new LinkedHashMap<>();
        for (int index = 0; index < optionList.size(); index++) {
            QuestionOption option = optionList.get(index);
            optionKeyMap.put(option.getOptionId(), resolveOptionKey(index));
        }
        List<String> correctKeyList = parseIntegerIds(questionInfo.getCorrectAnswer()).stream()
                .map(optionKeyMap::get)
                .filter(Objects::nonNull)
                .toList();
        return String.join("、", correctKeyList);
    }

    private boolean usesOptionAnswer(Integer questionType) {
        return Objects.equals(questionType, 1) || Objects.equals(questionType, 2) || Objects.equals(questionType, 3);
    }

    private String resolveQuestionTypeText(Integer questionType) {
        if (Objects.equals(questionType, 1)) {
            return "单选题";
        }
        if (Objects.equals(questionType, 2)) {
            return "多选题";
        }
        if (Objects.equals(questionType, 3)) {
            return "判断题";
        }
        if (Objects.equals(questionType, 4)) {
            return "简答题";
        }
        return "题目";
    }

    private String resolveDifficultyLevelText(Integer difficultyLevel) {
        if (Objects.equals(difficultyLevel, 1)) {
            return "简单";
        }
        if (Objects.equals(difficultyLevel, 2)) {
            return "较易";
        }
        if (Objects.equals(difficultyLevel, 3)) {
            return "中等";
        }
        if (Objects.equals(difficultyLevel, 4)) {
            return "较难";
        }
        if (Objects.equals(difficultyLevel, 5)) {
            return "困难";
        }
        return "";
    }

    private List<Integer> parseIntegerIds(String idsText) {
        if (StringTools.isEmpty(idsText)) {
            return List.of();
        }
        List<Integer> result = new ArrayList<>();
        for (String item : idsText.split(",")) {
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

    private String resolveOptionKey(int index) {
        return String.valueOf((char) ('A' + index));
    }

    private String defaultString(String value) {
        return value == null ? "" : value;
    }

    private static class ExamContext {
        private UserInfo currentStudent;
        private ExamInfo examInfo;
        private CourseInfo courseInfo;
        private PaperInfo paperInfo;
        private List<PaperQuestion> paperQuestionList = List.of();
    }

    private static class PaperQuestionSnapshotVO {
        private Integer questionType;
        private String questionTypeText;
        private String questionTitle;
        private Integer difficultyLevel;
        private String difficultyLevelText;
        private String correctAnswerText;
        private String answerAnalysis;
        private List<Integer> questionImageResourceIdList = new ArrayList<>();
        private List<CourseAssessmentQuestionOptionVO> optionList = new ArrayList<>();
    }
}
