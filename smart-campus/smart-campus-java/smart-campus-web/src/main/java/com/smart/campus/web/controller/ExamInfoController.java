package com.smart.campus.web.controller;

import com.smart.campus.controller.ABaseController;
import com.smart.campus.web.biz.CourseExamWebBiz;
import com.smart.campus.entity.vo.ResponseVO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController("webExamInfoController")
@RequestMapping("/examInfo")
public class ExamInfoController extends ABaseController {

    private final CourseExamWebBiz courseExamWebBiz;

    public ExamInfoController(CourseExamWebBiz courseExamWebBiz) {
        this.courseExamWebBiz = courseExamWebBiz;
    }

    @RequestMapping("/loadMyExamList")
    public ResponseVO loadMyExamList() {
        return getSuccessResponseVO(courseExamWebBiz.loadMyExamList());
    }
}
