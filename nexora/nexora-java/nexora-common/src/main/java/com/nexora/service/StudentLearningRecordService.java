package com.nexora.service;

import com.nexora.entity.po.StudentLearningRecord;
import com.nexora.entity.query.StudentLearningRecordQuery;
import com.nexora.entity.vo.PaginationResultVO;

import java.util.List;

/**
 * 学生学习行为记录 业务接口
 */
public interface StudentLearningRecordService {

    List<StudentLearningRecord> findListByParam(StudentLearningRecordQuery param);

    Integer findCountByParam(StudentLearningRecordQuery param);

    PaginationResultVO<StudentLearningRecord> findListByPage(StudentLearningRecordQuery param);

    Integer add(StudentLearningRecord bean);

    Integer deleteByParam(StudentLearningRecordQuery param);
}