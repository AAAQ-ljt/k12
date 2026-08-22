package com.nexora.controller;

import com.nexora.annotation.GlobalInterceptor;
import com.nexora.entity.dto.TokenUserInfoDTO;
import com.nexora.entity.po.CourseChapter;
import com.nexora.entity.po.CourseChapterLesson;
import com.nexora.entity.po.CourseChapterLessonResource;
import com.nexora.entity.po.CourseInfo;
import com.nexora.entity.po.KnowledgeDoc;
import com.nexora.entity.po.ResourceInfo;
import com.nexora.entity.query.CourseChapterLessonQuery;
import com.nexora.entity.query.CourseChapterLessonResourceQuery;
import com.nexora.entity.query.CourseChapterQuery;
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
import com.nexora.service.CourseInfoService;
import com.nexora.service.ResourceInfoService;
import com.nexora.service.StudentWikiService;
import com.nexora.utils.LoginUserContext;
import com.nexora.utils.StringTools;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * 学生端课程教材 Controller：按登录学生年级返回课程列表与课程详情。
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
        if (current != null && !StringTools.isEmpty(current.getGrade())) {
            query.setGrade(current.getGrade());
        } else if (current != null && !StringTools.isEmpty(current.getStage())) {
            query.setStage(current.getStage());
        }
        query.setOrderBy("sort asc, create_time desc");
        return getSuccessResponseVO(courseInfoService.findListByPage(query));
    }

    @GetMapping("/getDetail")
    public ResponseVO<CourseDetailVO> getDetail(@RequestParam String courseId) {
        TokenUserInfoDTO current = LoginUserContext.get();
        CourseInfo course = courseInfoService.getCourseInfoByCourseId(courseId);
        if (course == null || course.getStatus() == null || course.getStatus() != 1
                || !courseVisibleTo(course, current)) {
            throw new BusinessException("课程不存在或暂不可用");
        }

        CourseDetailVO detail = new CourseDetailVO();
        detail.setCourse(course);

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
        String stage = StringTools.isEmpty(current.getStage()) ? course.getStage() : current.getStage();
        return getSuccessResponseVO(studentWikiService.syncFromCourse(
                current.getUserId(), stage, courseId, course.getCourseName()));
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
}
