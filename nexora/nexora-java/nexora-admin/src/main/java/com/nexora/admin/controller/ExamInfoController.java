package com.nexora.admin.controller;

import com.nexora.admin.biz.ExamBiz;
import com.nexora.controller.ABaseController;
import com.nexora.entity.po.ExamInfo;
import com.nexora.entity.query.ExamInfoQuery;
import com.nexora.entity.vo.ExamInfoVO;
import com.nexora.entity.vo.PaginationResultVO;
import com.nexora.entity.vo.ResponseVO;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 考试管理 Controller
 */
@RestController
@RequestMapping("/examInfo")
public class ExamInfoController extends ABaseController {

    @Resource
    private ExamBiz examBiz;

    @GetMapping("/loadDataList")
    public ResponseVO<PaginationResultVO<ExamInfoVO>> loadDataList(ExamInfoQuery query) {
        return getSuccessResponseVO(examBiz.page(query));
    }

    @GetMapping("/getInfo")
    public ResponseVO<ExamInfoVO> getInfo(@RequestParam String examId) {
        return getSuccessResponseVO(examBiz.detail(examId));
    }

    @PostMapping("/save")
    public ResponseVO<Void> save(@RequestBody ExamInfo bean) {
        examBiz.save(bean);
        return getSuccessResponseVO(null);
    }

    @DeleteMapping("/del")
    public ResponseVO<Void> del(@RequestParam String examId) {
        examBiz.delete(examId);
        return getSuccessResponseVO(null);
    }
}
