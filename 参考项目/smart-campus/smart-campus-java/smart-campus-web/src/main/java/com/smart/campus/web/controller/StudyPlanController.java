package com.smart.campus.web.controller;

import com.smart.campus.controller.ABaseController;
import com.smart.campus.entity.vo.ResponseVO;
import com.smart.campus.web.biz.StudyPlanWebBiz;
import com.smart.campus.web.entity.dto.studyplan.StudyPlanItemStatusDTO;
import com.smart.campus.web.entity.dto.studyplan.StudyPlanSaveDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController("webStudyPlanController")
@RequestMapping("/studyPlan")
public class StudyPlanController extends ABaseController {

    private final StudyPlanWebBiz studyPlanWebBiz;

    public StudyPlanController(StudyPlanWebBiz studyPlanWebBiz) {
        this.studyPlanWebBiz = studyPlanWebBiz;
    }

    @RequestMapping("/loadDashboard")
    public ResponseVO loadDashboard() {
        return getSuccessResponseVO(studyPlanWebBiz.loadDashboard());
    }

    @RequestMapping("/getDetail")
    public ResponseVO getDetail(@NotBlank(message = "学习计划ID不能为空") String planId) {
        return getSuccessResponseVO(studyPlanWebBiz.getDetail(planId));
    }

    @RequestMapping("/save")
    public ResponseVO save(@Valid StudyPlanSaveDTO dto) {
        return getSuccessResponseVO(studyPlanWebBiz.savePlan(dto));
    }

    @RequestMapping("/getPlannedLessonIds")
    public ResponseVO getPlannedLessonIds(String courseId, String excludePlanId) {
        return getSuccessResponseVO(studyPlanWebBiz.getPlannedLessonIds(courseId, excludePlanId));
    }

    @RequestMapping("/updateItemStatus")
    public ResponseVO updateItemStatus(@Valid StudyPlanItemStatusDTO dto) {
        return getSuccessResponseVO(studyPlanWebBiz.updateItemStatus(dto));
    }
}
