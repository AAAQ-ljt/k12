package com.nexora.admin.controller;

import com.nexora.admin.biz.KnowledgeBaseBiz;
import com.nexora.admin.dto.KnowledgeSearchTestRequest;
import com.nexora.admin.vo.KnowledgeImportResultVO;
import com.nexora.admin.vo.KnowledgeOverviewVO;
import com.nexora.admin.vo.KnowledgeSearchResultVO;
import com.nexora.admin.vo.KnowledgeTreeNodeVO;
import com.nexora.controller.ABaseController;
import com.nexora.entity.po.KnowledgeDoc;
import com.nexora.entity.po.KnowledgePoint;
import com.nexora.entity.query.KnowledgeDocQuery;
import com.nexora.entity.vo.PaginationResultVO;
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

import java.util.List;

/**
 * 知识库管理 Controller：知识总览、知识目录、问答测试。
 */
@RestController
@RequestMapping("/knowledgeBase")
public class KnowledgeBaseController extends ABaseController {

    @Resource
    private KnowledgeBaseBiz knowledgeBaseBiz;

    @GetMapping("/overview")
    public ResponseVO<KnowledgeOverviewVO> overview() {
        return getSuccessResponseVO(knowledgeBaseBiz.overview());
    }

    @GetMapping("/tree")
    public ResponseVO<List<KnowledgeTreeNodeVO>> tree() {
        return getSuccessResponseVO(knowledgeBaseBiz.tree());
    }

    @GetMapping("/docList")
    public ResponseVO<PaginationResultVO<KnowledgeDoc>> docList(KnowledgeDocQuery query) {
        return getSuccessResponseVO(knowledgeBaseBiz.docList(query));
    }

    @PostMapping("/docAdd")
    public ResponseVO<Void> docAdd(@RequestBody KnowledgeDoc bean) {
        knowledgeBaseBiz.docAdd(bean);
        return getSuccessResponseVO(null);
    }

    @PutMapping("/docUpdate")
    public ResponseVO<Void> docUpdate(@RequestBody KnowledgeDoc bean) {
        knowledgeBaseBiz.docUpdate(bean);
        return getSuccessResponseVO(null);
    }

    @DeleteMapping("/docDel")
    public ResponseVO<Void> docDel(@RequestParam String docId) {
        knowledgeBaseBiz.docDel(docId);
        return getSuccessResponseVO(null);
    }

    @PostMapping("/pointAdd")
    public ResponseVO<Void> pointAdd(@RequestBody KnowledgePoint bean) {
        knowledgeBaseBiz.pointAdd(bean);
        return getSuccessResponseVO(null);
    }

    @PutMapping("/pointUpdate")
    public ResponseVO<Void> pointUpdate(@RequestBody KnowledgePoint bean) {
        knowledgeBaseBiz.pointUpdate(bean);
        return getSuccessResponseVO(null);
    }

    @DeleteMapping("/pointDel")
    public ResponseVO<Void> pointDel(@RequestParam String knowledgePointId) {
        knowledgeBaseBiz.pointDel(knowledgePointId);
        return getSuccessResponseVO(null);
    }

    @PostMapping("/importDir")
    public ResponseVO<KnowledgeImportResultVO> importDir() {
        return getSuccessResponseVO(knowledgeBaseBiz.importDir());
    }

    @PostMapping("/vectorize")
    public ResponseVO<Void> vectorize(@RequestParam String docId) {
        knowledgeBaseBiz.vectorize(docId);
        return getSuccessResponseVO(null);
    }

    @PostMapping("/searchTest")
    public ResponseVO<List<KnowledgeSearchResultVO>> searchTest(@RequestBody KnowledgeSearchTestRequest request) {
        return getSuccessResponseVO(knowledgeBaseBiz.searchTest(request));
    }
}
