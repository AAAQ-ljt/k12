package com.nexora.mappers;

import com.nexora.entity.po.ExamInfo;
import com.nexora.entity.query.ExamInfoQuery;
import com.nexora.entity.vo.ExamInfoVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 考试表数据库操作接口
 */
public interface ExamInfoMapper {

    List<ExamInfoVO> selectPage(ExamInfoQuery query);

    Integer selectCount(ExamInfoQuery query);

    ExamInfoVO selectById(@Param("examId") String examId);

    Integer insert(ExamInfo bean);

    Integer update(ExamInfo bean);

    Integer deleteById(@Param("examId") String examId);
}
