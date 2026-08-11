package com.smart.campus.web.controller;

import com.smart.campus.controller.ABaseController;
import com.smart.campus.web.biz.CourseHomeworkWebBiz;
import com.smart.campus.entity.dto.CourseHomeworkAnswerSaveDTO;
import com.smart.campus.entity.dto.CourseHomeworkDraftSaveDTO;
import com.smart.campus.entity.dto.CourseHomeworkStartDTO;
import com.smart.campus.entity.dto.CourseHomeworkSubmitDTO;
import com.smart.campus.entity.vo.ResponseVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController("webCourseHomeworkController")
@RequestMapping("/courseHomework")
public class CourseHomeworkController extends ABaseController {

    private final CourseHomeworkWebBiz courseHomeworkWebBiz;

    public CourseHomeworkController(CourseHomeworkWebBiz courseHomeworkWebBiz) {
        this.courseHomeworkWebBiz = courseHomeworkWebBiz;
    }

    @RequestMapping("/getDetail")
    public ResponseVO getDetail(@NotBlank(message = "课程ID不能为空") String courseId,
                                @NotBlank(message = "课时ID不能为空") String lessonId) {
        return getSuccessResponseVO(courseHomeworkWebBiz.getHomeworkDetail(courseId, lessonId));
    }

    @RequestMapping("/start")
    public ResponseVO start(@Valid CourseHomeworkStartDTO dto) {
        return getSuccessResponseVO(courseHomeworkWebBiz.startHomework(dto));
    }

    @RequestMapping("/saveAnswer")
    public ResponseVO saveAnswer(@Valid CourseHomeworkAnswerSaveDTO dto) {
        return getSuccessResponseVO(courseHomeworkWebBiz.saveAnswer(dto));
    }

    @RequestMapping("/saveDraft")
    public ResponseVO saveDraft(@Valid CourseHomeworkDraftSaveDTO dto) {
        return getSuccessResponseVO(courseHomeworkWebBiz.saveDraft(dto));
    }

    @RequestMapping("/submit")
    public ResponseVO submit(@Valid CourseHomeworkSubmitDTO dto) {
        return getSuccessResponseVO(courseHomeworkWebBiz.submitHomework(dto));
    }
}
