package com.nexora.controller;

import com.nexora.annotation.GlobalInterceptor;
import com.nexora.component.PointAwardComponent;
import com.nexora.dto.CodingJudgeDTO;
import com.nexora.entity.dto.TokenUserInfoDTO;
import com.nexora.entity.vo.CodingJudgeResultVO;
import com.nexora.entity.vo.CodingProblemReferenceVO;
import com.nexora.entity.vo.CodingProblemVO;
import com.nexora.entity.vo.ResponseVO;
import com.nexora.service.CodingLabBiz;
import com.nexora.utils.LoginUserContext;
import com.nexora.utils.StringTools;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 学生端编程题库（题目列表/详情/参考答案）。
 *
 * 参考答案单独走 `/reference`：比赛进行中默认拒绝，练习模式允许但由前端按 30% 计分。
 */
@RestController
@RequestMapping("/codingProblem")
@GlobalInterceptor(checkLogin = true)
public class StudentCodingProblemController extends ABaseController {

    @Resource
    private CodingLabBiz codingLabBiz;

    /** 记录「看过答案」事实供判分折算（30%）——事实记录，规则在 PointAwardComponent 内 */
    @Resource
    private PointAwardComponent pointAwardComponent;

    @GetMapping("/list")
    public ResponseVO<List<CodingProblemVO>> list(@RequestParam(required = false) Integer difficulty,
                                                  @RequestParam(required = false) String keyword) {
        TokenUserInfoDTO current = LoginUserContext.get();
        String stage = current == null ? null : current.getStage();
        return getSuccessResponseVO(codingLabBiz.listProblems(stage, difficulty, keyword));
    }

    @GetMapping("/getInfo")
    public ResponseVO<CodingProblemVO> getInfo(@RequestParam String problemId) {
        TokenUserInfoDTO current = LoginUserContext.get();
        return getSuccessResponseVO(codingLabBiz.getProblem(problemId, current == null ? null : current.getStage()));
    }

    /**
     * 判分：上报运行输出，由服务端比对（预期值不下发前端）。
     *
     * 学段取登录态（不信任客户端传参），与题目详情同口径：跨学段判分直接拒绝；
     * 比赛模式下额外校验「该题属于这场比赛」。
     */
    @PostMapping("/judge")
    public ResponseVO<CodingJudgeResultVO> judge(@RequestBody CodingJudgeDTO dto) {
        if (dto == null || StringTools.isEmpty(dto.getProblemId())) {
            throw new com.nexora.exception.BusinessException("题目ID不能为空");
        }
        TokenUserInfoDTO current = LoginUserContext.get();
        String stage = current == null ? null : current.getStage();
        String userId = current == null ? null : current.getUserId();
        Object[] result = codingLabBiz.judge(dto.getProblemId(), dto.getOutput(), stage, dto.getContestId(), userId);
        return getSuccessResponseVO(new CodingJudgeResultVO((Boolean) result[0], String.valueOf(result[1])));
    }

    /** 参考答案（「显示答案」）：已下架或非本学段的题目一律拒绝 */
    @GetMapping("/reference")
    public ResponseVO<CodingProblemReferenceVO> reference(@RequestParam String problemId,
                                                          @RequestParam(required = false) String contestId) {
        TokenUserInfoDTO current = LoginUserContext.get();
        String userId = current == null ? null : current.getUserId();
        if (StringTools.isEmpty(userId)) {
            return getSuccessResponseVO(new CodingProblemReferenceVO());
        }
        String stage = current.getStage();
        CodingProblemReferenceVO vo = codingLabBiz.getReference(problemId, contestId, userId, stage);
        // 真的拿到了参考答案 → 记一次「看过答案」：判分时按 30% 计分（服务端标记，前端不可绕过）
        if (vo != null && !StringTools.isEmpty(vo.getReferenceCode())) {
            pointAwardComponent.markCodingAnswerUsed(userId, problemId);
        }
        return getSuccessResponseVO(vo);
    }
}
