package com.nexora.controller;

import com.nexora.annotation.GlobalInterceptor;
import com.nexora.dto.LessonQuizSubmitDTO;
import com.nexora.entity.dto.TokenUserInfoDTO;
import com.nexora.entity.po.CourseChapter;
import com.nexora.entity.po.CourseChapterLesson;
import com.nexora.entity.po.CourseChapterLessonResource;
import com.nexora.entity.po.CourseEnrollment;
import com.nexora.entity.po.CourseInfo;
import com.nexora.entity.po.KnowledgeDoc;
import com.nexora.entity.po.ResourceInfo;
import com.nexora.entity.query.CourseChapterLessonQuery;
import com.nexora.entity.query.CourseChapterLessonResourceQuery;
import com.nexora.entity.query.CourseChapterQuery;
import com.nexora.entity.query.CourseEnrollmentQuery;
import com.nexora.entity.query.CourseInfoQuery;
import com.nexora.entity.vo.CourseChapterDetailVO;
import com.nexora.entity.vo.CourseDetailVO;
import com.nexora.entity.vo.CourseLessonDetailVO;
import com.nexora.entity.vo.CourseLessonResourceVO;
import com.nexora.entity.vo.PaginationResultVO;
import com.nexora.entity.vo.ResponseVO;
import com.nexora.exception.BusinessException;
import com.nexora.service.CourseChapterLessonResourceService;
import com.nexora.service.CourseChapterLessonService;
import com.nexora.service.CourseChapterService;
import com.nexora.service.CourseEnrollmentService;
import com.nexora.service.CourseInfoService;
import com.nexora.service.CourseQuizBiz;
import com.nexora.service.CourseStudyBiz;
import com.nexora.service.ResourceInfoService;
import com.nexora.service.StudentWikiService;
import com.nexora.utils.LoginUserContext;
import com.nexora.utils.StringTools;
import com.nexora.vo.LessonQuizStatusVO;
import com.nexora.vo.LessonQuizSubmitResultVO;
import com.nexora.vo.LessonQuizVO;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 学生端课程教材 Controller：按登录学生年级返回课程列表与课程详情。
 * 课程须先加入（course_enrollment）才可学习：详情/测验/同步知识页均校验加入状态。
 */
@RestController
@RequestMapping("/courseInfo")
@GlobalInterceptor(checkLogin = true)
public class StudentCourseController extends ABaseController {

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
    private StudentWikiService studentWikiService;

    @Resource
    private CourseQuizBiz courseQuizBiz;

    @Resource
    private CourseStudyBiz courseStudyBiz;

    @Resource
    private CourseEnrollmentService courseEnrollmentService;

    @GetMapping("/loadMyCourses")
    public ResponseVO<PaginationResultVO<CourseInfo>> loadMyCourses(CourseInfoQuery query) {
        TokenUserInfoDTO current = LoginUserContext.get();
        if (query.getPageNo() == null) {
            query.setPageNo(1);
        }
        if (query.getPageSize() == null) {
            query.setPageSize(20);
        }
        query.setStatus(1);
        applyGradeFilter(query, current);
        List<String> joinedIds = joinedCourseIds(current == null ? null : current.getUserId());
        if (joinedIds.isEmpty()) {
            return getSuccessResponseVO(new PaginationResultVO<>(0, query.getPageSize(), 1, List.of()));
        }
        query.setCourseIds(joinedIds);
        query.setOrderBy("sort asc, create_time desc");
        return getSuccessResponseVO(courseInfoService.findListByPage(query));
    }

    /**
     * 可加入课程：同年级上架且尚未加入的课程
     */
    @GetMapping("/loadJoinCourses")
    public ResponseVO<PaginationResultVO<CourseInfo>> loadJoinCourses(CourseInfoQuery query) {
        TokenUserInfoDTO current = LoginUserContext.get();
        if (query.getPageNo() == null) {
            query.setPageNo(1);
        }
        if (query.getPageSize() == null) {
            query.setPageSize(20);
        }
        query.setStatus(1);
        applyGradeFilter(query, current);
        List<String> joinedIds = joinedCourseIds(current == null ? null : current.getUserId());
        if (!joinedIds.isEmpty()) {
            query.setExcludeCourseIds(joinedIds);
        }
        query.setOrderBy("sort asc, create_time desc");
        return getSuccessResponseVO(courseInfoService.findListByPage(query));
    }

    /**
     * 加入课程：幂等，重复加入不产生重复记录
     */
    @PostMapping("/join")
    public ResponseVO<Void> join(@RequestParam String courseId) {
        TokenUserInfoDTO current = LoginUserContext.get();
        CourseInfo course = courseInfoService.getCourseInfoByCourseId(courseId);
        if (course == null || course.getStatus() == null || course.getStatus() != 1
                || !courseVisibleTo(course, current)) {
            throw new BusinessException("课程不存在或暂不可用");
        }
        Date now = new Date();
        CourseEnrollment enrollment =
                courseEnrollmentService.getCourseEnrollmentByUserIdAndCourseId(current.getUserId(), courseId);
        if (enrollment == null) {
            CourseEnrollment bean = new CourseEnrollment();
            bean.setUserId(current.getUserId());
            bean.setCourseId(courseId);
            bean.setStatus(1);
            bean.setCreateTime(now);
            bean.setUpdateTime(now);
            courseEnrollmentService.add(bean);
        } else if (enrollment.getStatus() == null || enrollment.getStatus() != 1) {
            CourseEnrollment update = new CourseEnrollment();
            update.setStatus(1);
            update.setUpdateTime(now);
            courseEnrollmentService.updateCourseEnrollmentByUserIdAndCourseId(update, current.getUserId(), courseId);
        }
        return getSuccessResponseVO(null);
    }

    @GetMapping("/getDetail")
    public ResponseVO<CourseDetailVO> getDetail(@RequestParam String courseId) {
        TokenUserInfoDTO current = LoginUserContext.get();
        CourseInfo course = courseInfoService.getCourseInfoByCourseId(courseId);
        if (course == null || course.getStatus() == null || course.getStatus() != 1
                || !courseVisibleTo(course, current)) {
            throw new BusinessException("课程不存在或暂不可用");
        }
        boolean enrolled = isEnrolled(current.getUserId(), courseId);

        CourseDetailVO detail = new CourseDetailVO();
        detail.setCourse(course);
        detail.setEnrolled(enrolled);
        // 未加入课程只返回课程基本信息，章节/课时/资源在加入后才可见
        if (!enrolled) {
            detail.setChapters(List.of());
            return getSuccessResponseVO(detail);
        }

        CourseChapterQuery chapterQuery = new CourseChapterQuery();
        chapterQuery.setCourseId(courseId);
        chapterQuery.setStatus(0);
        chapterQuery.setOrderBy("sort asc, create_time asc");
        List<CourseChapter> chapters = courseChapterService.findListByParam(chapterQuery);

        List<CourseChapterDetailVO> chapterVOs = new ArrayList<>();
        for (CourseChapter chapter : chapters) {
            CourseChapterDetailVO chapterVO = new CourseChapterDetailVO();
            chapterVO.setChapter(chapter);
            chapterVO.setLessons(loadLessons(chapter.getChapterId(), courseId));
            chapterVOs.add(chapterVO);
        }
        detail.setChapters(chapterVOs);
        return getSuccessResponseVO(detail);
    }

    /**
     * 同步课程教材为知识页草稿（主线 6）：AI 将课程绑定资源（名称+简介）整理后落个人知识库草稿，按课程去重
     */
    @PostMapping("/syncWiki")
    public ResponseVO<KnowledgeDoc> syncWiki(@RequestParam String courseId) {
        TokenUserInfoDTO current = LoginUserContext.get();
        CourseInfo course = courseInfoService.getCourseInfoByCourseId(courseId);
        if (course == null || course.getStatus() == null || course.getStatus() != 1
                || !courseVisibleTo(course, current)) {
            throw new BusinessException("课程不存在或暂不可用");
        }
        if (!isEnrolled(current.getUserId(), courseId)) {
            throw new BusinessException("请先加入该课程后再同步知识页");
        }
        String stage = StringTools.isEmpty(current.getStage()) ? course.getStage() : current.getStage();
        return getSuccessResponseVO(studentWikiService.syncFromCourse(
                current.getUserId(), stage, courseId, course.getCourseName()));
    }

    /**
     * 课时通关测验：答题面板数据（未配置/关闭返回 null）
     */
    @GetMapping("/lessonQuiz")
    public ResponseVO<LessonQuizVO> lessonQuiz(@RequestParam String lessonId) {
        TokenUserInfoDTO current = LoginUserContext.get();
        return getSuccessResponseVO(courseQuizBiz.quiz(current.getUserId(), lessonId));
    }

    /**
     * 课时通关测验：提交判分
     */
    @PostMapping("/lessonQuizSubmit")
    public ResponseVO<LessonQuizSubmitResultVO> lessonQuizSubmit(@RequestBody LessonQuizSubmitDTO dto) {
        TokenUserInfoDTO current = LoginUserContext.get();
        return getSuccessResponseVO(courseQuizBiz.submit(current.getUserId(), current.getStage(), dto));
    }

    /**
     * 课程内各课时测验状态与解锁状态（按学习顺序计算）
     */
    @GetMapping("/lessonQuizStatus")
    public ResponseVO<List<LessonQuizStatusVO>> lessonQuizStatus(@RequestParam String courseId) {
        TokenUserInfoDTO current = LoginUserContext.get();
        return getSuccessResponseVO(courseQuizBiz.quizStatus(current.getUserId(), courseId));
    }

    /**
     * 最近一次测验作答结果还原（章节卡「查看结果」；未作答过返回 null）
     */
    @GetMapping("/lessonQuizResult")
    public ResponseVO<LessonQuizSubmitResultVO> lessonQuizResult(@RequestParam String lessonId) {
        TokenUserInfoDTO current = LoginUserContext.get();
        return getSuccessResponseVO(courseQuizBiz.result(current.getUserId(), lessonId));
    }

    /**
     * 学习进度一期上报：打开课时资源即记课时完成（学习内容强制加入，未加入报业务异常）
     */
    @PostMapping("/reportStudy")
    public ResponseVO<Void> reportStudy(@RequestParam String lessonId, @RequestParam String resourceId) {
        TokenUserInfoDTO current = LoginUserContext.get();
        courseStudyBiz.reportStudy(current.getUserId(), lessonId, resourceId);
        return getSuccessResponseVO(null);
    }

    private List<CourseLessonDetailVO> loadLessons(String chapterId, String courseId) {
        CourseChapterLessonQuery lessonQuery = new CourseChapterLessonQuery();
        lessonQuery.setChapterId(chapterId);
        lessonQuery.setCourseId(courseId);
        lessonQuery.setStatus(0);
        lessonQuery.setOrderBy("sort asc, create_time asc");
        List<CourseChapterLesson> lessons = courseChapterLessonService.findListByParam(lessonQuery);

        List<CourseLessonDetailVO> lessonVOs = new ArrayList<>();
        for (CourseChapterLesson lesson : lessons) {
            CourseLessonDetailVO lessonVO = new CourseLessonDetailVO();
            lessonVO.setLesson(lesson);
            lessonVO.setResources(loadLessonResources(lesson.getLessonId()));
            lessonVOs.add(lessonVO);
        }
        return lessonVOs;
    }

    private List<CourseLessonResourceVO> loadLessonResources(String lessonId) {
        CourseChapterLessonResourceQuery resourceQuery = new CourseChapterLessonResourceQuery();
        resourceQuery.setLessonId(lessonId);
        resourceQuery.setOrderBy("sort asc, id asc");
        List<CourseChapterLessonResource> binds = courseChapterLessonResourceService.findListByParam(resourceQuery);

        List<CourseLessonResourceVO> result = new ArrayList<>();
        for (CourseChapterLessonResource bind : binds) {
            ResourceInfo resource = resourceInfoService.getResourceInfoByResourceId(bind.getResourceId());
            if (resource == null || resource.getStatus() == null || resource.getStatus() != 1) {
                continue;
            }
            CourseLessonResourceVO vo = new CourseLessonResourceVO();
            vo.setId(bind.getId());
            vo.setLessonId(bind.getLessonId());
            vo.setCourseId(bind.getCourseId());
            vo.setResourceId(bind.getResourceId());
            vo.setSort(bind.getSort());
            vo.setCreateTime(bind.getCreateTime());
            vo.setResourceName(resource.getResourceName());
            vo.setResourceType(resource.getResourceType());
            vo.setDescription(resource.getDescription());
            vo.setCover(resource.getCover());
            vo.setDuration(resource.getDuration());
            result.add(vo);
        }
        return result;
    }

    private boolean courseVisibleTo(CourseInfo course, TokenUserInfoDTO current) {
        if (current == null) {
            return false;
        }
        if (!StringTools.isEmpty(course.getGrade()) && !StringTools.isEmpty(current.getGrade())) {
            return course.getGrade().equals(current.getGrade());
        }
        if (!StringTools.isEmpty(course.getStage()) && !StringTools.isEmpty(current.getStage())) {
            return course.getStage().equals(current.getStage());
        }
        return true;
    }

    /** 按登录学生年级/学段过滤课程列表 */
    private void applyGradeFilter(CourseInfoQuery query, TokenUserInfoDTO current) {
        if (current != null && !StringTools.isEmpty(current.getGrade())) {
            query.setGrade(current.getGrade());
        } else if (current != null && !StringTools.isEmpty(current.getStage())) {
            query.setStage(current.getStage());
        }
    }

    /** 当前学生已加入（status=1）的课程ID列表 */
    private List<String> joinedCourseIds(String userId) {
        if (StringTools.isEmpty(userId)) {
            return List.of();
        }
        CourseEnrollmentQuery query = new CourseEnrollmentQuery();
        query.setUserId(userId);
        query.setStatus(1);
        return courseEnrollmentService.findListByParam(query).stream()
                .map(CourseEnrollment::getCourseId)
                .distinct()
                .toList();
    }

    /** 是否已加入课程 */
    private boolean isEnrolled(String userId, String courseId) {
        if (StringTools.isEmpty(userId)) {
            return false;
        }
        CourseEnrollment enrollment =
                courseEnrollmentService.getCourseEnrollmentByUserIdAndCourseId(userId, courseId);
        return enrollment != null && enrollment.getStatus() != null && enrollment.getStatus() == 1;
    }
}
