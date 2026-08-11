package com.smart.campus.web.controller;

import com.smart.campus.controller.ABaseController;
import com.smart.campus.web.biz.CourseExamWebBiz;
import com.smart.campus.entity.dto.CourseExamAnswerSaveDTO;
import com.smart.campus.entity.dto.CourseExamDraftSaveDTO;
import com.smart.campus.entity.dto.CourseExamStartDTO;
import com.smart.campus.entity.dto.CourseExamSubmitDTO;
import com.smart.campus.entity.vo.ResponseVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController("webCourseExamController")
@RequestMapping("/courseExam")
public class CourseExamController extends ABaseController {

    private final CourseExamWebBiz courseExamWebBiz;

    public CourseExamController(CourseExamWebBiz courseExamWebBiz) {
        this.courseExamWebBiz = courseExamWebBiz;
    }

    @RequestMapping("/getDetail")
    public ResponseVO getDetail(@NotBlank(message = "考试ID不能为空") String examId) {
        return getSuccessResponseVO(courseExamWebBiz.getExamDetail(examId));
    }

    @RequestMapping("/start")
    public ResponseVO start(@Valid CourseExamStartDTO dto) {
        return getSuccessResponseVO(courseExamWebBiz.startExam(dto));
    }

    @RequestMapping("/saveAnswer")
    public ResponseVO saveAnswer(@Valid CourseExamAnswerSaveDTO dto) {
        return getSuccessResponseVO(courseExamWebBiz.saveAnswer(dto));
    }

    @RequestMapping("/saveDraft")
    public ResponseVO saveDraft(@Valid CourseExamDraftSaveDTO dto) {
        return getSuccessResponseVO(courseExamWebBiz.saveDraft(dto));
    }

    @RequestMapping("/submit")
    public ResponseVO submit(@Valid CourseExamSubmitDTO dto) {
        return getSuccessResponseVO(courseExamWebBiz.submitExam(dto));
    }
}
