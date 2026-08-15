package com.nexora.admin.controller;

import com.nexora.admin.biz.QuestionBiz;
import com.nexora.admin.dto.QuestionSaveDTO;
import com.nexora.controller.ABaseController;
import com.nexora.entity.po.QuestionInfo;
import com.nexora.entity.query.QuestionInfoQuery;
import com.nexora.entity.vo.PaginationResultVO;
import com.nexora.entity.vo.QuestionDetailVO;
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
 * 习题管理 Controller
 */
@RestController
@RequestMapping("/questionInfo")
public class QuestionInfoController extends ABaseController {

    @Resource
    private QuestionBiz questionBiz;

    @GetMapping("/loadDataList")
    public ResponseVO<PaginationResultVO<QuestionInfo>> loadDataList(QuestionInfoQuery query) {
        return getSuccessResponseVO(questionBiz.questionPage(query));
    }

    @GetMapping("/getInfo")
    public ResponseVO<QuestionDetailVO> getInfo(@RequestParam String questionId) {
        return getSuccessResponseVO(questionBiz.questionDetail(questionId));
    }

    @PostMapping("/add")
    public ResponseVO<String> add(@RequestBody QuestionSaveDTO dto) {
        return getSuccessResponseVO(questionBiz.addQuestion(dto));
    }

    @PutMapping("/update")
    public ResponseVO<Void> update(@RequestBody QuestionSaveDTO dto) {
        questionBiz.updateQuestion(dto);
        return getSuccessResponseVO(null);
    }

    @DeleteMapping("/del")
    public ResponseVO<Void> del(@RequestParam String questionId) {
        questionBiz.deleteQuestion(questionId);
        return getSuccessResponseVO(null);
    }

    @PutMapping("/audit")
    public ResponseVO<Void> audit(@RequestParam String questionId, @RequestParam Integer auditStatus) {
        questionBiz.auditQuestion(questionId, auditStatus);
        return getSuccessResponseVO(null);
    }
}
