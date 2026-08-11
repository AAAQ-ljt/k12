package com.smart.campus.web.controller;

import com.smart.campus.controller.ABaseController;
import com.smart.campus.entity.dto.CourseStudyProgressReportDTO;
import com.smart.campus.entity.vo.ResponseVO;
import com.smart.campus.web.biz.CourseWebBiz;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("webCourseInfoController")
@RequestMapping("/courseInfo")
public class CourseInfoController extends ABaseController {

    private final CourseWebBiz courseWebBiz;

    public CourseInfoController(CourseWebBiz courseWebBiz) {
        this.courseWebBiz = courseWebBiz;
    }

    @RequestMapping("/loadMyCourseList")
    public ResponseVO loadMyCourseList() {
        return getSuccessResponseVO(courseWebBiz.loadMyCourseList());
    }

    @RequestMapping("/getMyCourseDetail")
    public ResponseVO getMyCourseDetail(String courseId) {
        return getSuccessResponseVO(courseWebBiz.getMyCourseDetail(courseId));
    }

    @RequestMapping("/reportStudyProgress")
    public ResponseVO reportStudyProgress(@Valid CourseStudyProgressReportDTO dto) {
        return getSuccessResponseVO(courseWebBiz.reportStudyProgress(dto));
    }

    @RequestMapping("/saveCollection")
    public ResponseVO saveCollection(String courseId, Integer collectFlag) {
        return getSuccessResponseVO(courseWebBiz.saveCourseCollection(courseId, collectFlag));
    }
}
