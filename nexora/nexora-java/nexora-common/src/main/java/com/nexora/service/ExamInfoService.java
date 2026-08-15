package com.nexora.service;

import com.nexora.entity.po.ExamInfo;
import com.nexora.entity.query.ExamInfoQuery;
import com.nexora.entity.vo.ExamInfoVO;
import com.nexora.entity.vo.PaginationResultVO;

/**
 * 考试表业务接口
 */
public interface ExamInfoService {

    PaginationResultVO<ExamInfoVO> findListByPage(ExamInfoQuery query);

    ExamInfoVO getById(String examId);

    Integer insert(ExamInfo bean);

    Integer update(ExamInfo bean);

    Integer deleteById(String examId);
}
