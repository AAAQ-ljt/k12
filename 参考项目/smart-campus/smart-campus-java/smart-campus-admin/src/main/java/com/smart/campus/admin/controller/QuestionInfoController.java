package com.smart.campus.admin.controller;

import com.smart.campus.admin.annotation.AdminPermission;
import com.smart.campus.admin.biz.QuestionAdminBiz;
import com.smart.campus.admin.entity.dto.QuestionSaveDTO;
import com.smart.campus.controller.ABaseController;
import com.smart.campus.entity.query.QuestionInfoQuery;
import com.smart.campus.entity.vo.ResponseVO;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@AdminPermission("teaching:exercise")
@Validated
@RestController("questionInfoController")
@RequestMapping("/questionInfo")
public class QuestionInfoController extends ABaseController {

    private final QuestionAdminBiz questionAdminBiz;

    public QuestionInfoController(QuestionAdminBiz questionAdminBiz) {
        this.questionAdminBiz = questionAdminBiz;
    }

    @RequestMapping("/loadDataList")
    public ResponseVO loadDataList(QuestionInfoQuery query) {
        return getSuccessResponseVO(questionAdminBiz.loadDataList(query));
    }

    @RequestMapping("/getQuestionInfoById")
    public ResponseVO getQuestionInfoById(@NotNull(message = "题目ID不能为空") Integer questionId) {
        return getSuccessResponseVO(questionAdminBiz.getQuestionInfoById(questionId));
    }

    @RequestMapping("/add")
    public ResponseVO add(@RequestBody @Validated(QuestionSaveDTO.Create.class) QuestionSaveDTO dto) {
        return getSuccessResponseVO(questionAdminBiz.add(dto));
    }

    @RequestMapping("/updateQuestionInfoById")
    public ResponseVO updateQuestionInfoById(@RequestBody @Validated(QuestionSaveDTO.Update.class) QuestionSaveDTO dto) {
        questionAdminBiz.updateQuestionInfoById(dto);
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/deleteQuestionInfoById")
    public ResponseVO deleteQuestionInfoById(@NotNull(message = "题目ID不能为空") Integer questionId) {
        questionAdminBiz.deleteQuestionInfoById(questionId);
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/deleteBatch")
    public ResponseVO deleteBatch(String ids) {
        questionAdminBiz.deleteBatch(ids);
        return getSuccessResponseVO(null);
    }
}
