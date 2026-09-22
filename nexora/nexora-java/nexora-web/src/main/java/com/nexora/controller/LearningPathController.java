package com.nexora.controller;

import com.nexora.annotation.GlobalInterceptor;
import com.nexora.dto.LearningPathGenTaskVO;
import com.nexora.dto.NodeQuizSubmitDTO;
import com.nexora.dto.NodeQuizTaskVO;
import com.nexora.dto.NodeQuizSubmitBizRequest;
import com.nexora.entity.dto.TokenUserInfoDTO;
import com.nexora.entity.po.AiGenerationRecord;
import com.nexora.entity.vo.ResponseVO;
import com.nexora.exception.BusinessException;
import com.nexora.service.LearningPathGenTaskService;
import com.nexora.service.NodeQuizTaskService;
import com.nexora.service.StudentLearningPathService;
import com.nexora.utils.LoginUserContext;
import com.nexora.utils.StringTools;
import com.nexora.vo.LearningPathSummaryVO;
import com.nexora.vo.LearningPathVO;
import com.nexora.vo.NodeQuizSubmitResultVO;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 学生个性化学习路径 Controller：
 * AI 生成路线（异步任务 + Redis 状态机 + 前端轮询）/ 我的路径 / 详情 / 删除 + 历史计划 + 节点快测
 */
@RestController
@RequestMapping("/learningPath")
@GlobalInterceptor(checkLogin = true)
public class LearningPathController extends ABaseController {

    @Resource
    private StudentLearningPathService studentLearningPathService;

    @Resource
    private LearningPathGenTaskService learningPathGenTaskService;

    @Resource
    private NodeQuizTaskService nodeQuizTaskService;

    /**
     * 提交 AI 生成学习路径任务（主线 + 兴趣分支）：立即返回任务体，前端轮询 /genTask 获取进度；
     * 运行中同一用户重复提交直接返回原任务，防止连续点击重复生成
     */
    @PostMapping("/generate")
    public ResponseVO<LearningPathGenTaskVO> generate() {
        TokenUserInfoDTO current = LoginUserContext.get();
        return getSuccessResponseVO(
                learningPathGenTaskService.submit(current.getUserId(), current.getStage()));
    }

    /** 查询学习路径生成任务状态（前端轮询） */
    @GetMapping("/genTask")
    public ResponseVO<LearningPathGenTaskVO> genTask(@RequestParam String taskId) {
        return getSuccessResponseVO(learningPathGenTaskService.get(currentUserId(), taskId));
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

    /**
     * 提交节点快测出题任务（节点自己出题自测，不依赖课程题库）：立即返回任务体，
     * 前端轮询 /nodeQuizTask 获取进度；生成期间节点快测按钮禁用，后端按用户互斥防止重复出题
     */
    @PostMapping("/genNodeQuiz")
    public ResponseVO<NodeQuizTaskVO> genNodeQuiz(@RequestBody NodeQuizSubmitBizRequest request) {
        String itemId = request == null ? null : request.getItemId();
        return getSuccessResponseVO(nodeQuizTaskService.submit(currentUserId(), itemId));
    }

    /** 查询节点快测出题任务状态（前端轮询） */
    @GetMapping("/nodeQuizTask")
    public ResponseVO<NodeQuizTaskVO> nodeQuizTask(@RequestParam String taskId) {
        return getSuccessResponseVO(nodeQuizTaskService.get(currentUserId(), taskId));
    }

    /** 节点快测提交判分（服务端权威判分 → 掌握度回写 → 节点解锁联动） */
    @PostMapping("/submitNodeQuiz")
    public ResponseVO<NodeQuizSubmitResultVO> submitNodeQuiz(@RequestBody NodeQuizSubmitDTO dto) {
        return getSuccessResponseVO(studentLearningPathService.submitNodeQuiz(currentUserId(), dto));
    }

    private String currentUserId() {
        TokenUserInfoDTO current = LoginUserContext.get();
        if (current == null || StringTools.isEmpty(current.getUserId())) {
            throw new BusinessException("登录状态异常");
        }
        return current.getUserId();
    }
}