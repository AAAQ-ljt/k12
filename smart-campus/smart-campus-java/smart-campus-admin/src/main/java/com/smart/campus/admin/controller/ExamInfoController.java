package com.smart.campus.admin.controller;

import com.smart.campus.admin.annotation.AdminPermission;
import com.smart.campus.admin.biz.ExamAdminBiz;
import com.smart.campus.admin.entity.dto.ExamSaveDTO;
import com.smart.campus.controller.ABaseController;
import com.smart.campus.entity.dto.CourseHomeworkJudgeDTO;
import com.smart.campus.entity.query.ExamInfoQuery;
import com.smart.campus.entity.query.ExamSubmitManageQuery;
import com.smart.campus.entity.vo.ResponseVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@AdminPermission("teaching:exam")
@Validated
@RestController("adminExamInfoController")
@RequestMapping("/examInfo")
public class ExamInfoController extends ABaseController {

    private final ExamAdminBiz examAdminBiz;

    public ExamInfoController(ExamAdminBiz examAdminBiz) {
        this.examAdminBiz = examAdminBiz;
    }

    @RequestMapping("/loadDataList")
    public ResponseVO loadDataList(ExamInfoQuery query) {
        return getSuccessResponseVO(examAdminBiz.loadDataList(query));
    }

    @RequestMapping("/getExamInfoById")
    public ResponseVO getExamInfoById(@NotBlank(message = "考试ID不能为空") String examId) {
        return getSuccessResponseVO(examAdminBiz.getExamInfoById(examId));
    }

    @RequestMapping("/loadExamSubmitClassList")
    public ResponseVO loadExamSubmitClassList(@NotBlank(message = "考试ID不能为空") String examId) {
        return getSuccessResponseVO(examAdminBiz.loadExamSubmitClassList(examId));
    }

    @RequestMapping("/loadExamSubmitList")
    public ResponseVO loadExamSubmitList(ExamSubmitManageQuery query) {
        return getSuccessResponseVO(examAdminBiz.loadExamSubmitList(query));
    }

    @RequestMapping("/getExamSubmitDetail")
    public ResponseVO getExamSubmitDetail(@NotBlank(message = "考试ID不能为空") String examId,
                                          @NotNull(message = "学生ID不能为空") Integer studentId) {
        return getSuccessResponseVO(examAdminBiz.getExamSubmitDetail(examId, studentId));
    }

    @RequestMapping("/judgeExamSubmit")
    public ResponseVO judgeExamSubmit(@RequestBody @Validated @Valid CourseHomeworkJudgeDTO dto) {
        examAdminBiz.judgeExamSubmit(dto);
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/add")
    public ResponseVO add(@RequestBody @Validated(ExamSaveDTO.Create.class) ExamSaveDTO dto) {
        return getSuccessResponseVO(examAdminBiz.add(dto));
    }

    @RequestMapping("/updateExamInfoById")
    public ResponseVO updateExamInfoById(@RequestBody @Validated(ExamSaveDTO.Update.class) ExamSaveDTO dto) {
        examAdminBiz.updateExamInfoById(dto);
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/publish")
    public ResponseVO publish(@NotBlank(message = "考试ID不能为空") String examId) {
        examAdminBiz.publish(examId);
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/deleteBatch")
    public ResponseVO deleteBatch(@NotBlank(message = "请选择需要删除的考试") String ids) {
        examAdminBiz.deleteBatch(ids);
        return getSuccessResponseVO(null);
    }
}
