package com.nexora.admin.controller;

import com.nexora.admin.biz.CourseBiz;
import com.nexora.controller.ABaseController;
import com.nexora.entity.po.CourseChapterLesson;
import com.nexora.entity.vo.ResponseVO;
import jakarta.annotation.Resource;
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
 * 课程课时管理 Controller
 */
@RestController
@RequestMapping("/courseChapterLesson")
public class CourseChapterLessonController extends ABaseController {

    @Resource
    private CourseBiz courseBiz;

    @GetMapping("/loadDataList")
    public ResponseVO<List<CourseChapterLesson>> loadDataList(@RequestParam(required = false) String chapterId,
                                                              @RequestParam(required = false) String courseId) {
        return getSuccessResponseVO(courseBiz.lessonList(chapterId, courseId));
    }

    @PostMapping("/add")
    public ResponseVO<String> add(@RequestBody CourseChapterLesson bean) {
        return getSuccessResponseVO(courseBiz.addLesson(bean));
    }

    @PutMapping("/update")
    public ResponseVO<Void> update(@RequestBody CourseChapterLesson bean) {
        courseBiz.updateLesson(bean);
        return getSuccessResponseVO(null);
    }

    @DeleteMapping("/del")
    public ResponseVO<Void> del(@RequestParam String lessonId) {
        courseBiz.deleteLesson(lessonId);
        return getSuccessResponseVO(null);
    }
}
