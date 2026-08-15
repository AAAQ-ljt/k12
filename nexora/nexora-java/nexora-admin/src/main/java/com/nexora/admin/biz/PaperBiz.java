package com.nexora.admin.biz;

import com.nexora.admin.dto.PaperSaveDTO;
import com.nexora.admin.vo.PaperDetailVO;
import com.nexora.constants.Constants;
import com.nexora.entity.enums.StageEnum;
import com.nexora.entity.po.PaperGroup;
import com.nexora.entity.po.PaperInfo;
import com.nexora.entity.po.PaperQuestion;
import com.nexora.entity.po.QuestionInfo;
import com.nexora.entity.query.PaperInfoQuery;
import com.nexora.entity.query.QuestionInfoQuery;
import com.nexora.entity.vo.PaginationResultVO;
import com.nexora.exception.BusinessException;
import com.nexora.service.PaperInfoService;
import com.nexora.service.QuestionInfoService;
import com.nexora.utils.StringTools;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 试卷管理业务
 */
@Service
public class PaperBiz {

    @Resource
    private PaperInfoService paperInfoService;

    @Resource
    private QuestionInfoService questionInfoService;

    public PaginationResultVO<PaperInfo> page(PaperInfoQuery query) {
        if (StringTools.isEmpty(query.getOrderBy())) {
            query.setOrderBy("create_time desc");
        }
        return paperInfoService.findListByPage(query);
    }

    public PaperDetailVO detail(String paperId) {
        if (StringTools.isEmpty(paperId)) {
            throw new BusinessException("试卷ID不能为空");
        }
        PaperInfo paper = paperInfoService.getById(paperId);
        if (paper == null) {
            throw new BusinessException("试卷不存在");
        }
        List<PaperGroup> groups = paperInfoService.selectGroups(paperId);
        List<PaperQuestion> questions = paperInfoService.selectQuestions(paperId);
        Map<String, QuestionInfo> questionMap = loadQuestionMap(questions);
        Map<String, List<PaperQuestion>> groupQuestionMap = questions.stream()
                .collect(Collectors.groupingBy(PaperQuestion::getGroupId));

        PaperDetailVO vo = new PaperDetailVO();
        vo.setPaper(paper);
        List<PaperDetailVO.GroupVO> groupVOs = new ArrayList<>();
        for (PaperGroup group : groups) {
            PaperDetailVO.GroupVO groupVO = new PaperDetailVO.GroupVO();
            groupVO.setGroupId(group.getGroupId());
            groupVO.setGroupName(group.getGroupName());
            List<PaperQuestion> groupQuestions = groupQuestionMap.getOrDefault(group.getGroupId(), List.of());
            List<PaperDetailVO.QuestionVO> questionVOs = new ArrayList<>();
            for (PaperQuestion pq : groupQuestions) {
                QuestionInfo question = questionMap.get(pq.getQuestionId());
                if (question == null) {
                    continue;
                }
                PaperDetailVO.QuestionVO qv = new PaperDetailVO.QuestionVO();
                qv.setQuestionId(question.getQuestionId());
                qv.setTitle(question.getTitle());
                qv.setQuestionType(question.getQuestionType());
                qv.setDifficulty(question.getDifficulty());
                qv.setScore(pq.getScore());
                qv.setSort(pq.getSort());
                questionVOs.add(qv);
            }
            groupVO.setQuestions(questionVOs);
            groupVOs.add(groupVO);
        }
        vo.setGroups(groupVOs);
        return vo;
    }

    @Transactional(rollbackFor = Exception.class)
    public void save(PaperSaveDTO dto) {
        if (dto == null || dto.getPaper() == null) {
            throw new BusinessException("试卷信息不能为空");
        }
        PaperInfo paper = dto.getPaper();
        if (StringTools.isEmpty(paper.getPaperName())) {
            throw new BusinessException("请填写试卷名称");
        }
        if (StringTools.isEmpty(paper.getGrade())) {
            throw new BusinessException("请选择年级");
        }
        String stage = StageEnum.matchByGrade(paper.getGrade());
        if (stage == null) {
            throw new BusinessException("非法的年级");
        }
        paper.setStage(stage);
        Date now = new Date();
        boolean create = StringTools.isEmpty(paper.getPaperId());
        if (create) {
            paper.setPaperId(StringTools.getRandomNumber(Constants.LENGTH_15));
            if (paper.getPaperType() == null) {
                paper.setPaperType(0);
            }
            if (paper.getStatus() == null) {
                paper.setStatus(1);
            }
            paper.setTotalScore(0);
            paper.setCreateTime(now);
            paper.setUpdateTime(now);
            paperInfoService.insert(paper);
        } else {
            if (paperInfoService.getById(paper.getPaperId()) == null) {
                throw new BusinessException("试卷不存在");
            }
            paper.setUpdateTime(now);
            paperInfoService.update(paper);
        }

        String paperId = paper.getPaperId();
        paperInfoService.deleteQuestions(paperId);
        paperInfoService.deleteGroups(paperId);
        int totalScore = 0;
        List<PaperSaveDTO.GroupItem> groups = dto.getGroups();
        if (groups != null) {
            int groupSort = 0;
            for (PaperSaveDTO.GroupItem item : groups) {
                if (StringTools.isEmpty(item.getGroupName())) {
                    throw new BusinessException("大题名称不能为空");
                }
                PaperGroup group = new PaperGroup();
                group.setGroupId(StringTools.isEmpty(item.getGroupId())
                        ? StringTools.getRandomNumber(Constants.LENGTH_15)
                        : item.getGroupId());
                group.setPaperId(paperId);
                group.setGroupName(item.getGroupName());
                group.setGroupSort(groupSort++);
                group.setCreateTime(now);
                paperInfoService.insertGroup(group);
                List<PaperSaveDTO.QuestionItem> questions = item.getQuestions();
                if (questions == null) {
                    continue;
                }
                int sort = 0;
                for (PaperSaveDTO.QuestionItem q : questions) {
                    if (StringTools.isEmpty(q.getQuestionId())) {
                        throw new BusinessException("大题包含空题目");
                    }
                    PaperQuestion pq = new PaperQuestion();
                    pq.setPaperId(paperId);
                    pq.setGroupId(group.getGroupId());
                    pq.setQuestionId(q.getQuestionId());
                    pq.setScore(q.getScore() == null ? 5 : q.getScore());
                    pq.setSort(sort++);
                    pq.setCreateTime(now);
                    paperInfoService.insertQuestion(pq);
                    totalScore += pq.getScore();
                }
            }
        }
        PaperInfo totalUpdate = new PaperInfo();
        totalUpdate.setPaperId(paperId);
        totalUpdate.setTotalScore(totalScore);
        totalUpdate.setUpdateTime(now);
        paperInfoService.update(totalUpdate);
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(String paperId) {
        if (StringTools.isEmpty(paperId)) {
            throw new BusinessException("试卷ID不能为空");
        }
        if (paperInfoService.getById(paperId) == null) {
            throw new BusinessException("试卷不存在");
        }
        paperInfoService.deleteQuestions(paperId);
        paperInfoService.deleteGroups(paperId);
        paperInfoService.deleteById(paperId);
    }

    private Map<String, QuestionInfo> loadQuestionMap(List<PaperQuestion> questions) {
        Map<String, QuestionInfo> map = new HashMap<>();
        if (questions == null || questions.isEmpty()) {
            return map;
        }
        List<String> ids = questions.stream()
                .map(PaperQuestion::getQuestionId)
                .distinct()
                .collect(Collectors.toList());
        QuestionInfoQuery query = new QuestionInfoQuery();
        query.setQuestionIds(ids);
        for (QuestionInfo question : questionInfoService.findListByParam(query)) {
            map.put(question.getQuestionId(), question);
        }
        return map;
    }
}
