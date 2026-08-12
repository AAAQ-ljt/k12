package com.nexora.admin.controller;

import com.nexora.controller.ABaseController;
import com.nexora.entity.po.CourseChapterLesson;
import com.nexora.entity.query.CourseChapterLessonQuery;
import com.nexora.entity.vo.ResponseVO;
import com.nexora.exception.BusinessException;
import com.nexora.service.CourseChapterLessonService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 课时管理 Controller
 */
@RestController("courseChapterLessonController")
@RequestMapping("/courseChapterLesson")
public class CourseChapterLessonController extends ABaseController {

    private final CourseChapterLessonService courseChapterLessonService;

    public CourseChapterLessonController(CourseChapterLessonService courseChapterLessonService) {
        this.courseChapterLessonService = courseChapterLessonService;
    }

    /**
     * 分页查询课时列表
     */
    @GetMapping("/loadDataList")
    public ResponseVO loadDataList(CourseChapterLessonQuery query) {
        return getSuccessResponseVO(courseChapterLessonService.findListByPage(query));
    }

    /**
     * 按章节ID查询课时列表
     */
    @GetMapping("/loadByChapterId")
    public ResponseVO loadByChapterId(@RequestParam String chapterId) {
        CourseChapterLessonQuery query = new CourseChapterLessonQuery();
        query.setChapterId(chapterId);
        List<CourseChapterLesson> list = courseChapterLessonService.findListByParam(query);
        return getSuccessResponseVO(list);
    }

    /**
     * 获取课时详情
     */
    @GetMapping("/getInfo")
    public ResponseVO getInfo(@RequestParam String lessonId) {
        CourseChapterLesson lesson = courseChapterLessonService.getCourseChapterLessonByLessonId(lessonId);
        if (lesson == null) {
            throw new BusinessException("课时不存在");
        }
        return getSuccessResponseVO(lesson);
    }

    /**
     * 新增课时
     */
    @PostMapping("/add")
    public ResponseVO add(@RequestBody CourseChapterLesson courseChapterLesson) {
        courseChapterLessonService.add(courseChapterLesson);
        return getSuccessResponseVO(null);
    }

    /**
     * 编辑课时
     */
    @PutMapping("/update")
    public ResponseVO update(@RequestBody CourseChapterLesson courseChapterLesson) {
        if (courseChapterLesson.getLessonId() == null || courseChapterLesson.getLessonId().isEmpty()) {
            throw new BusinessException("课时ID不能为空");
        }
        courseChapterLessonService.updateCourseChapterLessonByLessonId(courseChapterLesson, courseChapterLesson.getLessonId());
        return getSuccessResponseVO(null);
    }

    /**
     * 删除课时
     */
    @DeleteMapping("/del")
    public ResponseVO del(@RequestParam String lessonId) {
        courseChapterLessonService.deleteCourseChapterLessonByLessonId(lessonId);
        return getSuccessResponseVO(null);
    }
}
