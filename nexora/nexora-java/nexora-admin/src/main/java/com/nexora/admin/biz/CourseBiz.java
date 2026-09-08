package com.nexora.admin.biz;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.nexora.admin.component.LessonQuizAiComponent;
import com.nexora.admin.component.LessonQuizTaskComponent;
import com.nexora.admin.dto.LessonQuizSaveDTO;
import com.nexora.admin.dto.LessonResourceBindDTO;
import com.nexora.admin.vo.LessonQuizDetailVO;
import com.nexora.admin.vo.LessonQuizTaskVO;
import com.nexora.constants.Constants;
import com.nexora.entity.enums.StageEnum;
import com.nexora.entity.po.CourseChapter;
import com.nexora.entity.po.CourseChapterLesson;
import com.nexora.entity.po.CourseChapterLessonResource;
import com.nexora.entity.po.CourseInfo;
import com.nexora.entity.po.CourseLessonQuiz;
import com.nexora.entity.po.KnowledgePoint;
import com.nexora.entity.po.QuestionInfo;
import com.nexora.entity.po.ResourceInfo;
import com.nexora.entity.query.CourseChapterLessonQuery;
import com.nexora.entity.query.CourseChapterLessonResourceQuery;
import com.nexora.entity.query.CourseChapterQuery;
import com.nexora.entity.query.CourseInfoQuery;
import com.nexora.entity.query.CourseLessonQuizQuery;
import com.nexora.entity.query.CourseStudyLessonProgressQuery;
import com.nexora.entity.query.CourseStudyLogQuery;
import com.nexora.entity.query.CourseStudyProgressQuery;
import com.nexora.entity.query.QuestionInfoQuery;
import com.nexora.entity.query.ResourceInfoQuery;
import com.nexora.entity.vo.CourseChapterDetailVO;
import com.nexora.entity.vo.CourseDetailVO;
import com.nexora.entity.vo.CourseLessonDetailVO;
import com.nexora.entity.vo.CourseLessonResourceVO;
import com.nexora.entity.vo.PaginationResultVO;
import com.nexora.exception.BusinessException;
import com.nexora.service.CourseChapterLessonResourceService;
import com.nexora.service.CourseChapterLessonService;
import com.nexora.service.CourseChapterService;
import com.nexora.service.CourseInfoService;
import com.nexora.service.KnowledgePointService;
import com.nexora.service.CourseLessonQuizService;
import com.nexora.service.CourseStudyLessonProgressService;
import com.nexora.service.CourseStudyLogService;
import com.nexora.service.CourseStudyProgressService;
import com.nexora.service.QuestionInfoService;
import com.nexora.service.ResourceInfoService;
import com.nexora.utils.StringTools;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 课程体系管理业务：课程、章节、课时、课时资源绑定。
 */
@Slf4j
@Service
public class CourseBiz {

    @Resource
    private CourseInfoService courseInfoService;

    @Resource
    private CourseChapterService courseChapterService;

    @Resource
    private CourseChapterLessonService courseChapterLessonService;

    @Resource
    private CourseChapterLessonResourceService courseChapterLessonResourceService;

    @Resource
    private ResourceInfoService resourceInfoService;

    @Resource
    private CourseStudyProgressService courseStudyProgressService;

    @Resource
    private CourseStudyLogService courseStudyLogService;

    @Resource
    private CourseStudyLessonProgressService courseStudyLessonProgressService;

    @Resource
    private CourseLessonQuizService courseLessonQuizService;

    @Resource
    private QuestionInfoService questionInfoService;

    @Resource
    private LessonQuizAiComponent lessonQuizAiComponent;

    @Resource
    private LessonQuizTaskComponent lessonQuizTaskComponent;

    @Resource
    private KnowledgePointService knowledgePointService;

    private static final int QUIZ_MODE_CLOSED = 0;
    private static final int QUIZ_MODE_LIBRARY = 1;
    private static final int QUIZ_MODE_AI = 2;

    public PaginationResultVO<CourseInfo> coursePage(CourseInfoQuery query) {
        return courseInfoService.findListByPage(query);
    }

    public CourseInfo getCourse(String courseId) {
        CourseInfo course = courseInfoService.getCourseInfoByCourseId(courseId);
        if (course == null) {
            throw new BusinessException("课程不存在");
        }
        return course;
    }

    @Transactional(rollbackFor = Exception.class)
    public String addCourse(CourseInfo bean) {
        if (StringTools.isEmpty(bean.getCourseName())) {
            throw new BusinessException("课程名称不能为空");
        }
        fillStageByGrade(bean);
        bean.setCourseId(StringTools.getRandomNumber(Constants.LENGTH_15));
        if (bean.getSort() == null) {
            bean.setSort(0);
        }
        if (bean.getStatus() == null) {
            bean.setStatus(1);
        }
        if (bean.getLessonCount() == null) {
            bean.setLessonCount(0);
        }
        if (bean.getStudyCount() == null) {
            bean.setStudyCount(0);
        }
        Date now = new Date();
        bean.setCreateTime(now);
        bean.setUpdateTime(now);
        courseInfoService.add(bean);
        return bean.getCourseId();
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateCourse(CourseInfo bean) {
        if (StringTools.isEmpty(bean.getCourseId())) {
            throw new BusinessException("课程ID不能为空");
        }
        CourseInfo exist = courseInfoService.getCourseInfoByCourseId(bean.getCourseId());
        if (exist == null) {
            throw new BusinessException("课程不存在");
        }
        fillStageByGrade(bean);
        bean.setUpdateTime(new Date());
        courseInfoService.updateCourseInfoByCourseId(bean, bean.getCourseId());
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteCourse(String courseId) {
        if (StringTools.isEmpty(courseId)) {
            throw new BusinessException("课程ID不能为空");
        }
        courseInfoService.getCourseInfoByCourseId(courseId);

        deleteLessonResourcesByCourse(courseId);
        CourseChapterLessonQuery lessonQuery = new CourseChapterLessonQuery();
        lessonQuery.setCourseId(courseId);
        courseChapterLessonService.deleteByParam(lessonQuery);

        CourseChapterQuery chapterQuery = new CourseChapterQuery();
        chapterQuery.setCourseId(courseId);
        courseChapterService.deleteByParam(chapterQuery);

        CourseStudyProgressQuery progressQuery = new CourseStudyProgressQuery();
        progressQuery.setCourseId(courseId);
        courseStudyProgressService.deleteByParam(progressQuery);

        CourseStudyLogQuery logQuery = new CourseStudyLogQuery();
        logQuery.setCourseId(courseId);
        courseStudyLogService.deleteByParam(logQuery);

        CourseStudyLessonProgressQuery lessonProgressQuery = new CourseStudyLessonProgressQuery();
        lessonProgressQuery.setCourseId(courseId);
        courseStudyLessonProgressService.deleteByParam(lessonProgressQuery);

        courseInfoService.deleteCourseInfoByCourseId(courseId);
    }

    public CourseDetailVO courseDetail(String courseId) {
        CourseInfo course = getCourse(courseId);
        CourseDetailVO detail = new CourseDetailVO();
        detail.setCourse(course);

        CourseChapterQuery chapterQuery = new CourseChapterQuery();
        chapterQuery.setCourseId(courseId);
        chapterQuery.setOrderBy("sort asc, create_time asc");
        List<CourseChapter> chapters = courseChapterService.findListByParam(chapterQuery);

        List<CourseChapterDetailVO> chapterVOs = new ArrayList<>();
        Map<String, CourseLessonQuiz> quizEnabledMap = quizEnabledMap(courseId);
        for (CourseChapter chapter : chapters) {
            CourseChapterDetailVO chapterVO = new CourseChapterDetailVO();
            chapterVO.setChapter(chapter);
            chapterVO.setLessons(lessonDetails(chapter.getChapterId(), courseId, quizEnabledMap));
            chapterVOs.add(chapterVO);
        }
        detail.setChapters(chapterVOs);
        return detail;
    }

    /**
     * 一次查询课程下全部测验配置，构建 lessonId → 配置 映射（避免循环查库）
     */
    private Map<String, CourseLessonQuiz> quizEnabledMap(String courseId) {
        CourseLessonQuizQuery quizQuery = new CourseLessonQuizQuery();
        quizQuery.setCourseId(courseId);
        List<CourseLessonQuiz> quizList = courseLessonQuizService.findListByParam(quizQuery);
        Map<String, CourseLessonQuiz> map = new HashMap<>();
        for (CourseLessonQuiz quiz : quizList) {
            map.put(quiz.getLessonId(), quiz);
        }
        return map;
    }

    public List<CourseChapter> chapterList(String courseId) {
        if (StringTools.isEmpty(courseId)) {
            throw new BusinessException("课程ID不能为空");
        }
        CourseChapterQuery query = new CourseChapterQuery();
        query.setCourseId(courseId);
        query.setOrderBy("sort asc, create_time asc");
        return courseChapterService.findListByParam(query);
    }

    @Transactional(rollbackFor = Exception.class)
    public String addChapter(CourseChapter bean) {
        if (StringTools.isEmpty(bean.getCourseId())) {
            throw new BusinessException("课程ID不能为空");
        }
        if (StringTools.isEmpty(bean.getChapterName())) {
            throw new BusinessException("章节名称不能为空");
        }
        getCourse(bean.getCourseId());
        bean.setChapterId(StringTools.getRandomNumber(Constants.LENGTH_15));
        if (bean.getSort() == null) {
            bean.setSort(nextChapterSort(bean.getCourseId()));
        }
        if (bean.getStatus() == null) {
            bean.setStatus(0);
        }
        Date now = new Date();
        bean.setCreateTime(now);
        bean.setUpdateTime(now);
        courseChapterService.add(bean);
        return bean.getChapterId();
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateChapter(CourseChapter bean) {
        if (StringTools.isEmpty(bean.getChapterId())) {
            throw new BusinessException("章节ID不能为空");
        }
        if (courseChapterService.getCourseChapterByChapterId(bean.getChapterId()) == null) {
            throw new BusinessException("章节不存在");
        }
        bean.setUpdateTime(new Date());
        courseChapterService.updateCourseChapterByChapterId(bean, bean.getChapterId());
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteChapter(String chapterId) {
        if (StringTools.isEmpty(chapterId)) {
            throw new BusinessException("章节ID不能为空");
        }
        CourseChapter chapter = courseChapterService.getCourseChapterByChapterId(chapterId);
        if (chapter == null) {
            throw new BusinessException("章节不存在");
        }
        CourseChapterLessonQuery lessonQuery = new CourseChapterLessonQuery();
        lessonQuery.setChapterId(chapterId);
        List<CourseChapterLesson> lessons = courseChapterLessonService.findListByParam(lessonQuery);
        for (CourseChapterLesson lesson : lessons) {
            deleteLessonResourcesByLesson(lesson.getLessonId());
            deleteLessonStudyData(lesson.getLessonId());
            courseLessonQuizService.deleteCourseLessonQuizByLessonId(lesson.getLessonId());
        }
        courseChapterLessonService.deleteByParam(lessonQuery);
        courseChapterService.deleteCourseChapterByChapterId(chapterId);
        refreshLessonCount(chapter.getCourseId());
    }

    public List<CourseChapterLesson> lessonList(String chapterId, String courseId) {
        CourseChapterLessonQuery query = new CourseChapterLessonQuery();
        if (!StringTools.isEmpty(chapterId)) {
            query.setChapterId(chapterId);
        }
        if (!StringTools.isEmpty(courseId)) {
            query.setCourseId(courseId);
        }
        query.setOrderBy("sort asc, create_time asc");
        return courseChapterLessonService.findListByParam(query);
    }

    @Transactional(rollbackFor = Exception.class)
    public String addLesson(CourseChapterLesson bean) {
        if (StringTools.isEmpty(bean.getChapterId()) || StringTools.isEmpty(bean.getCourseId())) {
            throw new BusinessException("章节和课程不能为空");
        }
        if (StringTools.isEmpty(bean.getLessonName())) {
            throw new BusinessException("课时名称不能为空");
        }
        getCourse(bean.getCourseId());
        if (courseChapterService.getCourseChapterByChapterId(bean.getChapterId()) == null) {
            throw new BusinessException("章节不存在");
        }
        bean.setLessonId(StringTools.getRandomNumber(Constants.LENGTH_15));
        if (bean.getSort() == null) {
            bean.setSort(nextLessonSort(bean.getChapterId()));
        }
        if (bean.getStatus() == null) {
            bean.setStatus(0);
        }
        Date now = new Date();
        bean.setCreateTime(now);
        bean.setUpdateTime(now);
        courseChapterLessonService.add(bean);
        refreshLessonCount(bean.getCourseId());
        return bean.getLessonId();
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateLesson(CourseChapterLesson bean) {
        if (StringTools.isEmpty(bean.getLessonId())) {
            throw new BusinessException("课时ID不能为空");
        }
        if (courseChapterLessonService.getCourseChapterLessonByLessonId(bean.getLessonId()) == null) {
            throw new BusinessException("课时不存在");
        }
        bean.setUpdateTime(new Date());
        courseChapterLessonService.updateCourseChapterLessonByLessonId(bean, bean.getLessonId());
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteLesson(String lessonId) {
        if (StringTools.isEmpty(lessonId)) {
            throw new BusinessException("课时ID不能为空");
        }
        CourseChapterLesson lesson = courseChapterLessonService.getCourseChapterLessonByLessonId(lessonId);
        if (lesson == null) {
            throw new BusinessException("课时不存在");
        }
        deleteLessonResourcesByLesson(lessonId);
        deleteLessonStudyData(lessonId);
        courseLessonQuizService.deleteCourseLessonQuizByLessonId(lessonId);
        courseChapterLessonService.deleteCourseChapterLessonByLessonId(lessonId);
        refreshLessonCount(lesson.getCourseId());
    }

    public List<CourseLessonResourceVO> lessonResourceList(String lessonId) {
        if (StringTools.isEmpty(lessonId)) {
            throw new BusinessException("课时ID不能为空");
        }
        CourseChapterLessonResourceQuery query = new CourseChapterLessonResourceQuery();
        query.setLessonId(lessonId);
        query.setOrderBy("sort asc, id asc");
        List<CourseChapterLessonResource> binds = courseChapterLessonResourceService.findListByParam(query);
        List<CourseLessonResourceVO> result = new ArrayList<>();
        for (CourseChapterLessonResource bind : binds) {
            result.add(toLessonResourceVO(bind));
        }
        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    public void bindResources(LessonResourceBindDTO dto) {
        if (dto == null || StringTools.isEmpty(dto.getLessonId())) {
            throw new BusinessException("课时ID不能为空");
        }
        if (dto.getResourceIds() == null || dto.getResourceIds().isEmpty()) {
            throw new BusinessException("请选择要绑定的资源");
        }
        CourseChapterLesson lesson = courseChapterLessonService.getCourseChapterLessonByLessonId(dto.getLessonId());
        if (lesson == null) {
            throw new BusinessException("课时不存在");
        }
        CourseInfo course = courseInfoService.getCourseInfoByCourseId(lesson.getCourseId());
        if (course == null) {
            throw new BusinessException("课程不存在");
        }
        String courseStage = course.getStage();

        CourseChapterLessonResourceQuery existQuery = new CourseChapterLessonResourceQuery();
        existQuery.setLessonId(dto.getLessonId());
        List<CourseChapterLessonResource> existList = courseChapterLessonResourceService.findListByParam(existQuery);
        Set<String> existResourceIds = new HashSet<>();
        int maxSort = 0;
        for (CourseChapterLessonResource item : existList) {
            existResourceIds.add(item.getResourceId());
            if (item.getSort() != null && item.getSort() > maxSort) {
                maxSort = item.getSort();
            }
        }

        int sort = maxSort;
        for (String resourceId : dto.getResourceIds()) {
            if (StringTools.isEmpty(resourceId) || existResourceIds.contains(resourceId)) {
                continue;
            }
            ResourceInfo resource = resourceInfoService.getResourceInfoByResourceId(resourceId);
            if (resource == null || resource.getStatus() == null || resource.getStatus() != 1) {
                throw new BusinessException("资源不存在或不可用");
            }
            if (!StringTools.isEmpty(resource.getStage()) && !StringTools.isEmpty(courseStage)
                    && !courseStage.equals(resource.getStage())) {
                String resourceName = StringTools.isEmpty(resource.getResourceName())
                        ? resourceId : resource.getResourceName();
                throw new BusinessException("不能绑定《" + resourceName + "》：资源学段（"
                        + resource.getStage() + "）与课程学段（" + courseStage + "）不一致");
            }
            CourseChapterLessonResource bind = new CourseChapterLessonResource();
            bind.setLessonId(dto.getLessonId());
            bind.setCourseId(lesson.getCourseId());
            bind.setResourceId(resourceId);
            bind.setSort(++sort);
            bind.setCreateTime(new Date());
            courseChapterLessonResourceService.add(bind);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void unbindResource(Integer id) {
        if (id == null) {
            throw new BusinessException("资源绑定ID不能为空");
        }
        courseChapterLessonResourceService.deleteCourseChapterLessonResourceById(id);
    }

    /**
     * 课时通关测验详情：配置 + 已关联题目（quizMode=0 或未配置时 quiz 为空）
     */
    public LessonQuizDetailVO lessonQuizDetail(String lessonId) {
        if (StringTools.isEmpty(lessonId)) {
            throw new BusinessException("课时ID不能为空");
        }
        CourseLessonQuiz quiz = courseLessonQuizService.getCourseLessonQuizByLessonId(lessonId);
        LessonQuizDetailVO vo = new LessonQuizDetailVO();
        if (quiz == null || QUIZ_MODE_CLOSED == quiz.getQuizMode()) {
            vo.setQuiz(null);
            vo.setQuestions(List.of());
            vo.setQuestionScores(Map.of());
            vo.setPartialCredit(false);
            return vo;
        }
        List<String> ids = parseQuestionIds(quiz.getQuestionIds());
        List<QuestionInfo> questions = ids.isEmpty() ? List.of() : loadQuestions(ids);
        vo.setQuiz(quiz);
        vo.setQuestions(questions);
        vo.setQuestionScores(parseQuestionScores(quiz.getQuizConfig()));
        vo.setPartialCredit(parsePartialCreditConfig(quiz.getQuizConfig()));
        return vo;
    }

    /**
     * 保存课时通关测验配置（关闭/题库选题）；AI 出题走异步接口 quizGenerateAsync。
     * 题库模式校验：每题分值必填（≥1），合计总分必须达到及格线。
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveLessonQuiz(LessonQuizSaveDTO dto) {
        if (dto == null || StringTools.isEmpty(dto.getLessonId())) {
            throw new BusinessException("课时ID不能为空");
        }
        if (dto.getQuizMode() == null || dto.getQuizMode() < 0 || dto.getQuizMode() > 2) {
            throw new BusinessException("非法的测验方式");
        }
        if (QUIZ_MODE_AI == dto.getQuizMode()) {
            throw new BusinessException("AI 出题已改为异步生成，请使用 quizGenerateAsync 接口");
        }
        CourseChapterLesson lesson = courseChapterLessonService.getCourseChapterLessonByLessonId(dto.getLessonId());
        if (lesson == null) {
            throw new BusinessException("课时不存在");
        }
        CourseInfo course = courseInfoService.getCourseInfoByCourseId(lesson.getCourseId());
        if (course == null) {
            throw new BusinessException("课程不存在");
        }
        if (dto.getPassScore() == null || dto.getPassScore() < 1 || dto.getPassScore() > 100) {
            throw new BusinessException("及格分必须为 1-100");
        }
        if (dto.getUnlockNext() == null || (dto.getUnlockNext() != 0 && dto.getUnlockNext() != 1)) {
            throw new BusinessException("非法的门禁设置");
        }

        Date now = new Date();
        List<String> questionIds = new ArrayList<>();
        if (QUIZ_MODE_CLOSED == dto.getQuizMode()) {
            // 关闭测验：清空关联题目
        } else {
            // 题库选题：校验题目可用 + 分值必填 + 总分达到及格线
            questionIds.addAll(requireQuestions(dto.getQuestionIds()));
            validateQuestionScores(dto, questionIds, dto.getPassScore());
        }

        CourseLessonQuiz bean = new CourseLessonQuiz();
        bean.setCourseId(lesson.getCourseId());
        bean.setQuizMode(dto.getQuizMode());
        bean.setQuestionIds(questionIds.isEmpty() ? "[]" : JSON.toJSONString(questionIds));
        bean.setQuestionCount(dto.getQuestionCount() == null ? 5 : dto.getQuestionCount());
        bean.setDifficulty(dto.getDifficulty() == null ? 1 : dto.getDifficulty());
        bean.setPassScore(dto.getPassScore());
        bean.setUnlockNext(dto.getUnlockNext());
        bean.setQuizConfig(buildQuizConfig(dto, questionIds));
        bean.setStatus(1);
        bean.setUpdateTime(now);
        CourseLessonQuiz exist = courseLessonQuizService.getCourseLessonQuizByLessonId(dto.getLessonId());
        if (exist == null) {
            bean.setLessonId(dto.getLessonId());
            bean.setCreateTime(now);
            courseLessonQuizService.add(bean);
        } else {
            courseLessonQuizService.updateCourseLessonQuizByLessonId(bean, dto.getLessonId());
        }
    }

    /**
     * 关闭课时通关测验：删除配置行，回到未配置状态
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteLessonQuiz(String lessonId) {
        if (StringTools.isEmpty(lessonId)) {
            throw new BusinessException("课时ID不能为空");
        }
        courseLessonQuizService.deleteCourseLessonQuizByLessonId(lessonId);
    }

    /**
     * 题库模式分值校验：每道已选题必须设置分值（≥1），合计总分必须达到及格线
     */
    private void validateQuestionScores(LessonQuizSaveDTO dto, List<String> questionIds, int passScore) {
        Map<String, Integer> scores = dto.getQuestionScores();
        if (scores == null || scores.isEmpty()) {
            throw new BusinessException("请为每道题设置分值");
        }
        int total = 0;
        for (String questionId : questionIds) {
            Integer score = scores.get(questionId);
            if (score == null || score < 1) {
                throw new BusinessException("请为每道题设置分值（存在未设置或非法分值的题目）");
            }
            total += Math.min(score, 100);
        }
        if (total < passScore) {
            throw new BusinessException("测验总分为 " + total + " 分，未达到及格线 " + passScore + " 分，请调整各题分值");
        }
    }

    /**
     * AI 出题异步入口：校验课程/知识点后提交任务，立即返回任务状态（前端轮询 quizTask）
     */
    public LessonQuizTaskVO startQuizGenerateAsync(LessonQuizSaveDTO dto) {
        if (dto == null || StringTools.isEmpty(dto.getLessonId())) {
            throw new BusinessException("课时ID不能为空");
        }
        CourseChapterLesson lesson = courseChapterLessonService.getCourseChapterLessonByLessonId(dto.getLessonId());
        if (lesson == null) {
            throw new BusinessException("课时不存在");
        }
        CourseInfo course = courseInfoService.getCourseInfoByCourseId(lesson.getCourseId());
        if (course == null || course.getStatus() == null || course.getStatus() != 1) {
            throw new BusinessException("课程不存在或暂不可用");
        }
        if (dto.getPassScore() == null || dto.getPassScore() < 1 || dto.getPassScore() > 100) {
            throw new BusinessException("及格分必须为 1-100");
        }
        if (dto.getUnlockNext() == null || (dto.getUnlockNext() != 0 && dto.getUnlockNext() != 1)) {
            throw new BusinessException("非法的门禁设置");
        }
        if (StringTools.isEmpty(dto.getKnowledgePointId())) {
            throw new BusinessException("请选择知识点（AI 出题的题目将挂载到该知识点）");
        }
        KnowledgePoint point = knowledgePointService.getKnowledgePointByKnowledgePointId(dto.getKnowledgePointId());
        if (point == null) {
            throw new BusinessException("知识点不存在");
        }
        if (point.getStage() != null && !point.getStage().equals(course.getStage())) {
            throw new BusinessException("知识点与课程学段不匹配");
        }
        String topic = StringTools.isEmpty(dto.getTopic()) ? lesson.getLessonName() : dto.getTopic().trim();
        int count = dto.getQuestionCount() == null ? 5 : Math.max(1, Math.min(dto.getQuestionCount(), 6));

        LessonQuizTaskVO task = new LessonQuizTaskVO();
        task.setLessonId(lesson.getLessonId());
        task.setCourseId(lesson.getCourseId());
        task.setStage(course.getStage());
        task.setGrade(course.getGrade());
        task.setTopic(topic);
        task.setKnowledgePointId(dto.getKnowledgePointId());
        task.setKnowledgePointName(point.getName());
        task.setTotal(count);
        task.setDifficulty(dto.getDifficulty() == null ? 1 : dto.getDifficulty());
        task.setPassScore(dto.getPassScore());
        task.setUnlockNext(dto.getUnlockNext());
        task.setPartialCredit(Boolean.TRUE.equals(dto.getPartialCredit()));
        return lessonQuizTaskComponent.start(task);
    }

    private List<String> requireQuestions(List<String> questionIds) {        if (questionIds == null || questionIds.isEmpty()) {
            throw new BusinessException("请选择测验题目");
        }
        List<String> distinct = new ArrayList<>(new HashSet<>(questionIds));
        QuestionInfoQuery query = new QuestionInfoQuery();
        query.setQuestionIds(distinct);
        List<QuestionInfo> questions = questionInfoService.findListByParam(query);
        Set<String> existIds = new HashSet<>();
        for (QuestionInfo question : questions) {
            if (question.getStatus() == null || question.getStatus() != 1) {
                throw new BusinessException("题目已下架：" + question.getTitle());
            }
            existIds.add(question.getQuestionId());
        }
        for (String id : distinct) {
            if (!existIds.contains(id)) {
                throw new BusinessException("题目不存在或不可用：" + id);
            }
        }
        return distinct;
    }

    private List<QuestionInfo> loadQuestions(List<String> questionIds) {
        QuestionInfoQuery query = new QuestionInfoQuery();
        query.setQuestionIds(questionIds);
        List<QuestionInfo> questions = questionInfoService.findListByParam(query);
        questions.sort((a, b) -> questionIds.indexOf(a.getQuestionId()) - questionIds.indexOf(b.getQuestionId()));
        return questions;
    }

    private List<String> parseQuestionIds(String jsonText) {
        if (StringTools.isEmpty(jsonText)) {
            return new ArrayList<>();
        }
        try {
            JSONArray array = JSON.parseArray(jsonText);
            List<String> ids = new ArrayList<>();
            if (array != null) {
                for (int i = 0; i < array.size(); i++) {
                    String id = array.getString(i);
                    if (!StringTools.isEmpty(id)) {
                        ids.add(id);
                    }
                }
            }
            return ids;
        } catch (Exception e) {
            log.warn("课时测验题目ID解析失败", e);
            return new ArrayList<>();
        }
    }

    /**
     * 构建 quiz_config 扩展 JSON（弹性扩展配置，后续能力直接进此列不加表）：
     * {"questionScores": {"questionId": 分值}}
     */
    private String buildQuizConfig(LessonQuizSaveDTO dto, List<String> questionIds) {
        Map<String, Integer> scores = new LinkedHashMap<>();
        if (dto.getQuestionScores() != null) {
            for (String questionId : questionIds) {
                Integer score = dto.getQuestionScores().get(questionId);
                if (score != null) {
                    int safe = Math.max(1, Math.min(score, 100));
                    scores.put(questionId, safe);
                }
            }
        }
        JSONObject config = new JSONObject();
        if (!scores.isEmpty()) {
            config.put("questionScores", scores);
        }
        if (Boolean.TRUE.equals(dto.getPartialCredit())) {
            config.put("partialCredit", true);
        }
        return config.isEmpty() ? null : config.toJSONString();
    }

    /** quiz_config.partialCredit：多选题漏选（未错选）按比例部分给分 */
    private boolean parsePartialCreditConfig(String quizConfig) {
        if (StringTools.isEmpty(quizConfig)) {
            return false;
        }
        try {
            JSONObject config = JSON.parseObject(quizConfig);
            return config != null && Boolean.TRUE.equals(config.getBoolean("partialCredit"));
        } catch (Exception e) {
            return false;
        }
    }

    private Map<String, Integer> parseQuestionScores(String quizConfig) {
        if (StringTools.isEmpty(quizConfig)) {
            return new LinkedHashMap<>();
        }
        try {
            JSONObject config = JSON.parseObject(quizConfig);
            JSONObject scores = config == null ? null : config.getJSONObject("questionScores");
            Map<String, Integer> map = new LinkedHashMap<>();
            if (scores != null) {
                for (String key : scores.keySet()) {
                    Integer value = scores.getInteger(key);
                    if (value != null) {
                        map.put(key, value);
                    }
                }
            }
            return map;
        } catch (Exception e) {
            log.warn("课时测验分值配置解析失败", e);
            return new LinkedHashMap<>();
        }
    }

    private List<CourseLessonDetailVO> lessonDetails(String chapterId, String courseId,
                                                     Map<String, CourseLessonQuiz> quizEnabledMap) {
        CourseChapterLessonQuery lessonQuery = new CourseChapterLessonQuery();
        lessonQuery.setChapterId(chapterId);
        lessonQuery.setCourseId(courseId);
        lessonQuery.setOrderBy("sort asc, create_time asc");
        List<CourseChapterLesson> lessons = courseChapterLessonService.findListByParam(lessonQuery);
        List<CourseLessonDetailVO> lessonVOs = new ArrayList<>();
        for (CourseChapterLesson lesson : lessons) {
            CourseLessonDetailVO lessonVO = new CourseLessonDetailVO();
            lessonVO.setLesson(lesson);
            lessonVO.setResources(lessonResourceList(lesson.getLessonId()));
            CourseLessonQuiz quiz = quizEnabledMap.get(lesson.getLessonId());
            lessonVO.setQuizEnabled(quiz != null && quiz.getQuizMode() != null && quiz.getQuizMode() > 0);
            lessonVOs.add(lessonVO);
        }
        return lessonVOs;
    }

    private CourseLessonResourceVO toLessonResourceVO(CourseChapterLessonResource bind) {
        CourseLessonResourceVO vo = new CourseLessonResourceVO();
        vo.setId(bind.getId());
        vo.setLessonId(bind.getLessonId());
        vo.setCourseId(bind.getCourseId());
        vo.setResourceId(bind.getResourceId());
        vo.setSort(bind.getSort());
        vo.setCreateTime(bind.getCreateTime());
        ResourceInfo resource = resourceInfoService.getResourceInfoByResourceId(bind.getResourceId());
        if (resource != null) {
            vo.setResourceName(resource.getResourceName());
            vo.setResourceType(resource.getResourceType());
            vo.setDescription(resource.getDescription());
            vo.setCover(resource.getCover());
            vo.setDuration(resource.getDuration());
        }
        return vo;
    }

    private void fillStageByGrade(CourseInfo bean) {
        if (!StringTools.isEmpty(bean.getGrade())) {
            String stage = StageEnum.matchByGrade(bean.getGrade());
            if (stage == null) {
                throw new BusinessException("非法的年级");
            }
            bean.setStage(stage);
        }
        if (StringTools.isEmpty(bean.getStage())) {
            throw new BusinessException("请选择年级");
        }
    }

    private int nextChapterSort(String courseId) {
        CourseChapterQuery query = new CourseChapterQuery();
        query.setCourseId(courseId);
        List<CourseChapter> list = courseChapterService.findListByParam(query);
        return list.stream().map(CourseChapter::getSort).filter(sort -> sort != null)
                .max(Integer::compareTo).orElse(0) + 1;
    }

    private int nextLessonSort(String chapterId) {
        CourseChapterLessonQuery query = new CourseChapterLessonQuery();
        query.setChapterId(chapterId);
        List<CourseChapterLesson> list = courseChapterLessonService.findListByParam(query);
        return list.stream().map(CourseChapterLesson::getSort).filter(sort -> sort != null)
                .max(Integer::compareTo).orElse(0) + 1;
    }

    private void refreshLessonCount(String courseId) {
        CourseChapterLessonQuery query = new CourseChapterLessonQuery();
        query.setCourseId(courseId);
        int count = courseChapterLessonService.findCountByParam(query);
        CourseInfo updateBean = new CourseInfo();
        updateBean.setLessonCount(count);
        updateBean.setUpdateTime(new Date());
        courseInfoService.updateCourseInfoByCourseId(updateBean, courseId);
    }

    private void deleteLessonResourcesByCourse(String courseId) {
        CourseChapterLessonResourceQuery query = new CourseChapterLessonResourceQuery();
        query.setCourseId(courseId);
        courseChapterLessonResourceService.deleteByParam(query);

        CourseLessonQuizQuery quizQuery = new CourseLessonQuizQuery();
        quizQuery.setCourseId(courseId);
        courseLessonQuizService.deleteByParam(quizQuery);
    }

    private void deleteLessonResourcesByLesson(String lessonId) {
        CourseChapterLessonResourceQuery query = new CourseChapterLessonResourceQuery();
        query.setLessonId(lessonId);
        courseChapterLessonResourceService.deleteByParam(query);
    }

    private void deleteLessonStudyData(String lessonId) {
        CourseStudyLessonProgressQuery lessonProgressQuery = new CourseStudyLessonProgressQuery();
        lessonProgressQuery.setLessonId(lessonId);
        courseStudyLessonProgressService.deleteByParam(lessonProgressQuery);

        CourseStudyLogQuery logQuery = new CourseStudyLogQuery();
        logQuery.setLessonId(lessonId);
        courseStudyLogService.deleteByParam(logQuery);
    }
}
