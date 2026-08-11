package com.smart.campus.admin.biz;

import com.alibaba.fastjson2.JSON;
import com.smart.campus.admin.entity.dto.PaperQuestionSaveDTO;
import com.smart.campus.admin.entity.dto.PaperSaveDTO;
import com.smart.campus.admin.entity.dto.PaperSectionSaveDTO;
import com.smart.campus.admin.entity.dto.PaperStructureSaveDTO;
import com.smart.campus.entity.enums.ResponseCodeEnum;
import com.smart.campus.entity.po.PaperInfo;
import com.smart.campus.entity.po.PaperQuestion;
import com.smart.campus.entity.po.QuestionInfo;
import com.smart.campus.entity.po.QuestionOption;
import com.smart.campus.entity.query.PaperInfoQuery;
import com.smart.campus.entity.query.PaperQuestionQuery;
import com.smart.campus.entity.query.QuestionOptionQuery;
import com.smart.campus.entity.vo.PaginationResultVO;
import com.smart.campus.admin.entity.vo.PaperDetailVO;
import com.smart.campus.admin.entity.vo.PaperListItemVO;
import com.smart.campus.admin.entity.vo.PaperQuestionItemVO;
import com.smart.campus.admin.entity.vo.PaperQuestionSnapshotVO;
import com.smart.campus.admin.entity.vo.PaperSectionVO;
import com.smart.campus.admin.entity.vo.QuestionOptionVO;
import com.smart.campus.exception.BusinessException;
import com.smart.campus.service.PaperInfoService;
import com.smart.campus.service.PaperQuestionService;
import com.smart.campus.service.QuestionInfoService;
import com.smart.campus.service.QuestionOptionService;
import com.smart.campus.utils.StringTools;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PaperAdminBiz {

    private static final int DEFAULT_PAGE_NO = 1;
    private static final int DEFAULT_PAGE_SIZE = 15;
    private static final int PAPER_TYPE_HOMEWORK = 1;
    private static final int PAPER_TYPE_EXAM = 2;
    private static final int SECTION_TYPE_GROUP = 1;
    private static final int SECTION_TYPE_QUESTION = 0;
    private static final int QUESTION_TYPE_SINGLE = 1;
    private static final int QUESTION_TYPE_MULTI = 2;
    private static final int QUESTION_TYPE_JUDGE = 3;
    private static final int QUESTION_TYPE_FILL = 4;
    private static final int QUESTION_TYPE_ESSAY = 5;
    private static final String PAPER_ORDER_BY = "p.update_time desc,p.create_time desc";
    private static final String PAPER_QUESTION_ORDER_BY = "p.sort_order asc,p.id asc";

    @Resource
    private PaperInfoService paperInfoService;

    @Resource
    private PaperQuestionService paperQuestionService;

    @Resource
    private QuestionInfoService questionInfoService;

    @Resource
    private QuestionOptionService questionOptionService;

    public PaginationResultVO<PaperListItemVO> loadDataList(PaperInfoQuery query) {
        PaperInfoQuery request = buildPageQuery(query);
        PaginationResultVO<PaperInfo> pageResult = paperInfoService.findListByPage(request);
        List<PaperListItemVO> list = buildPaperList(pageResult.getList());
        return new PaginationResultVO<>(
                pageResult.getTotalCount(),
                pageResult.getPageSize(),
                pageResult.getPageNo(),
                pageResult.getPageTotal(),
                list
        );
    }

    public PaperDetailVO getPaperInfoById(String paperId) {
        PaperInfo paperInfo = paperInfoService.getPaperInfoByPaperId(paperId);
        if (paperInfo == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "试卷信息不存在");
        }
        return buildPaperDetail(paperInfo);
    }

    @Transactional(rollbackFor = Exception.class)
    public PaperDetailVO add(PaperSaveDTO dto) {
        PaperSaveDTO request = normalizeSaveDTO(dto);
        validatePaperSaveDTO(request);
        String paperId = generateStringId();
        PaperInfo bean = buildPaperInfo(request, paperId, null);
        paperInfoService.add(bean);
        return getPaperInfoById(paperId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void updatePaperInfoById(PaperSaveDTO dto) {
        PaperSaveDTO request = normalizeSaveDTO(dto);
        PaperInfo original = paperInfoService.getPaperInfoByPaperId(request.getPaperId());
        if (original == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "试卷信息不存在");
        }
        validatePaperSaveDTO(request);
        PaperInfo bean = buildPaperInfo(request, original.getPaperId(), original);
        paperInfoService.updatePaperInfoByPaperId(bean, original.getPaperId());
    }

    @Transactional(rollbackFor = Exception.class)
    public void saveStructure(PaperStructureSaveDTO dto) {
        PaperStructureSaveDTO request = normalizeStructureSaveDTO(dto);
        PaperInfo original = paperInfoService.getPaperInfoByPaperId(request.getPaperId());
        if (original == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "试卷信息不存在");
        }
        validatePaperStructure(request);

        deletePaperQuestions(original.getPaperId());
        BigDecimal totalScore = BigDecimal.ZERO;
        int questionCount = 0;
        for (PaperSectionSaveDTO sectionDTO : request.getSectionList()) {
            PaperQuestion section = new PaperQuestion();
            section.setPaperId(original.getPaperId());
            section.setSectionType(SECTION_TYPE_GROUP);
            section.setSectionName(sectionDTO.getSectionName());
            section.setParentId(0);
            section.setSortOrder(sectionDTO.getSortOrder());
            section.setQuestionScore(BigDecimal.ZERO);
            section.setQuestionSnapshot("");
            paperQuestionService.add(section);

            for (PaperQuestionSaveDTO questionDTO : sectionDTO.getQuestionList()) {
                QuestionInfo questionInfo = questionInfoService.getQuestionInfoByQuestionId(questionDTO.getQuestionId());
                if (questionInfo == null) {
                    throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "存在已删除或无效的题目");
                }
                PaperQuestion question = new PaperQuestion();
                question.setPaperId(original.getPaperId());
                question.setQuestionId(questionInfo.getQuestionId());
                question.setQuestionScore(questionDTO.getQuestionScore());
                question.setQuestionType(questionInfo.getQuestionType());
                question.setSectionType(SECTION_TYPE_QUESTION);
                question.setSectionName(sectionDTO.getSectionName());
                question.setParentId(section.getId());
                question.setSortOrder(questionDTO.getSortOrder());
                question.setQuestionSnapshot(JSON.toJSONString(buildQuestionSnapshot(questionInfo)));
                paperQuestionService.add(question);
                totalScore = totalScore.add(defaultScore(questionDTO.getQuestionScore()));
                questionCount++;
            }
        }

        PaperInfo updateBean = new PaperInfo();
        updateBean.setTotalScore(totalScore);
        paperInfoService.updatePaperInfoByPaperId(updateBean, original.getPaperId());
    }

    @Transactional(rollbackFor = Exception.class)
    public void deletePaperInfoById(String paperId) {
        PaperInfo original = paperInfoService.getPaperInfoByPaperId(paperId);
        if (original == null) {
            return;
        }
        deletePaperQuestions(paperId);
        paperInfoService.deletePaperInfoByPaperId(paperId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteBatch(String ids) {
        for (String paperId : parseStringIds(ids)) {
            deletePaperInfoById(paperId);
        }
    }

    private PaperInfoQuery buildPageQuery(PaperInfoQuery query) {
        PaperInfoQuery request = query == null ? new PaperInfoQuery() : query;
        if (request.getPageNo() == null || request.getPageNo() < 1) {
            request.setPageNo(DEFAULT_PAGE_NO);
        }
        if (request.getPageSize() == null || request.getPageSize() < 1) {
            request.setPageSize(DEFAULT_PAGE_SIZE);
        }
        request.setOrderBy(PAPER_ORDER_BY);
        return request;
    }

    private List<PaperListItemVO> buildPaperList(List<PaperInfo> paperList) {
        if (paperList == null || paperList.isEmpty()) {
            return List.of();
        }
        Map<String, List<PaperQuestion>> questionMap = loadPaperQuestionMap(paperList.stream()
                .map(PaperInfo::getPaperId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet()));
        List<PaperListItemVO> result = new ArrayList<>();
        for (PaperInfo item : paperList) {
            List<PaperQuestion> structureList = questionMap.getOrDefault(item.getPaperId(), List.of());
            PaperListItemVO vo = new PaperListItemVO();
            vo.setPaperId(item.getPaperId());
            vo.setPaperName(item.getPaperName());
            vo.setPaperType(item.getPaperType());
            vo.setPaperTypeText(resolvePaperTypeText(item.getPaperType()));
            vo.setTotalScore(item.getTotalScore());
            vo.setQuestionCount((int) structureList.stream()
                    .filter(structure -> Objects.equals(structure.getSectionType(), SECTION_TYPE_QUESTION))
                    .count());
            vo.setSectionCount((int) structureList.stream()
                    .filter(structure -> Objects.equals(structure.getSectionType(), SECTION_TYPE_GROUP))
                    .count());
            vo.setCreateTime(item.getCreateTime());
            vo.setUpdateTime(item.getUpdateTime());
            result.add(vo);
        }
        return result;
    }

    private PaperDetailVO buildPaperDetail(PaperInfo paperInfo) {
        PaperDetailVO vo = new PaperDetailVO();
        vo.setPaperId(paperInfo.getPaperId());
        vo.setPaperName(paperInfo.getPaperName());
        vo.setPaperType(paperInfo.getPaperType());
        vo.setPaperTypeText(resolvePaperTypeText(paperInfo.getPaperType()));
        vo.setDescription(paperInfo.getDescription());
        vo.setTotalScore(paperInfo.getTotalScore());
        vo.setCreateTime(paperInfo.getCreateTime());
        vo.setUpdateTime(paperInfo.getUpdateTime());

        List<PaperQuestion> structureList = loadPaperQuestions(paperInfo.getPaperId());
        Map<Integer, List<PaperQuestion>> childMap = buildPaperQuestionChildMap(structureList);
        List<PaperSectionVO> sectionList = buildPaperSectionTree(childMap);
        int questionCount = sectionList.stream().mapToInt(section -> section.getQuestionList().size()).sum();

        vo.setSectionList(sectionList);
        vo.setSectionCount(sectionList.size());
        vo.setQuestionCount(questionCount);
        return vo;
    }

    private PaperQuestionItemVO buildPaperQuestionItemVO(PaperQuestion item) {
        PaperQuestionItemVO vo = new PaperQuestionItemVO();
        vo.setId(item.getId());
        vo.setQuestionScore(item.getQuestionScore());
        vo.setSortOrder(item.getSortOrder());

        PaperQuestionSnapshotVO snapshot = parseQuestionSnapshot(item.getQuestionSnapshot());
        if (snapshot != null) {
            copySnapshot(snapshot, vo);
            return vo;
        }

        if (item.getQuestionId() != null) {
            QuestionInfo questionInfo = questionInfoService.getQuestionInfoByQuestionId(item.getQuestionId());
            if (questionInfo != null) {
                copySnapshot(buildQuestionSnapshot(questionInfo), vo);
            }
        }
        return vo;
    }

    private void copySnapshot(PaperQuestionSnapshotVO source, PaperQuestionItemVO target) {
        target.setQuestionId(source.getQuestionId());
        target.setQuestionType(source.getQuestionType());
        target.setQuestionTypeText(source.getQuestionTypeText());
        target.setQuestionTitle(source.getQuestionTitle());
        target.setDifficultyLevel(source.getDifficultyLevel());
        target.setDifficultyLevelText(source.getDifficultyLevelText());
        target.setQuestionImageResourceIdList(source.getQuestionImageResourceIdList());
        target.setCorrectAnswerText(source.getCorrectAnswerText());
        target.setAnswerAnalysis(source.getAnswerAnalysis());
        target.setOptionList(source.getOptionList());
    }

    private PaperQuestionSnapshotVO buildQuestionSnapshot(QuestionInfo questionInfo) {
        PaperQuestionSnapshotVO snapshot = new PaperQuestionSnapshotVO();
        snapshot.setQuestionId(questionInfo.getQuestionId());
        snapshot.setQuestionType(questionInfo.getQuestionType());
        snapshot.setQuestionTypeText(resolveQuestionTypeText(questionInfo.getQuestionType()));
        snapshot.setQuestionTitle(questionInfo.getQuestionTitle());
        snapshot.setDifficultyLevel(questionInfo.getDifficultyLevel());
        snapshot.setDifficultyLevelText(resolveDifficultyLevelText(questionInfo.getDifficultyLevel()));
        snapshot.setQuestionImageResourceIdList(parseIntegerIds(questionInfo.getQuestionImage()));
        snapshot.setAnswerAnalysis(defaultString(questionInfo.getAnswerAnalysis()));

        List<QuestionOption> optionList = loadQuestionOptions(questionInfo.getQuestionId());
        List<QuestionOptionVO> optionVOList = new ArrayList<>();
        Map<Integer, String> optionKeyMap = new LinkedHashMap<>();
        for (int index = 0; index < optionList.size(); index++) {
            QuestionOption option = optionList.get(index);
            String optionKey = resolveOptionKey(index);
            optionKeyMap.put(option.getOptionId(), optionKey);
            QuestionOptionVO optionVO = new QuestionOptionVO();
            optionVO.setOptionId(option.getOptionId());
            optionVO.setOptionKey(optionKey);
            optionVO.setOptionContent(option.getOptionContent());
            optionVO.setSortOrder(option.getSortOrder());
            optionVOList.add(optionVO);
        }
        snapshot.setOptionList(optionVOList);
        snapshot.setCorrectAnswerText(resolveCorrectAnswerDisplay(questionInfo, optionList, optionKeyMap));
        return snapshot;
    }

    private String resolveCorrectAnswerDisplay(QuestionInfo questionInfo,
                                               List<QuestionOption> optionList,
                                               Map<Integer, String> optionKeyMap) {
        if (!usesOptionAnswer(questionInfo.getQuestionType())) {
            return defaultString(questionInfo.getCorrectAnswer());
        }
        List<String> correctKeyList = parseIntegerIds(questionInfo.getCorrectAnswer()).stream()
                .map(optionKeyMap::get)
                .filter(Objects::nonNull)
                .toList();
        if (!correctKeyList.isEmpty()) {
            return String.join("、", correctKeyList);
        }
        Set<Integer> answerIdSet = new LinkedHashSet<>(parseIntegerIds(questionInfo.getCorrectAnswer()));
        List<String> fallbackKeyList = new ArrayList<>();
        for (int index = 0; index < optionList.size(); index++) {
            if (answerIdSet.contains(optionList.get(index).getOptionId())) {
                fallbackKeyList.add(resolveOptionKey(index));
            }
        }
        return String.join("、", fallbackKeyList);
    }

    private List<QuestionOption> loadQuestionOptions(Integer questionId) {
        QuestionOptionQuery query = new QuestionOptionQuery();
        query.setQuestionId(questionId);
        query.setOrderBy("q.sort_order asc,q.option_id asc");
        List<QuestionOption> optionList = questionOptionService.findListByParam(query);
        return optionList == null ? List.of() : optionList;
    }

    private PaperSaveDTO normalizeSaveDTO(PaperSaveDTO dto) {
        PaperSaveDTO request = dto == null ? new PaperSaveDTO() : dto;
        request.setPaperId(StringTools.trim(request.getPaperId()));
        request.setPaperName(StringTools.trim(request.getPaperName()));
        request.setDescription(StringTools.trim(request.getDescription()));
        return request;
    }

    private PaperStructureSaveDTO normalizeStructureSaveDTO(PaperStructureSaveDTO dto) {
        PaperStructureSaveDTO request = dto == null ? new PaperStructureSaveDTO() : dto;
        request.setPaperId(StringTools.trim(request.getPaperId()));
        List<PaperSectionSaveDTO> sectionList = request.getSectionList() == null
                ? new ArrayList<>()
                : new ArrayList<>(request.getSectionList());
        List<PaperSectionSaveDTO> normalizedSectionList = new ArrayList<>();
        for (int sectionIndex = 0; sectionIndex < sectionList.size(); sectionIndex++) {
            PaperSectionSaveDTO section = sectionList.get(sectionIndex);
            if (section == null) {
                continue;
            }
            section.setSectionName(StringTools.trim(section.getSectionName()));
            section.setSortOrder(section.getSortOrder() == null ? sectionIndex + 1 : section.getSortOrder());
            List<PaperQuestionSaveDTO> questionList = section.getQuestionList() == null
                    ? new ArrayList<>()
                    : new ArrayList<>(section.getQuestionList());
            List<PaperQuestionSaveDTO> normalizedQuestionList = new ArrayList<>();
            for (int questionIndex = 0; questionIndex < questionList.size(); questionIndex++) {
                PaperQuestionSaveDTO question = questionList.get(questionIndex);
                if (question == null) {
                    continue;
                }
                question.setSortOrder(question.getSortOrder() == null ? questionIndex + 1 : question.getSortOrder());
                normalizedQuestionList.add(question);
            }
            section.setQuestionList(normalizedQuestionList);
            normalizedSectionList.add(section);
        }
        request.setSectionList(normalizedSectionList);
        return request;
    }

    private void validatePaperSaveDTO(PaperSaveDTO dto) {
        if (!List.of(PAPER_TYPE_HOMEWORK, PAPER_TYPE_EXAM).contains(dto.getPaperType())) {
            throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "试卷类型不正确");
        }
    }

    private void validatePaperStructure(PaperStructureSaveDTO dto) {
        Set<Integer> questionIdSet = new LinkedHashSet<>();
        for (PaperSectionSaveDTO section : dto.getSectionList()) {
            if (StringTools.isEmpty(section.getSectionName())) {
                throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "分组名称不能为空");
            }
            for (PaperQuestionSaveDTO question : section.getQuestionList()) {
                if (question.getQuestionId() == null) {
                    throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "题目ID不能为空");
                }
                if (!questionIdSet.add(question.getQuestionId())) {
                    throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "同一张试卷不能重复选择同一道题");
                }
                if (question.getQuestionScore() == null || question.getQuestionScore().compareTo(BigDecimal.ZERO) < 0) {
                    throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "题目分值不能小于0");
                }
            }
        }
    }

    private PaperInfo buildPaperInfo(PaperSaveDTO dto, String paperId, PaperInfo original) {
        PaperInfo bean = new PaperInfo();
        bean.setPaperId(paperId);
        bean.setPaperName(dto.getPaperName());
        bean.setPaperType(dto.getPaperType());
        bean.setDescription(defaultString(dto.getDescription()));
        bean.setTotalScore(original == null ? BigDecimal.ZERO : original.getTotalScore());
        if (original != null) {
            bean.setCreateTime(original.getCreateTime());
        }
        return bean;
    }

    private Map<String, List<PaperQuestion>> loadPaperQuestionMap(Set<String> paperIdSet) {
        if (paperIdSet == null || paperIdSet.isEmpty()) {
            return Map.of();
        }
        PaperQuestionQuery query = new PaperQuestionQuery();
        query.setOrderBy(PAPER_QUESTION_ORDER_BY);
        return paperQuestionService.findListByParam(query).stream()
                .filter(item -> paperIdSet.contains(item.getPaperId()))
                .collect(Collectors.groupingBy(PaperQuestion::getPaperId));
    }

    private List<PaperQuestion> loadPaperQuestions(String paperId) {
        PaperQuestionQuery query = new PaperQuestionQuery();
        query.setPaperId(paperId);
        query.setOrderBy(PAPER_QUESTION_ORDER_BY);
        List<PaperQuestion> questionList = paperQuestionService.findListByParam(query);
        return questionList == null ? List.of() : questionList;
    }

    private Map<Integer, List<PaperQuestion>> buildPaperQuestionChildMap(List<PaperQuestion> structureList) {
        Map<Integer, List<PaperQuestion>> childMap = new LinkedHashMap<>();
        if (structureList == null || structureList.isEmpty()) {
            return childMap;
        }
        Comparator<PaperQuestion> comparator = Comparator
                .comparing(PaperQuestion::getSortOrder, Comparator.nullsLast(Integer::compareTo))
                .thenComparing(PaperQuestion::getId, Comparator.nullsLast(Integer::compareTo));
        for (PaperQuestion item : structureList) {
            Integer parentId = item.getParentId() == null ? 0 : item.getParentId();
            childMap.computeIfAbsent(parentId, key -> new ArrayList<>()).add(item);
        }
        childMap.values().forEach(list -> list.sort(comparator));
        return childMap;
    }

    private List<PaperSectionVO> buildPaperSectionTree(Map<Integer, List<PaperQuestion>> childMap) {
        List<PaperSectionVO> sectionList = new ArrayList<>();
        for (PaperQuestion item : childMap.getOrDefault(0, List.of())) {
            if (Objects.equals(item.getSectionType(), SECTION_TYPE_GROUP)) {
                PaperSectionVO sectionVO = new PaperSectionVO();
                sectionVO.setId(item.getId());
                sectionVO.setSectionName(item.getSectionName());
                sectionVO.setSortOrder(item.getSortOrder());
                appendQuestionItems(sectionVO, item.getId(), childMap);
                sectionList.add(sectionVO);
                continue;
            }
            PaperSectionVO fallbackSection = ensureFallbackSection(sectionList, item);
            appendPaperQuestionItem(fallbackSection, item);
        }
        return sectionList;
    }

    private void appendQuestionItems(PaperSectionVO sectionVO, Integer parentId, Map<Integer, List<PaperQuestion>> childMap) {
        for (PaperQuestion child : childMap.getOrDefault(parentId == null ? 0 : parentId, List.of())) {
            if (Objects.equals(child.getSectionType(), SECTION_TYPE_GROUP)) {
                appendQuestionItems(sectionVO, child.getId(), childMap);
                continue;
            }
            appendPaperQuestionItem(sectionVO, child);
        }
    }

    private void appendPaperQuestionItem(PaperSectionVO sectionVO, PaperQuestion paperQuestion) {
        if (sectionVO == null || paperQuestion == null || !Objects.equals(paperQuestion.getSectionType(), SECTION_TYPE_QUESTION)) {
            return;
        }
        sectionVO.getQuestionList().add(buildPaperQuestionItemVO(paperQuestion));
    }

    private PaperSectionVO ensureFallbackSection(List<PaperSectionVO> sectionList, PaperQuestion paperQuestion) {
        if (!sectionList.isEmpty()) {
            return sectionList.get(sectionList.size() - 1);
        }
        PaperSectionVO fallbackSection = new PaperSectionVO();
        fallbackSection.setId(paperQuestion.getParentId());
        fallbackSection.setSectionName(defaultString(paperQuestion.getSectionName()));
        fallbackSection.setSortOrder(paperQuestion.getSortOrder());
        sectionList.add(fallbackSection);
        return fallbackSection;
    }

    private void deletePaperQuestions(String paperId) {
        PaperQuestionQuery query = new PaperQuestionQuery();
        query.setPaperId(paperId);
        paperQuestionService.deleteByParam(query);
    }

    private PaperQuestionSnapshotVO parseQuestionSnapshot(String questionSnapshot) {
        if (StringTools.isEmpty(questionSnapshot)) {
            return null;
        }
        try {
            return JSON.parseObject(questionSnapshot, PaperQuestionSnapshotVO.class);
        } catch (Exception ignored) {
            return null;
        }
    }

    private boolean usesOptionAnswer(Integer questionType) {
        return Objects.equals(questionType, QUESTION_TYPE_SINGLE)
                || Objects.equals(questionType, QUESTION_TYPE_MULTI);
    }

    private String resolvePaperTypeText(Integer paperType) {
        return switch (paperType == null ? 0 : paperType) {
            case PAPER_TYPE_HOMEWORK -> "课后习题";
            case PAPER_TYPE_EXAM -> "考试试卷";
            default -> "未知";
        };
    }

    private String resolveQuestionTypeText(Integer questionType) {
        return switch (questionType == null ? 0 : questionType) {
            case QUESTION_TYPE_SINGLE -> "单选题";
            case QUESTION_TYPE_MULTI -> "多选题";
            case QUESTION_TYPE_JUDGE -> "判断题";
            case QUESTION_TYPE_FILL -> "简答题";
            case QUESTION_TYPE_ESSAY -> "简答题";
            default -> "未知";
        };
    }

    private String resolveDifficultyLevelText(Integer difficultyLevel) {
        return switch (difficultyLevel == null ? 0 : difficultyLevel) {
            case 1 -> "简单";
            case 2 -> "较易";
            case 3 -> "中等";
            case 4 -> "较难";
            case 5 -> "困难";
            default -> "未知";
        };
    }

    private String resolveOptionKey(int index) {
        return String.valueOf((char) ('A' + index));
    }

    private List<Integer> parseIntegerIds(String ids) {
        if (StringTools.isEmpty(ids)) {
            return List.of();
        }
        return List.of(ids.split(",")).stream()
                .map(StringTools::trim)
                .filter(value -> !StringTools.isEmpty(value))
                .distinct()
                .map(Integer::valueOf)
                .toList();
    }

    private List<String> parseStringIds(String ids) {
        if (StringTools.isEmpty(ids)) {
            return List.of();
        }
        return List.of(ids.split(",")).stream()
                .map(StringTools::trim)
                .filter(value -> !StringTools.isEmpty(value))
                .distinct()
                .toList();
    }

    private BigDecimal defaultScore(BigDecimal score) {
        return score == null ? BigDecimal.ZERO : score;
    }

    private String defaultString(String value) {
        return StringTools.isEmpty(value) ? "" : value;
    }

    private String generateStringId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();
    }
}
