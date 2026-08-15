package com.nexora.admin.biz;

import com.nexora.admin.dto.QuestionSaveDTO;
import com.nexora.constants.Constants;
import com.nexora.entity.enums.StageEnum;
import com.nexora.entity.po.KnowledgePoint;
import com.nexora.entity.po.QuestionInfo;
import com.nexora.entity.po.QuestionOption;
import com.nexora.entity.query.QuestionInfoQuery;
import com.nexora.entity.query.QuestionOptionQuery;
import com.nexora.entity.vo.PaginationResultVO;
import com.nexora.entity.vo.QuestionDetailVO;
import com.nexora.exception.BusinessException;
import com.nexora.service.KnowledgePointService;
import com.nexora.service.QuestionInfoService;
import com.nexora.service.QuestionOptionService;
import com.nexora.utils.StringTools;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 习题管理业务：题目 CRUD、选项维护、审核上架。
 */
@Service
public class QuestionBiz {

    private static final int TYPE_SINGLE = 0;
    private static final int TYPE_MULTIPLE = 1;
    private static final int TYPE_JUDGE = 2;
    private static final int TYPE_FILL = 3;

    @Resource
    private QuestionInfoService questionInfoService;

    @Resource
    private QuestionOptionService questionOptionService;

    @Resource
    private KnowledgePointService knowledgePointService;

    public PaginationResultVO<QuestionInfo> questionPage(QuestionInfoQuery query) {
        if (StringTools.isEmpty(query.getOrderBy())) {
            query.setOrderBy("create_time desc");
        }
        return questionInfoService.findListByPage(query);
    }

    public QuestionDetailVO questionDetail(String questionId) {
        if (StringTools.isEmpty(questionId)) {
            throw new BusinessException("题目ID不能为空");
        }
        QuestionInfo question = questionInfoService.getQuestionInfoByQuestionId(questionId);
        if (question == null) {
            throw new BusinessException("题目不存在");
        }
        QuestionDetailVO detail = new QuestionDetailVO();
        detail.setQuestion(question);
        detail.setOptions(loadOptions(questionId));
        return detail;
    }

    @Transactional(rollbackFor = Exception.class)
    public String addQuestion(QuestionSaveDTO dto) {
        QuestionInfo bean = requireQuestion(dto);
        bean.setQuestionId(StringTools.getRandomNumber(Constants.LENGTH_15));
        fillStageByGrade(bean);
        validateQuestion(bean, dto.getOptions());
        if (bean.getSource() == null) {
            bean.setSource(0);
        }
        if (bean.getAuditStatus() == null) {
            bean.setAuditStatus(bean.getSource() == 1 ? 0 : 1);
        }
        if (bean.getStatus() == null) {
            bean.setStatus(bean.getAuditStatus() == 1 ? 1 : 0);
        }
        if (bean.getScore() == null) {
            bean.setScore(5);
        }
        Date now = new Date();
        bean.setCreateTime(now);
        bean.setUpdateTime(now);
        questionInfoService.add(bean);
        saveOptions(bean.getQuestionId(), bean.getQuestionType(), dto.getOptions());
        return bean.getQuestionId();
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateQuestion(QuestionSaveDTO dto) {
        QuestionInfo bean = requireQuestion(dto);
        if (StringTools.isEmpty(bean.getQuestionId())) {
            throw new BusinessException("题目ID不能为空");
        }
        if (questionInfoService.getQuestionInfoByQuestionId(bean.getQuestionId()) == null) {
            throw new BusinessException("题目不存在");
        }
        fillStageByGrade(bean);
        validateQuestion(bean, dto.getOptions());
        bean.setUpdateTime(new Date());
        questionInfoService.updateQuestionInfoByQuestionId(bean, bean.getQuestionId());

        QuestionOptionQuery optionQuery = new QuestionOptionQuery();
        optionQuery.setQuestionId(bean.getQuestionId());
        questionOptionService.deleteByParam(optionQuery);
        saveOptions(bean.getQuestionId(), bean.getQuestionType(), dto.getOptions());
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteQuestion(String questionId) {
        if (StringTools.isEmpty(questionId)) {
            throw new BusinessException("题目ID不能为空");
        }
        if (questionInfoService.getQuestionInfoByQuestionId(questionId) == null) {
            throw new BusinessException("题目不存在");
        }
        QuestionOptionQuery optionQuery = new QuestionOptionQuery();
        optionQuery.setQuestionId(questionId);
        questionOptionService.deleteByParam(optionQuery);
        questionInfoService.deleteQuestionInfoByQuestionId(questionId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void auditQuestion(String questionId, Integer auditStatus) {
        if (StringTools.isEmpty(questionId)) {
            throw new BusinessException("题目ID不能为空");
        }
        if (auditStatus == null || auditStatus < 0 || auditStatus > 2) {
            throw new BusinessException("非法的审核状态");
        }
        if (questionInfoService.getQuestionInfoByQuestionId(questionId) == null) {
            throw new BusinessException("题目不存在");
        }
        QuestionInfo updateBean = new QuestionInfo();
        updateBean.setAuditStatus(auditStatus);
        updateBean.setStatus(auditStatus == 1 ? 1 : 0);
        updateBean.setUpdateTime(new Date());
        questionInfoService.updateQuestionInfoByQuestionId(updateBean, questionId);
    }

    private QuestionInfo requireQuestion(QuestionSaveDTO dto) {
        if (dto == null || dto.getQuestion() == null) {
            throw new BusinessException("题目信息不能为空");
        }
        return dto.getQuestion();
    }

    private void validateQuestion(QuestionInfo bean, List<QuestionOption> options) {
        if (StringTools.isEmpty(bean.getTitle())) {
            throw new BusinessException("题干不能为空");
        }
        if (StringTools.isEmpty(bean.getGrade())) {
            throw new BusinessException("请选择年级");
        }
        validateKnowledgePoint(bean);
        if (bean.getQuestionType() == null) {
            throw new BusinessException("请选择题型");
        }
        if (bean.getDifficulty() == null || bean.getDifficulty() < 1 || bean.getDifficulty() > 3) {
            throw new BusinessException("难度必须为 1-3");
        }
        if (bean.getQuestionType() == TYPE_SINGLE || bean.getQuestionType() == TYPE_MULTIPLE) {
            validateChoiceOptions(bean.getQuestionType(), options);
        } else if (bean.getQuestionType() == TYPE_JUDGE || bean.getQuestionType() == TYPE_FILL) {
            if (StringTools.isEmpty(bean.getAnswer())) {
                throw new BusinessException("请填写答案");
            }
        } else {
            throw new BusinessException("非法的题型");
        }
    }

    private void validateKnowledgePoint(QuestionInfo bean) {
        if (StringTools.isEmpty(bean.getKnowledgePointId())) {
            throw new BusinessException("请选择知识点");
        }
        KnowledgePoint point = knowledgePointService
                .getKnowledgePointByKnowledgePointId(bean.getKnowledgePointId());
        if (point == null) {
            throw new BusinessException("知识点不存在");
        }
        if (!bean.getStage().equals(point.getStage())) {
            throw new BusinessException("知识点与所选年级不匹配");
        }
    }

    private void validateChoiceOptions(Integer questionType, List<QuestionOption> options) {
        if (options == null || options.isEmpty()) {
            throw new BusinessException("选择题必须配置选项");
        }
        if (options.size() < 2) {
            throw new BusinessException("选择题至少需要两个选项");
        }
        long answerCount = options.stream()
                .filter(option -> option.getIsAnswer() != null && option.getIsAnswer() == 1)
                .count();
        if (answerCount == 0) {
            throw new BusinessException("请至少标记一个正确答案");
        }
        if (questionType == TYPE_SINGLE && answerCount != 1) {
            throw new BusinessException("单选题只能有一个正确答案");
        }
    }

    private void saveOptions(String questionId, Integer questionType, List<QuestionOption> options) {
        if (questionType != TYPE_SINGLE && questionType != TYPE_MULTIPLE) {
            return;
        }
        if (options == null || options.isEmpty()) {
            return;
        }
        Date now = new Date();
        List<QuestionOption> saveList = new ArrayList<>();
        int index = 0;
        for (QuestionOption option : options) {
            if (option == null || StringTools.isEmpty(option.getOptionContent())) {
                continue;
            }
            if (StringTools.isEmpty(option.getOptionLabel())) {
                option.setOptionLabel(String.valueOf((char) ('A' + Math.min(index, 25))));
            }
            if (option.getIsAnswer() == null) {
                option.setIsAnswer(0);
            }
            option.setQuestionId(questionId);
            option.setSort(index + 1);
            option.setCreateTime(now);
            saveList.add(option);
            index++;
        }
        if (!saveList.isEmpty()) {
            questionOptionService.addBatch(saveList);
        }
    }

    private List<QuestionOption> loadOptions(String questionId) {
        QuestionOptionQuery query = new QuestionOptionQuery();
        query.setQuestionId(questionId);
        query.setOrderBy("sort asc, option_id asc");
        return questionOptionService.findListByParam(query);
    }

    private void fillStageByGrade(QuestionInfo bean) {
        if (!StringTools.isEmpty(bean.getGrade())) {
            String stage = StageEnum.matchByGrade(bean.getGrade());
            if (stage == null) {
                throw new BusinessException("非法的年级");
            }
            bean.setStage(stage);
        }
        if (StringTools.isEmpty(bean.getStage())) {
            throw new BusinessException("请选择年级");
        }
    }
}
