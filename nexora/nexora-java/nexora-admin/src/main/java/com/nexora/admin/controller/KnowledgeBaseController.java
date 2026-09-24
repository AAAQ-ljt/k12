package com.nexora.admin.controller;

import com.nexora.admin.biz.AiOrganizeTaskBiz;
import com.nexora.admin.biz.KnowledgeBaseBiz;
import com.nexora.admin.biz.KnowledgeImportTaskBiz;
import com.nexora.admin.dto.AiOrganizeTaskVO;
import com.nexora.admin.dto.KnowledgeImportTaskVO;
import com.nexora.admin.dto.KnowledgeSearchTestRequest;
import com.nexora.admin.dto.ResourceKnowledgeImportRequest;
import com.nexora.admin.vo.KnowledgeAIDocVO;
import com.nexora.admin.vo.KnowledgeImportResultVO;
import com.nexora.admin.vo.KnowledgeOverviewVO;
import com.nexora.admin.vo.KnowledgeSearchResultVO;
import com.nexora.admin.vo.ResourceKnowledgeImportResultVO;
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

    @Resource
    private KnowledgeImportTaskBiz knowledgeImportTaskBiz;

    @Resource
    private AiOrganizeTaskBiz aiOrganizeTaskBiz;

    /** 解析入库任务状态查询（前端轮询：PENDING/EXTRACTING/VECTORIZING/COMPLETED/FAILED + 进度） */
    @GetMapping("/importTask")
    public ResponseVO<KnowledgeImportTaskVO> importTask(@RequestParam String taskId) {
        return getSuccessResponseVO(knowledgeImportTaskBiz.get(taskId));
    }

    /**
     * 提交 AI 文档整理任务（异步任务状态机，前端轮询 /aiOrganizeTask 获取进度）；
     * 运行中同一资源重复提交直接返回进行中任务，防止重复发起大模型整理
     */
    @PostMapping("/aiOrganizeTask")
    public ResponseVO<AiOrganizeTaskVO> aiOrganizeTask(@RequestParam String resourceId) {
        return getSuccessResponseVO(aiOrganizeTaskBiz.submit(resourceId));
    }

    /** AI 文档整理任务状态查询（前端轮询：PENDING/ORGANIZING/COMPLETED/FAILED；完成带整理稿） */
    @GetMapping("/aiOrganizeTask")
    public ResponseVO<AiOrganizeTaskVO> aiOrganizeTaskStatus(@RequestParam String taskId) {
        return getSuccessResponseVO(aiOrganizeTaskBiz.get(taskId));
    }

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

    @PostMapping("/resourceImport")
    public ResponseVO<ResourceKnowledgeImportResultVO> resourceImport(
            @RequestBody ResourceKnowledgeImportRequest request) {
        return getSuccessResponseVO(knowledgeBaseBiz.resourceImport(request));
    }

    @PostMapping("/aiOrganize")
    public ResponseVO<KnowledgeAIDocVO> aiOrganize(@RequestParam String resourceId) {
        return getSuccessResponseVO(knowledgeBaseBiz.aiOrganize(resourceId));
    }

    @PostMapping("/vectorize")
    public ResponseVO<Void> vectorize(@RequestParam String docId) {
        knowledgeBaseBiz.submitVectorize(docId);
        return getSuccessResponseVO(null);
    }

    @PostMapping("/searchTest")
    public ResponseVO<List<KnowledgeSearchResultVO>> searchTest(@RequestBody KnowledgeSearchTestRequest request) {
        return getSuccessResponseVO(knowledgeBaseBiz.searchTest(request));
    }
}
