package com.nexora.admin.controller;

import com.nexora.admin.biz.CourseBiz;
import com.nexora.controller.ABaseController;
import com.nexora.entity.po.CourseInfo;
import com.nexora.entity.query.CourseInfoQuery;
import com.nexora.entity.vo.CourseDetailVO;
import com.nexora.entity.vo.PaginationResultVO;
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

/**
 * 课程体系管理 Controller
 */
@RestController
@RequestMapping("/courseInfo")
public class CourseInfoController extends ABaseController {

    @Resource
    private CourseBiz courseBiz;

    @GetMapping("/loadDataList")
    public ResponseVO<PaginationResultVO<CourseInfo>> loadDataList(CourseInfoQuery query) {
        return getSuccessResponseVO(courseBiz.coursePage(query));
    }

    @GetMapping("/getInfo")
    public ResponseVO<CourseInfo> getInfo(@RequestParam String courseId) {
        return getSuccessResponseVO(courseBiz.getCourse(courseId));
    }

    @GetMapping("/getDetail")
    public ResponseVO<CourseDetailVO> getDetail(@RequestParam String courseId) {
        return getSuccessResponseVO(courseBiz.courseDetail(courseId));
    }

    @PostMapping("/add")
    public ResponseVO<String> add(@RequestBody CourseInfo bean) {
        return getSuccessResponseVO(courseBiz.addCourse(bean));
    }

    @PutMapping("/update")
    public ResponseVO<Void> update(@RequestBody CourseInfo bean) {
        courseBiz.updateCourse(bean);
        return getSuccessResponseVO(null);
    }

    @DeleteMapping("/del")
    public ResponseVO<Void> del(@RequestParam String courseId) {
        courseBiz.deleteCourse(courseId);
        return getSuccessResponseVO(null);
    }
}
