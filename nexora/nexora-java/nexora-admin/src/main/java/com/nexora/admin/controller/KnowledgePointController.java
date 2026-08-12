package com.nexora.admin.controller;

import com.nexora.controller.ABaseController;
import com.nexora.entity.po.KnowledgePoint;
import com.nexora.entity.query.KnowledgePointQuery;
import com.nexora.entity.vo.ResponseVO;
import com.nexora.exception.BusinessException;
import com.nexora.service.KnowledgePointService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 知识点管理 Controller
 */
@RestController("knowledgePointController")
@RequestMapping("/knowledgePoint")
public class KnowledgePointController extends ABaseController {

    private final KnowledgePointService knowledgePointService;

    public KnowledgePointController(KnowledgePointService knowledgePointService) {
        this.knowledgePointService = knowledgePointService;
    }

    /**
     * 分页查询知识点列表
     */
    @GetMapping("/loadDataList")
    public ResponseVO loadDataList(KnowledgePointQuery query) {
        return getSuccessResponseVO(knowledgePointService.findListByPage(query));
    }

    /**
     * 获取知识点详情
     */
    @GetMapping("/getInfo")
    public ResponseVO getInfo(@RequestParam String knowledgePointId) {
        KnowledgePoint knowledgePoint = knowledgePointService.getKnowledgePointByKnowledgePointId(knowledgePointId);
        if (knowledgePoint == null) {
            throw new BusinessException("知识点不存在");
        }
        return getSuccessResponseVO(knowledgePoint);
    }

    /**
     * 新增知识点
     */
    @PostMapping("/add")
    public ResponseVO add(@RequestBody KnowledgePoint knowledgePoint) {
        knowledgePointService.add(knowledgePoint);
        return getSuccessResponseVO(null);
    }

    /**
     * 编辑知识点
     */
    @PutMapping("/update")
    public ResponseVO update(@RequestBody KnowledgePoint knowledgePoint) {
        if (knowledgePoint.getKnowledgePointId() == null || knowledgePoint.getKnowledgePointId().isEmpty()) {
            throw new BusinessException("知识点ID不能为空");
        }
        knowledgePointService.updateKnowledgePointByKnowledgePointId(knowledgePoint, knowledgePoint.getKnowledgePointId());
        return getSuccessResponseVO(null);
    }

    /**
     * 删除知识点
     */
    @DeleteMapping("/del")
    public ResponseVO del(@RequestParam String knowledgePointId) {
        knowledgePointService.deleteKnowledgePointByKnowledgePointId(knowledgePointId);
        return getSuccessResponseVO(null);
    }
}
