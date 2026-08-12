package com.easymall.controller;

import com.easymall.entity.po.RagQuestion;
import com.easymall.entity.query.RagQuestionQuery;
import com.easymall.entity.vo.PaginationResultVO;
import com.easymall.entity.vo.ResponseVO;
import com.easymall.service.RagQuestionService;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rag")
@Validated
public class RagQuestionController extends ABaseController {

    @Resource
    private RagQuestionService ragQuestionService;

    @RequestMapping("/loadRagQuestion")
    public ResponseVO loadRagQuestion(RagQuestionQuery query) {
        query.setOrderBy("r.question_id desc");
        PaginationResultVO resultVO = ragQuestionService.findListByPage(query);
        return getSuccessResponseVO(resultVO);
    }


    @RequestMapping("/saveRagQuestion")
    public ResponseVO saveRagQuestion(RagQuestion ragQuestion) {
        ragQuestionService.saveRagQuestion(ragQuestion);
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/delRagQuestion")
    public ResponseVO delRagQuestion(Integer questionId) {
        ragQuestionService.deleteRagQuestionByQuestionId(questionId);
        return getSuccessResponseVO(null);
    }
}
