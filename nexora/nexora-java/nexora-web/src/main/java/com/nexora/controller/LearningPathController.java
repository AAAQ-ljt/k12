package com.nexora.controller;

import com.nexora.annotation.GlobalInterceptor;
import com.nexora.entity.dto.TokenUserInfoDTO;
import com.nexora.entity.po.AiGenerationRecord;
import com.nexora.entity.vo.ResponseVO;
import com.nexora.exception.BusinessException;
import com.nexora.service.StudentLearningPathService;
import com.nexora.utils.LoginUserContext;
import com.nexora.utils.StringTools;
import com.nexora.vo.LearningPathSummaryVO;
import com.nexora.vo.LearningPathVO;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 学生个性化学习路径 Controller：AI 生成（节点化）/ 我的路径 / 详情 / 删除 + 历史计划
 */
@RestController
@RequestMapping("/learningPath")
@GlobalInterceptor(checkLogin = true)
public class LearningPathController extends ABaseController {

    @Resource
    private StudentLearningPathService studentLearningPathService;

    /** 生成节点化学习路径（主线 + 兴趣分支），返回新建路径（含节点） */
    @PostMapping("/generate")
    public ResponseVO<LearningPathVO> generate() {
        TokenUserInfoDTO current = LoginUserContext.get();
        return getSuccessResponseVO(studentLearningPathService.generate(current.getUserId(), current.getStage()));
    }

    /** 我的路线库（卡片列表：不含节点明细，节点状态按掌握度实时刷新） */
    @GetMapping("/myList")
    public ResponseVO<List<LearningPathSummaryVO>> myList() {
        return getSuccessResponseVO(studentLearningPathService.myList(currentUserId()));
    }

    /** 单条路径详情 */
    @GetMapping("/detail")
    public ResponseVO<LearningPathVO> detail(@RequestParam String pathId) {
        return getSuccessResponseVO(studentLearningPathService.detail(currentUserId(), pathId));
    }

    /** 删除路径（级联节点） */
    @DeleteMapping("/del")
    public ResponseVO<Void> del(@RequestParam String pathId) {
        studentLearningPathService.delete(currentUserId(), pathId);
        return getSuccessResponseVO(null);
    }

    /** 历史计划（旧版基于 ai_generation_record 的生成记录） */
    @GetMapping("/historyList")
    public ResponseVO<List<AiGenerationRecord>> historyList() {
        return getSuccessResponseVO(studentLearningPathService.historyList(currentUserId()));
    }

    /** 删除历史计划记录 */
    @DeleteMapping("/historyDel")
    public ResponseVO<Void> historyDel(@RequestParam String recordId) {
        studentLearningPathService.deleteHistory(currentUserId(), recordId);
        return getSuccessResponseVO(null);
    }

    private String currentUserId() {
        TokenUserInfoDTO current = LoginUserContext.get();
        if (current == null || StringTools.isEmpty(current.getUserId())) {
            throw new BusinessException("登录状态异常");
        }
        return current.getUserId();
    }
}
