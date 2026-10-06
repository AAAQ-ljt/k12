package com.nexora.admin.controller;

import com.nexora.constants.Constants;
import com.nexora.controller.ABaseController;
import com.nexora.entity.po.CodingProblem;
import com.nexora.entity.query.CodingProblemQuery;
import com.nexora.entity.vo.PaginationResultVO;
import com.nexora.entity.vo.ResponseVO;
import com.nexora.exception.BusinessException;
import com.nexora.service.CodingProblemService;
import com.nexora.utils.StringTools;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * 管理端「编程题库」题目管理
 */
@RestController
@RequestMapping("/codingProblem")
public class CodingProblemController extends ABaseController {

    /** 难度 → 建议积分（入门/基础/进阶/挑战） */
    private static final Map<Integer, Integer> DIFFICULTY_SCORE = Map.of(1, 10, 2, 20, 3, 30, 4, 50);

    @Resource
    private CodingProblemService codingProblemService;

    @GetMapping("/loadDataList")
    public ResponseVO<PaginationResultVO<CodingProblem>> loadDataList(CodingProblemQuery query) {
        if (query.getPageNo() == null) {
            query.setPageNo(1);
        }
        if (query.getPageSize() == null) {
            query.setPageSize(15);
        }
        return getSuccessResponseVO(codingProblemService.findListByPage(query));
    }

    @GetMapping("/getInfo")
    public ResponseVO<CodingProblem> getInfo(@RequestParam String problemId) {
        CodingProblem problem = codingProblemService.getCodingProblemByProblemId(problemId);
        if (problem == null) {
            throw new BusinessException("题目不存在");
        }
        return getSuccessResponseVO(problem);
    }

    @PostMapping("/add")
    public ResponseVO<String> add(@RequestBody CodingProblem bean) {
        validate(bean);
        bean.setProblemId(StringTools.getRandomNumber(Constants.LENGTH_15));
        if (StringTools.isEmpty(bean.getLanguage())) {
            bean.setLanguage("python");
        }
        if (bean.getScore() == null) {
            bean.setScore(DIFFICULTY_SCORE.getOrDefault(bean.getDifficulty(), 10));
        }
        if (bean.getEstimateMinutes() == null) {
            bean.setEstimateMinutes(15);
        }
        if (bean.getSort() == null) {
            bean.setSort(0);
        }
        if (bean.getStatus() == null) {
            bean.setStatus(1);
        }
        if (bean.getJudgeType() == null) {
            bean.setJudgeType(1);
        }
        bean.setCreateTime(new Date());
        bean.setUpdateTime(new Date());
        codingProblemService.add(bean);
        return getSuccessResponseVO(bean.getProblemId());
    }

    @PutMapping("/update")
    public ResponseVO<Void> update(@RequestBody CodingProblem bean) {
        if (StringTools.isEmpty(bean.getProblemId())) {
            throw new BusinessException("题目ID不能为空");
        }
        validate(bean);
        bean.setUpdateTime(new Date());
        codingProblemService.updateCodingProblemByProblemId(bean, bean.getProblemId());
        return getSuccessResponseVO(null);
    }

    @PutMapping("/changeStatus")
    public ResponseVO<Void> changeStatus(@RequestParam String problemId, @RequestParam Integer status) {
        if (StringTools.isEmpty(problemId) || status == null) {
            throw new BusinessException("参数不完整");
        }
        CodingProblem update = new CodingProblem();
        update.setStatus(status == 1 ? 1 : 0);
        update.setUpdateTime(new Date());
        codingProblemService.updateCodingProblemByProblemId(update, problemId);
        return getSuccessResponseVO(null);
    }

    @DeleteMapping("/del")
    public ResponseVO<Void> del(@RequestParam String problemId) {
        if (StringTools.isEmpty(problemId)) {
            throw new BusinessException("题目ID不能为空");
        }
        codingProblemService.deleteCodingProblemByProblemId(problemId);
        return getSuccessResponseVO(null);
    }

    private void validate(CodingProblem bean) {
        if (StringTools.isEmpty(bean.getTitle())) {
            throw new BusinessException("题目标题不能为空");
        }
        if (StringTools.isEmpty(bean.getStage())) {
            throw new BusinessException("请选择学段");
        }
        if (bean.getDifficulty() == null || bean.getDifficulty() < 1 || bean.getDifficulty() > 4) {
            throw new BusinessException("难度必须为 1-4（入门/基础/进阶/挑战）");
        }
        if (bean.getJudgeType() != null && (bean.getJudgeType() < 1 || bean.getJudgeType() > 3)) {
            throw new BusinessException("判定方式必须为 1-3");
        }
        if (StringTools.isEmpty(bean.getReferenceCode())) {
            throw new BusinessException("参考答案不能为空（学生点「显示答案」时下发）");
        }
        if (bean.getJudgeType() != null && bean.getJudgeType() == 1
                && StringTools.isEmpty(bean.getExpectedKeywords())) {
            throw new BusinessException("按关键词判定时必须填写期望关键词");
        }
        if (bean.getJudgeType() != null && bean.getJudgeType() == 2) {
            if (StringTools.isEmpty(bean.getExpectedOutput())) {
                throw new BusinessException("按输出精确匹配判定时必须填写期望输出");
            }
            // 输出契约：精确匹配类题目必须写清「输出要求 + 输出示例」，否则学生只能靠猜格式（见 docs/二期规划设计 §5.1.5-B）
            if (StringTools.isEmpty(bean.getOutputSpec())) {
                throw new BusinessException("按输出精确匹配判定时必须填写「输出要求」（打印什么/几行/小数位/标点用英文半角）");
            }
            if (StringTools.isEmpty(bean.getOutputExample())) {
                throw new BusinessException("按输出精确匹配判定时必须填写「输出示例」（用另一组数据演示格式，不要用本题数据，避免泄题）");
            }
        }
        if (bean.getJudgeType() != null && bean.getJudgeType() == 3) {
            if (StringTools.isEmpty(bean.getExpectedPattern())) {
                throw new BusinessException("按正则判定时必须填写期望正则");
            }
            // 预校验：非法正则会让学生端整道题判分失败（原实现只在判分时编译，异常直抛 500）
            try {
                Pattern.compile(bean.getExpectedPattern());
            } catch (PatternSyntaxException e) {
                throw new BusinessException("期望正则不是合法的正则表达式：" + e.getDescription());
            }
        }
    }
}
