package com.nexora.admin.controller;

import com.nexora.admin.biz.PaperBiz;
import com.nexora.admin.dto.PaperSaveDTO;
import com.nexora.admin.vo.PaperDetailVO;
import com.nexora.controller.ABaseController;
import com.nexora.entity.po.PaperInfo;
import com.nexora.entity.query.PaperInfoQuery;
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
 * 试卷管理 Controller
 */
@RestController
@RequestMapping("/paperInfo")
public class PaperInfoController extends ABaseController {

    @Resource
    private PaperBiz paperBiz;

    @GetMapping("/loadDataList")
    public ResponseVO<PaginationResultVO<PaperInfo>> loadDataList(PaperInfoQuery query) {
        return getSuccessResponseVO(paperBiz.page(query));
    }

    @GetMapping("/getInfo")
    public ResponseVO<PaperDetailVO> getInfo(@RequestParam String paperId) {
        return getSuccessResponseVO(paperBiz.detail(paperId));
    }

    @PostMapping("/save")
    public ResponseVO<Void> save(@RequestBody PaperSaveDTO dto) {
        paperBiz.save(dto);
        return getSuccessResponseVO(null);
    }

    @DeleteMapping("/del")
    public ResponseVO<Void> del(@RequestParam String paperId) {
        paperBiz.delete(paperId);
        return getSuccessResponseVO(null);
    }
}
