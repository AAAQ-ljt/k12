package com.nexora.admin.controller;

import com.nexora.admin.biz.CourseBiz;
import com.nexora.controller.ABaseController;
import com.nexora.entity.po.CourseChapter;
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
 * 课程章节管理 Controller
 */
@RestController
@RequestMapping("/courseChapter")
public class CourseChapterController extends ABaseController {

    @Resource
    private CourseBiz courseBiz;

    @GetMapping("/loadDataList")
    public ResponseVO<List<CourseChapter>> loadDataList(@RequestParam String courseId) {
        return getSuccessResponseVO(courseBiz.chapterList(courseId));
    }

    @PostMapping("/add")
    public ResponseVO<String> add(@RequestBody CourseChapter bean) {
        return getSuccessResponseVO(courseBiz.addChapter(bean));
    }

    @PutMapping("/update")
    public ResponseVO<Void> update(@RequestBody CourseChapter bean) {
        courseBiz.updateChapter(bean);
        return getSuccessResponseVO(null);
    }

    @DeleteMapping("/del")
    public ResponseVO<Void> del(@RequestParam String chapterId) {
        courseBiz.deleteChapter(chapterId);
        return getSuccessResponseVO(null);
    }
}
