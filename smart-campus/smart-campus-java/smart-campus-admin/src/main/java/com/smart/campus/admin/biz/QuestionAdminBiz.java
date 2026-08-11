package com.smart.campus.admin.biz;

import com.smart.campus.admin.entity.dto.QuestionOptionSaveDTO;
import com.smart.campus.admin.entity.dto.QuestionSaveDTO;
import com.smart.campus.entity.enums.ResponseCodeEnum;
import com.smart.campus.entity.po.QuestionInfo;
import com.smart.campus.entity.po.QuestionOption;
import com.smart.campus.entity.po.ResourceInfo;
import com.smart.campus.entity.query.QuestionInfoQuery;
import com.smart.campus.entity.query.QuestionOptionQuery;
import com.smart.campus.entity.vo.PaginationResultVO;
import com.smart.campus.admin.entity.vo.QuestionDetailVO;
import com.smart.campus.admin.entity.vo.QuestionListItemVO;
import com.smart.campus.admin.entity.vo.QuestionOptionVO;
import com.smart.campus.exception.BusinessException;
import com.smart.campus.service.QuestionInfoService;
import com.smart.campus.service.QuestionOptionService;
import com.smart.campus.service.ResourceInfoService;
import com.smart.campus.utils.StringTools;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class QuestionAdminBiz {

    private static final int DEFAULT_PAGE_NO = 1;
    private static final int DEFAULT_PAGE_SIZE = 15;
    private static final String QUESTION_ORDER_BY = "q.update_time desc,q.create_time desc";
    private static final String OPTION_ORDER_BY = "q.sort_order asc,q.option_id asc";
    private static final int QUESTION_TYPE_SINGLE = 1;
    private static final int QUESTION_TYPE_MULTI = 2;
    private static final int QUESTION_TYPE_JUDGE = 3;
    private static final int QUESTION_TYPE_FILL = 4;
    private static final int QUESTION_TYPE_ESSAY = 5;
    private static final int IMAGE_RESOURCE_TYPE = 2;
    private static final List<String> JUDGE_ANSWER_LIST = List.of("T", "F");

    @Resource
    private QuestionInfoService questionInfoService;

    @Resource
    private QuestionOptionService questionOptionService;

    @Resource
    private ResourceInfoService resourceInfoService;

    public PaginationResultVO<QuestionListItemVO> loadDataList(QuestionInfoQuery query) {
        QuestionInfoQuery request = buildPageQuery(query);
        PaginationResultVO<QuestionInfo> pageResult = questionInfoService.findListByPage(request);
        List<QuestionListItemVO> list = buildQuestionList(pageResult.getList());
        return new PaginationResultVO<>(
                pageResult.getTotalCount(),
                pageResult.getPageSize(),
                pageResult.getPageNo(),
                pageResult.getPageTotal(),
                list
        );
    }

    public QuestionDetailVO getQuestionInfoById(Integer questionId) {
        QuestionInfo questionInfo = questionInfoService.getQuestionInfoByQuestionId(questionId);
        if (questionInfo == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "题目信息不存在");
        }
        return buildQuestionDetail(questionInfo);
    }

    @Transactional(rollbackFor = Exception.class)
    public QuestionDetailVO add(QuestionSaveDTO dto) {
        QuestionSaveDTO request = normalizeSaveDTO(dto);
        validateQuestionSaveDTO(request);
        validateImageResources(request.getQuestionImageResourceIdList());

        QuestionInfo bean = buildQuestionInfo(request, null);
        questionInfoService.add(bean);
        syncQuestionOptionsAndAnswer(bean.getQuestionId(), request);
        return getQuestionInfoById(bean.getQuestionId());
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateQuestionInfoById(QuestionSaveDTO dto) {
        QuestionSaveDTO request = normalizeSaveDTO(dto);
        QuestionInfo original = questionInfoService.getQuestionInfoByQuestionId(request.getQuestionId());
        if (original == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "题目信息不存在");
        }
        validateQuestionSaveDTO(request);
        validateImageResources(request.getQuestionImageResourceIdList());

        QuestionInfo bean = buildQuestionInfo(request, original);
        questionInfoService.updateQuestionInfoByQuestionId(bean, original.getQuestionId());
        syncQuestionOptionsAndAnswer(original.getQuestionId(), request);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteQuestionInfoById(Integer questionId) {
        QuestionInfo original = questionInfoService.getQuestionInfoByQuestionId(questionId);
        if (original == null) {
            return;
        }
        deleteQuestionOptions(questionId);
        questionInfoService.deleteQuestionInfoByQuestionId(questionId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteBatch(String ids) {
        for (Integer questionId : StringTools.convertIds2List(ids)) {
            deleteQuestionInfoById(questionId);
        }
    }

    private QuestionInfoQuery buildPageQuery(QuestionInfoQuery query) {
        QuestionInfoQuery request = query == null ? new QuestionInfoQuery() : query;
        if (request.getPageNo() == null || request.getPageNo() < 1) {
            request.setPageNo(DEFAULT_PAGE_NO);
        }
        if (request.getPageSize() == null || request.getPageSize() < 1) {
            request.setPageSize(DEFAULT_PAGE_SIZE);
        }
        request.setOrderBy(QUESTION_ORDER_BY);
        return request;
    }

    private List<QuestionListItemVO> buildQuestionList(List<QuestionInfo> questionList) {
        if (questionList == null || questionList.isEmpty()) {
            return List.of();
        }
        Set<Integer> questionIdSet = questionList.stream()
                .map(QuestionInfo::getQuestionId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Integer, List<QuestionOption>> optionMap = loadOptionMap(questionIdSet);
        List<QuestionListItemVO> result = new ArrayList<>();
        for (QuestionInfo item : questionList) {
            List<QuestionOption> optionList = optionMap.getOrDefault(item.getQuestionId(), List.of());
            QuestionListItemVO vo = new QuestionListItemVO();
            vo.setQuestionId(item.getQuestionId());
            vo.setQuestionType(item.getQuestionType());
            vo.setQuestionTypeText(resolveQuestionTypeText(item.getQuestionType()));
            vo.setQuestionTitle(item.getQuestionTitle());
            vo.setDifficultyLevel(item.getDifficultyLevel());
            vo.setDifficultyLevelText(resolveDifficultyLevelText(item.getDifficultyLevel()));
            vo.setImageCount(parseIntegerIds(item.getQuestionImage()).size());
            vo.setOptionCount(optionList.size());
            vo.setOptionList(buildQuestionOptionVOList(optionList));
            vo.setCorrectAnswerText(resolveCorrectAnswerDisplay(item, optionList));
            vo.setCreateTime(item.getCreateTime());
            vo.setUpdateTime(item.getUpdateTime());
            result.add(vo);
        }
        return result;
    }

    private QuestionDetailVO buildQuestionDetail(QuestionInfo questionInfo) {
        QuestionDetailVO vo = new QuestionDetailVO();
        vo.setQuestionId(questionInfo.getQuestionId());
        vo.setQuestionType(questionInfo.getQuestionType());
        vo.setQuestionTitle(questionInfo.getQuestionTitle());
        vo.setQuestionImageResourceIdList(parseIntegerIds(questionInfo.getQuestionImage()));
        vo.setDifficultyLevel(questionInfo.getDifficultyLevel());
        vo.setAnswerAnalysis(questionInfo.getAnswerAnalysis());
        vo.setCreateTime(questionInfo.getCreateTime());
        vo.setUpdateTime(questionInfo.getUpdateTime());

        List<QuestionOption> optionList = loadQuestionOptions(questionInfo.getQuestionId());
        List<QuestionOptionVO> optionVOList = new ArrayList<>();
        Map<Integer, String> optionIdKeyMap = new LinkedHashMap<>();
        for (int index = 0; index < optionList.size(); index++) {
            QuestionOption item = optionList.get(index);
            String optionKey = resolveOptionKey(index);
            optionIdKeyMap.put(item.getOptionId(), optionKey);
            QuestionOptionVO optionVO = new QuestionOptionVO();
            optionVO.setOptionId(item.getOptionId());
            optionVO.setOptionKey(optionKey);
            optionVO.setOptionContent(item.getOptionContent());
            optionVO.setSortOrder(item.getSortOrder());
            optionVOList.add(optionVO);
        }
        vo.setOptionList(optionVOList);

        if (usesOptionAnswer(questionInfo.getQuestionType())) {
            List<String> correctKeyList = parseIntegerIds(questionInfo.getCorrectAnswer()).stream()
                    .map(optionIdKeyMap::get)
                    .filter(Objects::nonNull)
                    .toList();
            vo.setCorrectOptionKeyList(correctKeyList);
            vo.setCorrectAnswerText(String.join("、", correctKeyList));
        } else {
            vo.setCorrectAnswerText(questionInfo.getCorrectAnswer());
        }
        return vo;
    }

    private QuestionSaveDTO normalizeSaveDTO(QuestionSaveDTO dto) {
        QuestionSaveDTO request = dto == null ? new QuestionSaveDTO() : dto;
        request.setQuestionTitle(StringTools.trim(request.getQuestionTitle()));
        request.setCorrectAnswerText(StringTools.trim(request.getCorrectAnswerText()));
        request.setAnswerAnalysis(StringTools.trim(request.getAnswerAnalysis()));
        request.setQuestionImageResourceIdList(normalizeIntegerList(request.getQuestionImageResourceIdList()));
        request.setCorrectOptionKeyList(normalizeStringList(request.getCorrectOptionKeyList()));

        List<QuestionOptionSaveDTO> optionList = request.getOptionList() == null
                ? new ArrayList<>()
                : new ArrayList<>(request.getOptionList());
        for (int index = 0; index < optionList.size(); index++) {
            QuestionOptionSaveDTO option = optionList.get(index);
            if (option == null) {
                continue;
            }
            option.setOptionContent(StringTools.trim(option.getOptionContent()));
            option.setSortOrder(option.getSortOrder() == null ? index + 1 : option.getSortOrder());
        }
        request.setOptionList(optionList.stream().filter(Objects::nonNull).toList());
        if (request.getDifficultyLevel() == null) {
            request.setDifficultyLevel(3);
        }
        return request;
    }

    private void validateQuestionSaveDTO(QuestionSaveDTO dto) {
        Integer questionType = dto.getQuestionType();
        if (!List.of(QUESTION_TYPE_SINGLE, QUESTION_TYPE_MULTI, QUESTION_TYPE_JUDGE, QUESTION_TYPE_FILL, QUESTION_TYPE_ESSAY)
                .contains(questionType)) {
            throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "题目类型不正确");
        }
        if (dto.getDifficultyLevel() == null || dto.getDifficultyLevel() < 1 || dto.getDifficultyLevel() > 5) {
            throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "难度等级不正确");
        }
        if (questionType == QUESTION_TYPE_SINGLE || questionType == QUESTION_TYPE_MULTI) {
            validateChoiceQuestion(dto);
            return;
        }
        if (questionType == QUESTION_TYPE_JUDGE) {
            validateJudgeQuestion(dto);
        }
    }

    private void validateJudgeQuestion(QuestionSaveDTO dto) {
        if (!dto.getOptionList().isEmpty()) {
            throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "判断题不需要设置选项");
        }
        if (!dto.getCorrectOptionKeyList().isEmpty()) {
            throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "判断题不需要设置选项答案");
        }
        String correctAnswerText = defaultString(dto.getCorrectAnswerText()).toUpperCase();
        if (!JUDGE_ANSWER_LIST.contains(correctAnswerText)) {
            throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "判断题标准答案只能为T或F");
        }
        dto.setCorrectAnswerText(correctAnswerText);
    }

    private void validateChoiceQuestion(QuestionSaveDTO dto) {
        List<QuestionOptionSaveDTO> optionList = dto.getOptionList();
        if (optionList.size() < 2) {
            throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "选择题至少需要两个选项");
        }
        for (QuestionOptionSaveDTO option : optionList) {
            if (StringTools.isEmpty(option.getOptionContent())) {
                throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "选项内容不能为空");
            }
        }
        List<String> correctOptionKeyList = dto.getCorrectOptionKeyList();
        if (dto.getQuestionType() == QUESTION_TYPE_SINGLE && correctOptionKeyList.size() != 1) {
            throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "单选题必须选择一个正确答案");
        }
        if (dto.getQuestionType() == QUESTION_TYPE_MULTI && correctOptionKeyList.isEmpty()) {
            throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "多选题至少选择一个正确答案");
        }
        int maxOptionCount = optionList.size();
        for (String optionKey : correctOptionKeyList) {
            int index = resolveOptionIndex(optionKey);
            if (index < 0 || index >= maxOptionCount) {
                throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "存在无效的正确答案选项");
            }
        }
    }

    private void validateImageResources(List<Integer> resourceIdList) {
        if (resourceIdList == null || resourceIdList.isEmpty()) {
            return;
        }
        List<String> normalizedIdList = resourceIdList.stream()
                .filter(Objects::nonNull)
                .map(String::valueOf)
                .toList();
        List<ResourceInfo> resourceList = resourceInfoService.getResourceInfoByResourceIdList(normalizedIdList);
        if (resourceList.size() != resourceIdList.size()) {
            throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "存在无效的题目配图资源");
        }
        for (ResourceInfo resourceInfo : resourceList) {
            if (!Objects.equals(resourceInfo.getResourceType(), IMAGE_RESOURCE_TYPE)) {
                throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "题目配图只能选择图片资源");
            }
        }
    }

    private QuestionInfo buildQuestionInfo(QuestionSaveDTO dto, QuestionInfo original) {
        QuestionInfo bean = new QuestionInfo();
        bean.setQuestionType(dto.getQuestionType());
        bean.setQuestionTitle(dto.getQuestionTitle());
        bean.setQuestionImage(joinIntegerIds(dto.getQuestionImageResourceIdList()));
        bean.setDifficultyLevel(dto.getDifficultyLevel());
        bean.setCorrectAnswer(resolveInitialCorrectAnswer(dto));
        bean.setAnswerAnalysis(defaultString(dto.getAnswerAnalysis()));
        if (original != null) {
            bean.setCreateTime(original.getCreateTime());
        }
        return bean;
    }

    private String resolveInitialCorrectAnswer(QuestionSaveDTO dto) {
        if (usesOptionAnswer(dto.getQuestionType())) {
            return "";
        }
        return defaultString(dto.getCorrectAnswerText());
    }

    private void syncQuestionOptionsAndAnswer(Integer questionId, QuestionSaveDTO dto) {
        deleteQuestionOptions(questionId);
        List<QuestionOption> savedOptionList = saveQuestionOptions(questionId, dto);
        QuestionInfo answerBean = new QuestionInfo();
        answerBean.setCorrectAnswer(resolveCorrectAnswer(dto, savedOptionList));
        questionInfoService.updateQuestionInfoByQuestionId(answerBean, questionId);
    }

    private List<QuestionOption> saveQuestionOptions(Integer questionId, QuestionSaveDTO dto) {
        List<QuestionOption> result = new ArrayList<>();
        if (dto.getQuestionType() == QUESTION_TYPE_SINGLE || dto.getQuestionType() == QUESTION_TYPE_MULTI) {
            for (int index = 0; index < dto.getOptionList().size(); index++) {
                QuestionOptionSaveDTO item = dto.getOptionList().get(index);
                QuestionOption option = new QuestionOption();
                option.setQuestionId(questionId);
                option.setOptionContent(item.getOptionContent());
                option.setSortOrder(item.getSortOrder() == null ? index + 1 : item.getSortOrder());
                questionOptionService.add(option);
                result.add(option);
            }
            return result;
        }
        return result;
    }

    private String resolveCorrectAnswer(QuestionSaveDTO dto, List<QuestionOption> optionList) {
        if (!usesOptionAnswer(dto.getQuestionType())) {
            return defaultString(dto.getCorrectAnswerText());
        }
        Map<String, Integer> optionKeyIdMap = new LinkedHashMap<>();
        for (int index = 0; index < optionList.size(); index++) {
            optionKeyIdMap.put(resolveOptionKey(index), optionList.get(index).getOptionId());
        }
        List<Integer> correctOptionIdList = dto.getCorrectOptionKeyList().stream()
                .map(optionKeyIdMap::get)
                .filter(Objects::nonNull)
                .toList();
        if (dto.getQuestionType() == QUESTION_TYPE_SINGLE && correctOptionIdList.size() != 1) {
            throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "单选题正确答案设置失败");
        }
        if (dto.getQuestionType() == QUESTION_TYPE_MULTI && correctOptionIdList.isEmpty()) {
            throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "多选题正确答案设置失败");
        }
        return joinIntegerIds(correctOptionIdList);
    }

    private Map<Integer, List<QuestionOption>> loadOptionMap(Set<Integer> questionIdSet) {
        if (questionIdSet == null || questionIdSet.isEmpty()) {
            return Map.of();
        }
        Map<Integer, List<QuestionOption>> result = new LinkedHashMap<>();
        for (Integer questionId : questionIdSet) {
            QuestionOptionQuery query = new QuestionOptionQuery();
            query.setQuestionId(questionId);
            query.setOrderBy(OPTION_ORDER_BY);
            List<QuestionOption> optionList = questionOptionService.findListByParam(query);
            if (optionList != null && !optionList.isEmpty()) {
                result.put(questionId, optionList);
            }
        }
        return result;
    }

    private List<QuestionOption> loadQuestionOptions(Integer questionId) {
        QuestionOptionQuery query = new QuestionOptionQuery();
        query.setQuestionId(questionId);
        query.setOrderBy(OPTION_ORDER_BY);
        List<QuestionOption> optionList = questionOptionService.findListByParam(query);
        return optionList == null ? List.of() : optionList;
    }

    private List<QuestionOptionVO> buildQuestionOptionVOList(List<QuestionOption> optionList) {
        if (optionList == null || optionList.isEmpty()) {
            return List.of();
        }
        List<QuestionOptionVO> result = new ArrayList<>();
        for (int index = 0; index < optionList.size(); index++) {
            QuestionOption item = optionList.get(index);
            QuestionOptionVO optionVO = new QuestionOptionVO();
            optionVO.setOptionId(item.getOptionId());
            optionVO.setOptionKey(resolveOptionKey(index));
            optionVO.setOptionContent(item.getOptionContent());
            optionVO.setSortOrder(item.getSortOrder());
            result.add(optionVO);
        }
        return result;
    }

    private void deleteQuestionOptions(Integer questionId) {
        QuestionOptionQuery query = new QuestionOptionQuery();
        query.setQuestionId(questionId);
        questionOptionService.deleteByParam(query);
    }

    private String resolveCorrectAnswerDisplay(QuestionInfo questionInfo, List<QuestionOption> optionList) {
        if (!usesOptionAnswer(questionInfo.getQuestionType())) {
            return defaultString(questionInfo.getCorrectAnswer());
        }
        Set<Integer> answerIdSet = new LinkedHashSet<>(parseIntegerIds(questionInfo.getCorrectAnswer()));
        List<String> correctKeyList = new ArrayList<>();
        for (int index = 0; index < optionList.size(); index++) {
            Integer optionId = optionList.get(index).getOptionId();
            if (answerIdSet.contains(optionId)) {
                correctKeyList.add(resolveOptionKey(index));
            }
        }
        return String.join("、", correctKeyList);
    }

    private boolean usesOptionAnswer(Integer questionType) {
        return Objects.equals(questionType, QUESTION_TYPE_SINGLE)
                || Objects.equals(questionType, QUESTION_TYPE_MULTI);
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

    private int resolveOptionIndex(String optionKey) {
        if (StringTools.isEmpty(optionKey)) {
            return -1;
        }
        char key = Character.toUpperCase(optionKey.charAt(0));
        return key - 'A';
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

    private String joinIntegerIds(Collection<Integer> idList) {
        if (idList == null || idList.isEmpty()) {
            return "";
        }
        return idList.stream()
                .filter(Objects::nonNull)
                .distinct()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
    }

    private List<Integer> normalizeIntegerList(List<Integer> valueList) {
        if (valueList == null || valueList.isEmpty()) {
            return List.of();
        }
        LinkedHashSet<Integer> result = valueList.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        return new ArrayList<>(result);
    }

    private List<String> normalizeStringList(List<String> valueList) {
        if (valueList == null || valueList.isEmpty()) {
            return List.of();
        }
        LinkedHashSet<String> result = valueList.stream()
                .map(StringTools::trim)
                .filter(value -> !StringTools.isEmpty(value))
                .map(String::toUpperCase)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        return new ArrayList<>(result);
    }

    private String defaultString(String value) {
        return StringTools.isEmpty(value) ? "" : value;
    }
}
