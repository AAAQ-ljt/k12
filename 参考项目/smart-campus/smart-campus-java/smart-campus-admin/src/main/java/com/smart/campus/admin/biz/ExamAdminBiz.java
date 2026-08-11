package com.smart.campus.admin.biz;

import com.alibaba.fastjson2.JSON;
import com.smart.campus.admin.biz.support.MessagePublishAdminSupport;
import com.smart.campus.entity.dto.CourseHomeworkJudgeDTO;
import com.smart.campus.entity.dto.CourseHomeworkJudgeQuestionDTO;
import com.smart.campus.admin.entity.dto.ExamSaveDTO;
import com.smart.campus.entity.enums.ResponseCodeEnum;
import com.smart.campus.entity.enums.UserRoleTypeEnum;
import com.smart.campus.entity.po.ClassInfo;
import com.smart.campus.entity.po.CourseAssessmentSubmit;
import com.smart.campus.entity.po.CourseAssessmentSubmitQuestion;
import com.smart.campus.entity.po.CourseInfo;
import com.smart.campus.entity.po.DepartmentInfo;
import com.smart.campus.entity.po.ExamClass;
import com.smart.campus.entity.po.ExamInfo;
import com.smart.campus.entity.po.MajorInfo;
import com.smart.campus.entity.po.PaperInfo;
import com.smart.campus.entity.po.PaperQuestion;
import com.smart.campus.entity.po.QuestionInfo;
import com.smart.campus.entity.po.UserInfo;
import com.smart.campus.entity.query.CourseAssessmentSubmitQuestionQuery;
import com.smart.campus.entity.query.CourseAssessmentSubmitQuery;
import com.smart.campus.entity.query.CourseInfoQuery;
import com.smart.campus.entity.query.ExamClassQuery;
import com.smart.campus.entity.query.ExamInfoQuery;
import com.smart.campus.entity.query.ExamSubmitManageQuery;
import com.smart.campus.entity.query.PaperQuestionQuery;
import com.smart.campus.entity.query.SimplePage;
import com.smart.campus.entity.query.UserInfoQuery;
import com.smart.campus.entity.vo.CourseHomeworkQuestionVO;
import com.smart.campus.entity.vo.CourseHomeworkSectionVO;
import com.smart.campus.entity.vo.CourseHomeworkSubmitManageDetailVO;
import com.smart.campus.entity.vo.CourseHomeworkSubmitManageItemVO;
import com.smart.campus.admin.entity.vo.ExamDetailVO;
import com.smart.campus.admin.entity.vo.ExamListItemVO;
import com.smart.campus.entity.vo.ExamSubmitClassVO;
import com.smart.campus.entity.vo.LoginUserVO;
import com.smart.campus.entity.vo.PaginationResultVO;
import com.smart.campus.exception.BusinessException;
import com.smart.campus.mappers.CourseAssessmentSubmitMapper;
import com.smart.campus.service.ClassInfoService;
import com.smart.campus.service.CourseAssessmentSubmitQuestionService;
import com.smart.campus.service.CourseAssessmentSubmitService;
import com.smart.campus.service.CourseInfoService;
import com.smart.campus.service.DepartmentInfoService;
import com.smart.campus.service.ExamClassService;
import com.smart.campus.service.ExamInfoService;
import com.smart.campus.service.MajorInfoService;
import com.smart.campus.service.PaperInfoService;
import com.smart.campus.service.PaperQuestionService;
import com.smart.campus.service.QuestionInfoService;
import com.smart.campus.service.UserInfoService;
import com.smart.campus.utils.DateUtil;
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
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.zip.CRC32;

@Service
public class ExamAdminBiz {

    private static final int DEFAULT_PAGE_NO = 1;
    private static final int DEFAULT_PAGE_SIZE = 15;
    private static final int DEFAULT_STATUS = 0;
    private static final int PAPER_TYPE_EXAM = 2;
    private static final int USER_ROLE_TEACHER = UserRoleTypeEnum.TEACHER.getCode();
    private static final int USER_ROLE_STUDENT = UserRoleTypeEnum.STUDENT.getCode();
    private static final int TASK_TYPE_EXAM = 2;
    private static final int PAPER_SECTION_TYPE_SECTION = 1;
    private static final int QUESTION_TYPE_SUBJECTIVE = 4;
    private static final int SUBMIT_STATUS_PENDING = 0;
    private static final int SUBMIT_STATUS_SUBMITTED = 3;
    private static final int JUDGE_STATUS_NOT_STARTED = 0;
    private static final int JUDGE_STATUS_AUTO_DONE = 1;
    private static final int JUDGE_STATUS_WAIT_MANUAL = 2;
    private static final int JUDGE_STATUS_MANUAL_DONE = 3;
    private static final String EXAM_ORDER_BY = "e.update_time desc,e.create_time desc";
    private static final String PAPER_QUESTION_ORDER_BY = "p.sort_order asc,p.id asc";
    private static final String USER_ORDER_BY = "u.user_no asc,u.user_id asc";
    private static final String DATETIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    @Resource
    private ExamInfoService examInfoService;

    @Resource
    private ExamClassService examClassService;

    @Resource
    private CourseInfoService courseInfoService;

    @Resource
    private PaperInfoService paperInfoService;

    @Resource
    private ClassInfoService classInfoService;

    @Resource
    private MajorInfoService majorInfoService;

    @Resource
    private DepartmentInfoService departmentInfoService;

    @Resource
    private UserInfoService userInfoService;

    @Resource
    private PaperQuestionService paperQuestionService;

    @Resource
    private QuestionInfoService questionInfoService;

    @Resource
    private CourseAssessmentSubmitService courseAssessmentSubmitService;

    @Resource
    private CourseAssessmentSubmitQuestionService courseAssessmentSubmitQuestionService;

    @Resource
    private CourseAssessmentSubmitMapper<CourseAssessmentSubmit, CourseAssessmentSubmitQuery> courseAssessmentSubmitMapper;

    @Resource
    private MessagePublishAdminSupport messagePublishAdminSupport;

    public PaginationResultVO<ExamListItemVO> loadDataList(ExamInfoQuery query) {
        ExamInfoQuery request = query == null ? new ExamInfoQuery() : query;
        if (request.getPageNo() == null || request.getPageNo() < 1) {
            request.setPageNo(DEFAULT_PAGE_NO);
        }
        if (request.getPageSize() == null || request.getPageSize() < 1) {
            request.setPageSize(DEFAULT_PAGE_SIZE);
        }
        request.setOrderBy(EXAM_ORDER_BY);
        LoginUserVO loginUser = LoginUserContextHolder.get();
        if (loginUser != null && Objects.equals(loginUser.getRoleType(), USER_ROLE_TEACHER)) {
            request.setTeacherId(loginUser.getUserId());
        }
        PaginationResultVO<ExamInfo> pageResult = examInfoService.findListByPage(request);
        List<ExamListItemVO> list = buildExamList(pageResult.getList());
        return new PaginationResultVO<>(
                pageResult.getTotalCount(),
                pageResult.getPageSize(),
                pageResult.getPageNo(),
                pageResult.getPageTotal(),
                list
        );
    }

    public ExamDetailVO getExamInfoById(String examId) {
        ExamInfo examInfo = examInfoService.getExamInfoByExamId(StringTools.trim(examId));
        if (examInfo == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "考试信息不存在");
        }
        return buildExamDetail(examInfo);
    }

    @Transactional(rollbackFor = Exception.class)
    public ExamDetailVO add(ExamSaveDTO dto) {
        ExamSaveDTO request = normalizeSaveDTO(dto);
        LoginUserVO loginUser = getCurrentTeacherLoginUser();
        validateSaveDTO(request, loginUser.getUserId(), null);
        String examId = generateStringId();
        ExamInfo examInfo = buildExamInfo(request, examId, loginUser.getUserId(), null);
        examInfoService.add(examInfo);
        syncExamClasses(examId, request.getClassIdList());
        return getExamInfoById(examId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateExamInfoById(ExamSaveDTO dto) {
        ExamSaveDTO request = normalizeSaveDTO(dto);
        ExamInfo original = examInfoService.getExamInfoByExamId(request.getExamId());
        if (original == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "考试信息不存在");
        }
        LoginUserVO loginUser = getCurrentTeacherLoginUser();
        checkTeacherOwnership(original, loginUser.getUserId());
        validateSaveDTO(request, loginUser.getUserId(), original);
        ExamInfo examInfo = buildExamInfo(request, original.getExamId(), loginUser.getUserId(), original);
        examInfoService.updateExamInfoByExamId(examInfo, original.getExamId());
        syncExamClasses(original.getExamId(), request.getClassIdList());
    }

    @Transactional(rollbackFor = Exception.class)
    public void publish(String examId) {
        String normalizedExamId = StringTools.trim(examId);
        ExamInfo original = examInfoService.getExamInfoByExamId(normalizedExamId);
        if (original == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "考试信息不存在");
        }
        LoginUserVO loginUser = getCurrentTeacherLoginUser();
        checkTeacherOwnership(original, loginUser.getUserId());
        ExamClassQuery classQuery = new ExamClassQuery();
        classQuery.setExamId(normalizedExamId);
        if (examClassService.findCountByParam(classQuery) <= 0) {
            throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "请先为考试分配班级");
        }
        if (Objects.equals(original.getStatus(), 1)) {
            return;
        }
        ExamInfo update = new ExamInfo();
        update.setStatus(1);
        examInfoService.updateExamInfoByExamId(update, normalizedExamId);
        CourseInfo courseInfo = StringTools.isEmpty(original.getCourseId())
                ? null
                : courseInfoService.getCourseInfoByCourseId(original.getCourseId());
        messagePublishAdminSupport.sendExamPublishMessage(original, courseInfo, loadExamClassIdList(normalizedExamId), loginUser);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteBatch(String ids) {
        for (String examId : parseStringIds(ids)) {
            deleteExam(examId);
        }
    }

    public List<ExamSubmitClassVO> loadExamSubmitClassList(String examId) {
        ExamInfo examInfo = loadExamWithPermission(examId);
        List<Integer> classIdList = loadExamClassIdList(examInfo.getExamId());
        if (classIdList.isEmpty()) {
            return List.of();
        }
        Map<Integer, ClassInfo> classMap = loadClassMap(new LinkedHashSet<>(classIdList));
        Map<Integer, MajorInfo> majorMap = loadMajorMap(classMap.values().stream()
                .map(ClassInfo::getMajorId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet()));
        Map<Integer, DepartmentInfo> departmentMap = loadDepartmentMap(classMap.values().stream()
                .map(ClassInfo::getDepartmentId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet()));
        List<ExamSubmitClassVO> result = new ArrayList<>();
        for (Integer classId : classIdList) {
            ClassInfo classInfo = classMap.get(classId);
            if (classInfo == null) {
                continue;
            }
            List<UserInfo> studentList = loadStudentList(classId, null);
            Map<String, CourseAssessmentSubmit> submitMap = loadExamSubmitMap(examInfo.getExamId(), studentList);
            ExamSubmitClassVO vo = new ExamSubmitClassVO();
            vo.setClassId(classId);
            vo.setClassName(classInfo.getClassName());
            MajorInfo majorInfo = majorMap.get(classInfo.getMajorId());
            vo.setMajorName(majorInfo == null ? "" : majorInfo.getMajorName());
            DepartmentInfo departmentInfo = departmentMap.get(classInfo.getDepartmentId());
            vo.setDepartmentName(departmentInfo == null ? "" : departmentInfo.getDepartmentName());
            vo.setStudentCount(studentList.size());
            vo.setSubmittedCount((int) submitMap.values().stream()
                    .filter(item -> Objects.equals(item.getSubmitStatus(), SUBMIT_STATUS_SUBMITTED))
                    .count());
            vo.setWaitJudgeCount((int) submitMap.values().stream()
                    .filter(item -> Objects.equals(item.getSubmitStatus(), SUBMIT_STATUS_SUBMITTED)
                            && Objects.equals(item.getJudgeStatus(), JUDGE_STATUS_WAIT_MANUAL))
                    .count());
            result.add(vo);
        }
        return result;
    }

    public PaginationResultVO<CourseHomeworkSubmitManageItemVO> loadExamSubmitList(ExamSubmitManageQuery query) {
        ExamSubmitManageQuery request = normalizeExamSubmitQuery(query);
        if (StringTools.isEmpty(request.getExamId())) {
            return emptyExamSubmitPage(request);
        }
        ExamInfo examInfo = loadExamWithPermission(request.getExamId());
        Integer classId = resolveExamSelectedClassId(examInfo.getExamId(), request.getClassId());
        if (classId == null) {
            return emptyExamSubmitPage(request);
        }
        ClassInfo classInfo = classInfoService.getClassInfoByClassId(classId);
        List<UserInfo> studentList = loadStudentList(classId, request.getKeyword());
        if (studentList.isEmpty()) {
            return emptyExamSubmitPage(request);
        }
        Map<String, CourseAssessmentSubmit> submitMap = loadExamSubmitMap(examInfo.getExamId(), studentList);
        List<CourseHomeworkSubmitManageItemVO> rowList = new ArrayList<>();
        for (UserInfo student : studentList) {
            CourseAssessmentSubmit submit = submitMap.get(buildSubmitKey(examInfo.getExamId(), student.getUserId()));
            CourseHomeworkSubmitManageItemVO item = buildExamManageItem(examInfo, classId, classInfo, student, submit);
            if (matchExamSubmitStatusFilter(item, request)) {
                rowList.add(item);
            }
        }
        int totalCount = rowList.size();
        SimplePage simplePage = new SimplePage(request.getPageNo(), totalCount, request.getPageSize());
        int start = simplePage.getStart();
        int end = Math.min(start + simplePage.getPageSize(), totalCount);
        List<CourseHomeworkSubmitManageItemVO> currentList = start >= totalCount ? List.of() : rowList.subList(start, end);
        return new PaginationResultVO<>(totalCount, simplePage.getPageSize(), simplePage.getPageNo(), simplePage.getPageTotal(), currentList);
    }

    public CourseHomeworkSubmitManageDetailVO getExamSubmitDetail(String examId, Integer studentId) {
        ExamAdminContext context = loadExamContext(examId, studentId);
        CourseAssessmentSubmit submit = getLatestExamSubmit(context.examInfo.getExamId(), context.student.getUserId());
        return buildExamManageDetail(context, submit);
    }

    @Transactional(rollbackFor = Exception.class)
    public void judgeExamSubmit(CourseHomeworkJudgeDTO dto) {
        CourseAssessmentSubmit submit = courseAssessmentSubmitService.getCourseAssessmentSubmitBySubmitId(dto.getSubmitId());
        if (submit == null || !Objects.equals(submit.getTaskType(), TASK_TYPE_EXAM)) {
            throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "考试提交记录不存在");
        }
        if (!Objects.equals(submit.getSubmitStatus(), SUBMIT_STATUS_SUBMITTED)) {
            throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "学生尚未提交试卷，暂不能批改");
        }
        ExamAdminContext context = loadExamContext(submit.getTaskId(), submit.getUserId());
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
        for (PaperQuestion paperQuestion : loadPaperQuestionList(context.examInfo.getPaperId())) {
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
            messagePublishAdminSupport.sendExamJudgedSystemMessage(
                    context.examInfo,
                    context.student.getUserId(),
                    context.student.getRealName(),
                    loginUser,
                    scaleScore(defaultScore(submit.getObjectiveScore()).add(update.getSubjectiveScore())).toPlainString()
            );
        }
    }

    private void deleteExam(String examId) {
        ExamInfo original = examInfoService.getExamInfoByExamId(examId);
        if (original == null) {
            return;
        }
        LoginUserVO loginUser = LoginUserContextHolder.get();
        if (loginUser != null && Objects.equals(loginUser.getRoleType(), USER_ROLE_TEACHER)) {
            checkTeacherOwnership(original, loginUser.getUserId());
        }
        deleteExamRelations(examId);
        examInfoService.deleteExamInfoByExamId(examId);
    }

    private ExamSaveDTO normalizeSaveDTO(ExamSaveDTO dto) {
        ExamSaveDTO request = dto == null ? new ExamSaveDTO() : dto;
        request.setExamId(StringTools.trim(request.getExamId()));
        request.setExamName(StringTools.trim(request.getExamName()));
        request.setCourseId(StringTools.trim(request.getCourseId()));
        request.setPaperId(StringTools.trim(request.getPaperId()));
        request.setDescription(StringTools.trim(request.getDescription()));
        if (request.getStatus() == null) {
            request.setStatus(DEFAULT_STATUS);
        }
        request.setClassIdList(normalizeClassIds(request.getClassIdList()));
        return request;
    }

    private void validateSaveDTO(ExamSaveDTO dto, Integer teacherId, ExamInfo original) {
        if (StringTools.isEmpty(dto.getExamName())) {
            throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "考试名称不能为空");
        }
        Date startTime = parseDate(dto.getStartTime(), "开始时间格式不正确");
        Date endTime = parseDate(dto.getEndTime(), "结束时间格式不正确");
        if (!startTime.before(endTime)) {
            throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "结束时间必须晚于开始时间");
        }
        CourseInfo courseInfo = courseInfoService.getCourseInfoByCourseId(dto.getCourseId());
        if (courseInfo == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "课程信息不存在");
        }
        checkTeacherOwnership(courseInfo, teacherId);
        PaperInfo paperInfo = paperInfoService.getPaperInfoByPaperId(dto.getPaperId());
        if (paperInfo == null || !Objects.equals(paperInfo.getPaperType(), PAPER_TYPE_EXAM)) {
            throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "请选择考试试卷");
        }
        validateClasses(dto.getClassIdList());
        if (original != null && Objects.equals(original.getStatus(), 1)) {
            Date now = new Date();
            if (now.after(original.getStartTime())) {
                throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "考试已发布且已开始，不允许再修改");
            }
        }
    }

    private ExamInfo buildExamInfo(ExamSaveDTO dto, String examId, Integer teacherId, ExamInfo original) {
        ExamInfo examInfo = new ExamInfo();
        examInfo.setExamId(examId);
        examInfo.setExamName(dto.getExamName());
        examInfo.setCourseId(dto.getCourseId());
        examInfo.setPaperId(dto.getPaperId());
        examInfo.setTeacherId(teacherId);
        examInfo.setStartTime(parseDate(dto.getStartTime(), "开始时间格式不正确"));
        examInfo.setEndTime(parseDate(dto.getEndTime(), "结束时间格式不正确"));
        examInfo.setStatus(original == null ? dto.getStatus() : original.getStatus());
        examInfo.setDescription(dto.getDescription());
        if (original != null) {
            examInfo.setCreateTime(original.getCreateTime());
        }
        return examInfo;
    }

    private void syncExamClasses(String examId, List<Integer> classIdList) {
        ExamClassQuery deleteQuery = new ExamClassQuery();
        deleteQuery.setExamId(examId);
        examClassService.deleteByParam(deleteQuery);
        if (classIdList == null || classIdList.isEmpty()) {
            return;
        }
        List<ExamClass> relationList = new ArrayList<>();
        for (Integer classId : classIdList) {
            ExamClass relation = new ExamClass();
            relation.setExamId(examId);
            relation.setClassId(classId);
            relationList.add(relation);
        }
        examClassService.addBatch(relationList);
    }

    private List<Integer> loadExamClassIdList(String examId) {
        ExamClassQuery classQuery = new ExamClassQuery();
        classQuery.setExamId(examId);
        classQuery.setOrderBy("e.class_id asc");
        return examClassService.findListByParam(classQuery).stream()
                .map(ExamClass::getClassId)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private void deleteExamRelations(String examId) {
        ExamClassQuery examClassQuery = new ExamClassQuery();
        examClassQuery.setExamId(examId);
        examClassService.deleteByParam(examClassQuery);

        CourseAssessmentSubmitQuery submitQuery = new CourseAssessmentSubmitQuery();
        submitQuery.setTaskId(examId);
        submitQuery.setTaskType(TASK_TYPE_EXAM);
        List<CourseAssessmentSubmit> submitList = courseAssessmentSubmitService.findListByParam(submitQuery);
        if (!submitList.isEmpty()) {
            List<Long> submitIdList = submitList.stream()
                    .map(CourseAssessmentSubmit::getSubmitId)
                    .filter(Objects::nonNull)
                    .toList();
            for (Long submitId : submitIdList) {
                CourseAssessmentSubmitQuestionQuery questionQuery = new CourseAssessmentSubmitQuestionQuery();
                questionQuery.setSubmitId(submitId);
                courseAssessmentSubmitQuestionService.deleteByParam(questionQuery);
            }
            courseAssessmentSubmitService.deleteByParam(submitQuery);
        }
    }

    private ExamSubmitManageQuery normalizeExamSubmitQuery(ExamSubmitManageQuery query) {
        ExamSubmitManageQuery request = query == null ? new ExamSubmitManageQuery() : query;
        if (request.getPageNo() == null || request.getPageNo() < 1) {
            request.setPageNo(DEFAULT_PAGE_NO);
        }
        if (request.getPageSize() == null || request.getPageSize() < 1) {
            request.setPageSize(DEFAULT_PAGE_SIZE);
        }
        request.setExamId(StringTools.trim(request.getExamId()));
        request.setKeyword(StringTools.trim(request.getKeyword()));
        return request;
    }

    private PaginationResultVO<CourseHomeworkSubmitManageItemVO> emptyExamSubmitPage(ExamSubmitManageQuery query) {
        int pageSize = query == null || query.getPageSize() == null ? DEFAULT_PAGE_SIZE : query.getPageSize();
        int pageNo = query == null || query.getPageNo() == null ? DEFAULT_PAGE_NO : query.getPageNo();
        return new PaginationResultVO<>(0, pageSize, pageNo, 0, List.of());
    }

    private ExamInfo loadExamWithPermission(String examId) {
        ExamInfo examInfo = examInfoService.getExamInfoByExamId(StringTools.trim(examId));
        if (examInfo == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "考试信息不存在");
        }
        LoginUserVO loginUser = getCurrentTeacherLoginUser();
        checkTeacherOwnership(examInfo, loginUser.getUserId());
        return examInfo;
    }

    private Integer resolveExamSelectedClassId(String examId, Integer classId) {
        List<Integer> classIdList = loadExamClassIdList(examId);
        if (classId != null && classIdList.contains(classId)) {
            return classId;
        }
        return classIdList.isEmpty() ? null : classIdList.get(0);
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

    private Map<String, CourseAssessmentSubmit> loadExamSubmitMap(String examId, List<UserInfo> studentList) {
        List<Integer> userIdList = studentList.stream()
                .map(UserInfo::getUserId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (StringTools.isEmpty(examId) || userIdList.isEmpty()) {
            return Map.of();
        }
        List<CourseAssessmentSubmit> submitList = courseAssessmentSubmitMapper.selectByTaskIdListAndUserIdList(
                List.of(examId),
                userIdList,
                TASK_TYPE_EXAM
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

    private CourseHomeworkSubmitManageItemVO buildExamManageItem(ExamInfo examInfo,
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
        item.setLessonId(examInfo.getExamId());
        item.setLessonName(examInfo.getExamName());
        item.setPaperId(examInfo.getPaperId());
        PaperInfo paperInfo = paperInfoService.getPaperInfoByPaperId(examInfo.getPaperId());
        item.setPaperName(defaultString(paperInfo == null ? null : paperInfo.getPaperName()));
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

    private boolean matchExamSubmitStatusFilter(CourseHomeworkSubmitManageItemVO item, ExamSubmitManageQuery query) {
        if (query.getSubmitStatus() != null && !Objects.equals(item.getSubmitStatus(), query.getSubmitStatus())) {
            return false;
        }
        if (query.getJudgeStatus() != null && !Objects.equals(item.getJudgeStatus(), query.getJudgeStatus())) {
            return false;
        }
        return true;
    }

    private int compareSubmitPriority(CourseAssessmentSubmit left, CourseAssessmentSubmit right) {
        Date leftTime = left.getSubmitTime() == null ? left.getUpdateTime() : left.getSubmitTime();
        Date rightTime = right.getSubmitTime() == null ? right.getUpdateTime() : right.getSubmitTime();
        if (leftTime == null && rightTime == null) {
            return Long.compare(left.getSubmitId() == null ? 0L : left.getSubmitId(), right.getSubmitId() == null ? 0L : right.getSubmitId());
        }
        if (leftTime == null) {
            return -1;
        }
        if (rightTime == null) {
            return 1;
        }
        return leftTime.compareTo(rightTime);
    }

    private String buildSubmitKey(String taskId, Integer userId) {
        return StringTools.trim(taskId) + "_" + userId;
    }

    private ExamAdminContext loadExamContext(String examId, Integer studentId) {
        if (StringTools.isEmpty(examId) || studentId == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "考试详情参数不能为空");
        }
        ExamInfo examInfo = loadExamWithPermission(examId);
        UserInfo student = userInfoService.getUserInfoByUserId(studentId);
        if (student == null || !Objects.equals(student.getRoleType(), USER_ROLE_STUDENT)) {
            throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "学生信息不存在");
        }
        Integer studentClassId = resolveStudentClassId(student);
        if (studentClassId == null || !loadExamClassIdList(examInfo.getExamId()).contains(studentClassId)) {
            throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "该学生不在当前考试班级中");
        }
        ExamAdminContext context = new ExamAdminContext();
        context.examInfo = examInfo;
        context.student = student;
        context.classInfo = classInfoService.getClassInfoByClassId(studentClassId);
        return context;
    }

    private CourseAssessmentSubmit getLatestExamSubmit(String examId, Integer studentId) {
        CourseAssessmentSubmitQuery query = new CourseAssessmentSubmitQuery();
        query.setTaskId(examId);
        query.setTaskType(TASK_TYPE_EXAM);
        query.setUserId(studentId);
        List<CourseAssessmentSubmit> submitList = courseAssessmentSubmitService.findListByParam(query);
        CourseAssessmentSubmit latest = null;
        for (CourseAssessmentSubmit item : submitList) {
            if (latest == null || compareSubmitPriority(item, latest) > 0) {
                latest = item;
            }
        }
        return latest;
    }

    private CourseHomeworkSubmitManageDetailVO buildExamManageDetail(ExamAdminContext context, CourseAssessmentSubmit submit) {
        CourseHomeworkSubmitManageDetailVO vo = new CourseHomeworkSubmitManageDetailVO();
        vo.setStudentId(context.student.getUserId());
        vo.setStudentNo(context.student.getUserNo());
        vo.setStudentName(context.student.getRealName());
        vo.setClassId(resolveStudentClassId(context.student));
        vo.setClassName(context.classInfo == null ? "" : context.classInfo.getClassName());
        vo.setLessonId(context.examInfo.getExamId());
        vo.setLessonName(context.examInfo.getExamName());
        vo.setPaperId(context.examInfo.getPaperId());
        PaperInfo paperInfo = paperInfoService.getPaperInfoByPaperId(context.examInfo.getPaperId());
        vo.setPaperName(paperInfo == null ? "" : paperInfo.getPaperName());
        Map<Integer, CourseAssessmentSubmitQuestion> submitQuestionMap = submit == null ? Map.of() : loadSubmitQuestionMap(submit.getSubmitId());
        List<PaperQuestion> paperQuestionList = loadPaperQuestionList(context.examInfo.getPaperId());
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

    private String resolveSubmitStatusText(Integer status) {
        if (Objects.equals(status, SUBMIT_STATUS_PENDING)) {
            return "待开始";
        }
        if (Objects.equals(status, 1)) {
            return "作答中";
        }
        if (Objects.equals(status, 2)) {
            return "草稿";
        }
        if (Objects.equals(status, SUBMIT_STATUS_SUBMITTED)) {
            return "已提交";
        }
        return "未知";
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

    private List<ExamListItemVO> buildExamList(List<ExamInfo> examList) {
        if (examList == null || examList.isEmpty()) {
            return List.of();
        }
        Set<String> courseIdSet = examList.stream().map(ExamInfo::getCourseId).filter(Objects::nonNull).collect(Collectors.toSet());
        Set<String> paperIdSet = examList.stream().map(ExamInfo::getPaperId).filter(Objects::nonNull).collect(Collectors.toSet());
        Set<Integer> teacherIdSet = examList.stream().map(ExamInfo::getTeacherId).filter(Objects::nonNull).collect(Collectors.toSet());
        Set<String> examIdSet = examList.stream().map(ExamInfo::getExamId).filter(Objects::nonNull).collect(Collectors.toSet());

        Map<String, CourseInfo> courseMap = loadCourseMap(courseIdSet);
        Map<String, PaperInfo> paperMap = loadPaperMap(paperIdSet);
        Map<Integer, UserInfo> teacherMap = loadTeacherMap(teacherIdSet);
        Map<String, List<ExamClass>> examClassMap = loadExamClassMap(examIdSet);
        Map<Integer, ClassInfo> classMap = loadClassMap(examClassMap.values().stream()
                .flatMap(Collection::stream)
                .map(ExamClass::getClassId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet()));

        List<ExamListItemVO> result = new ArrayList<>();
        for (ExamInfo item : examList) {
            ExamListItemVO vo = new ExamListItemVO();
            vo.setExamId(item.getExamId());
            vo.setExamName(item.getExamName());
            vo.setCourseId(item.getCourseId());
            vo.setCourseName(defaultString(courseMap.get(item.getCourseId()) == null ? null : courseMap.get(item.getCourseId()).getCourseName()));
            vo.setPaperId(item.getPaperId());
            vo.setPaperName(defaultString(paperMap.get(item.getPaperId()) == null ? null : paperMap.get(item.getPaperId()).getPaperName()));
            vo.setTeacherId(item.getTeacherId());
            vo.setTeacherName(defaultString(teacherMap.get(item.getTeacherId()) == null ? null : teacherMap.get(item.getTeacherId()).getRealName()));
            vo.setStartTime(item.getStartTime());
            vo.setEndTime(item.getEndTime());
            vo.setStatus(item.getStatus());
            vo.setStatusText(resolveStatusText(item.getStatus()));
            vo.setUpdateTime(item.getUpdateTime());
            List<ExamClass> classList = examClassMap.getOrDefault(item.getExamId(), List.of());
            vo.setClassIdList(classList.stream().map(ExamClass::getClassId).filter(Objects::nonNull).toList());
            vo.setClassNames(classList.stream()
                    .map(ExamClass::getClassId)
                    .map(classMap::get)
                    .filter(Objects::nonNull)
                    .map(ClassInfo::getClassName)
                    .filter(name -> !StringTools.isEmpty(name))
                    .collect(Collectors.joining("、")));
            result.add(vo);
        }
        return result;
    }

    private ExamDetailVO buildExamDetail(ExamInfo examInfo) {
        ExamDetailVO vo = new ExamDetailVO();
        vo.setExamId(examInfo.getExamId());
        vo.setExamName(examInfo.getExamName());
        vo.setCourseId(examInfo.getCourseId());
        vo.setPaperId(examInfo.getPaperId());
        vo.setTeacherId(examInfo.getTeacherId());
        vo.setStartTime(examInfo.getStartTime());
        vo.setEndTime(examInfo.getEndTime());
        vo.setStatus(examInfo.getStatus());
        vo.setStatusText(resolveStatusText(examInfo.getStatus()));
        vo.setDescription(examInfo.getDescription());
        vo.setCreateTime(examInfo.getCreateTime());
        vo.setUpdateTime(examInfo.getUpdateTime());

        CourseInfo courseInfo = courseInfoService.getCourseInfoByCourseId(examInfo.getCourseId());
        if (courseInfo != null) {
            vo.setCourseName(courseInfo.getCourseName());
        }
        PaperInfo paperInfo = paperInfoService.getPaperInfoByPaperId(examInfo.getPaperId());
        if (paperInfo != null) {
            vo.setPaperName(paperInfo.getPaperName());
        }
        UserInfo teacher = userInfoService.getUserInfoByUserId(examInfo.getTeacherId());
        if (teacher != null) {
            vo.setTeacherName(teacher.getRealName());
        }

        ExamClassQuery classQuery = new ExamClassQuery();
        classQuery.setExamId(examInfo.getExamId());
        vo.setClassIdList(examClassService.findListByParam(classQuery).stream()
                .map(ExamClass::getClassId)
                .filter(Objects::nonNull)
                .toList());
        return vo;
    }

    private Map<String, CourseInfo> loadCourseMap(Set<String> courseIdSet) {
        if (courseIdSet.isEmpty()) {
            return Map.of();
        }
        CourseInfoQuery query = new CourseInfoQuery();
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

    private Map<Integer, UserInfo> loadTeacherMap(Set<Integer> teacherIdSet) {
        if (teacherIdSet.isEmpty()) {
            return Map.of();
        }
        return userInfoService.getUserInfoByUserIdList(new ArrayList<>(teacherIdSet)).stream()
                .collect(Collectors.toMap(UserInfo::getUserId, item -> item, (left, right) -> left));
    }

    private Map<String, List<ExamClass>> loadExamClassMap(Set<String> examIdSet) {
        if (examIdSet.isEmpty()) {
            return Map.of();
        }
        ExamClassQuery query = new ExamClassQuery();
        return examClassService.findListByParam(query).stream()
                .filter(item -> examIdSet.contains(item.getExamId()))
                .collect(Collectors.groupingBy(ExamClass::getExamId));
    }

    private Map<Integer, ClassInfo> loadClassMap(Set<Integer> classIdSet) {
        if (classIdSet.isEmpty()) {
            return Map.of();
        }
        return classInfoService.getClassInfoByClassIdList(new ArrayList<>(classIdSet)).stream()
                .collect(Collectors.toMap(ClassInfo::getClassId, item -> item, (left, right) -> left));
    }

    private Map<Integer, MajorInfo> loadMajorMap(Set<Integer> majorIdSet) {
        if (majorIdSet.isEmpty()) {
            return Map.of();
        }
        return majorInfoService.getMajorInfoByMajorIdList(new ArrayList<>(majorIdSet)).stream()
                .collect(Collectors.toMap(MajorInfo::getMajorId, item -> item, (left, right) -> left));
    }

    private Map<Integer, DepartmentInfo> loadDepartmentMap(Set<Integer> departmentIdSet) {
        if (departmentIdSet.isEmpty()) {
            return Map.of();
        }
        return departmentInfoService.getDepartmentInfoByDepartmentIdList(new ArrayList<>(departmentIdSet)).stream()
                .collect(Collectors.toMap(DepartmentInfo::getDepartmentId, item -> item, (left, right) -> left));
    }

    private void validateClasses(List<Integer> classIdList) {
        if (classIdList == null || classIdList.isEmpty()) {
            throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "请选择考试班级");
        }
        List<ClassInfo> classList = classInfoService.getClassInfoByClassIdList(classIdList);
        if (classList.size() != classIdList.size()) {
            throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "存在无效的班级信息");
        }
    }

    private List<Integer> normalizeClassIds(List<Integer> classIdList) {
        if (classIdList == null || classIdList.isEmpty()) {
            return List.of();
        }
        return new ArrayList<>(classIdList.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new)));
    }

    private LoginUserVO getCurrentTeacherLoginUser() {
        LoginUserVO loginUser = LoginUserContextHolder.get();
        if (loginUser == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_901);
        }
        if (!Objects.equals(loginUser.getRoleType(), USER_ROLE_TEACHER)) {
            throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "请使用教师账号操作考试");
        }
        return loginUser;
    }

    private void checkTeacherOwnership(CourseInfo courseInfo, Integer teacherId) {
        if (courseInfo == null || !Objects.equals(courseInfo.getTeacherId(), teacherId)) {
            throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "只能操作当前教师自己的课程");
        }
    }

    private void checkTeacherOwnership(ExamInfo examInfo, Integer teacherId) {
        if (examInfo == null || !Objects.equals(examInfo.getTeacherId(), teacherId)) {
            throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "只能操作当前教师自己的考试");
        }
    }

    private Date parseDate(String value, String message) {
        String text = StringTools.trim(value);
        Date date = DateUtil.parse(text, DATETIME_PATTERN);
        if (date == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), message);
        }
        return date;
    }

    private List<String> parseStringIds(String ids) {
        if (StringTools.isEmpty(ids)) {
            return List.of();
        }
        return List.of(ids.split(",")).stream()
                .map(StringTools::trim)
                .filter(item -> !StringTools.isEmpty(item))
                .distinct()
                .toList();
    }

    private String resolveStatusText(Integer status) {
        return Objects.equals(status, 1) ? "已发布" : "草稿";
    }

    private String defaultString(String value) {
        return value == null ? "" : value;
    }

    private String generateStringId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();
    }

    private static class ExamAdminContext {
        private ExamInfo examInfo;
        private UserInfo student;
        private ClassInfo classInfo;
    }
}
