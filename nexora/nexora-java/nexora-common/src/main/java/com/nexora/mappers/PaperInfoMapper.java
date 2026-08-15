package com.nexora.mappers;

import com.nexora.entity.po.PaperGroup;
import com.nexora.entity.po.PaperInfo;
import com.nexora.entity.po.PaperQuestion;
import com.nexora.entity.query.PaperInfoQuery;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 试卷表数据库操作接口
 */
public interface PaperInfoMapper {

    List<PaperInfo> selectPage(PaperInfoQuery query);

    Integer selectCount(PaperInfoQuery query);

    PaperInfo selectById(@Param("paperId") String paperId);

    Integer insert(PaperInfo bean);

    Integer update(PaperInfo bean);

    Integer deleteById(@Param("paperId") String paperId);

    List<PaperGroup> selectGroups(@Param("paperId") String paperId);

    Integer insertGroup(PaperGroup group);

    Integer deleteGroups(@Param("paperId") String paperId);

    List<PaperQuestion> selectQuestions(@Param("paperId") String paperId);

    Integer insertQuestion(PaperQuestion question);

    Integer deleteQuestions(@Param("paperId") String paperId);
}
