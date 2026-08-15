package com.nexora.service;

import com.nexora.entity.po.PaperGroup;
import com.nexora.entity.po.PaperInfo;
import com.nexora.entity.po.PaperQuestion;
import com.nexora.entity.query.PaperInfoQuery;
import com.nexora.entity.vo.PaginationResultVO;

import java.util.List;

/**
 * 试卷表业务接口
 */
public interface PaperInfoService {

    PaginationResultVO<PaperInfo> findListByPage(PaperInfoQuery query);

    PaperInfo getById(String paperId);

    Integer insert(PaperInfo bean);

    Integer update(PaperInfo bean);

    Integer deleteById(String paperId);

    List<PaperGroup> selectGroups(String paperId);

    Integer insertGroup(PaperGroup group);

    Integer deleteGroups(String paperId);

    List<PaperQuestion> selectQuestions(String paperId);

    Integer insertQuestion(PaperQuestion question);

    Integer deleteQuestions(String paperId);
}
