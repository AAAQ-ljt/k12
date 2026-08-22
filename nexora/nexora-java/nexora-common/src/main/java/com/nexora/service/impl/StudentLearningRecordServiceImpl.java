package com.nexora.service.impl;

import com.nexora.entity.po.StudentLearningRecord;
import com.nexora.entity.query.SimplePage;
import com.nexora.entity.query.StudentLearningRecordQuery;
import com.nexora.entity.vo.PaginationResultVO;
import com.nexora.mappers.StudentLearningRecordMapper;
import com.nexora.service.StudentLearningRecordService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 学生学习行为记录 业务实现
 */
@Service("studentLearningRecordService")
public class StudentLearningRecordServiceImpl implements StudentLearningRecordService {

    @Resource
    private StudentLearningRecordMapper<StudentLearningRecord, StudentLearningRecordQuery> studentLearningRecordMapper;

    @Override
    public List<StudentLearningRecord> findListByParam(StudentLearningRecordQuery param) {
        return studentLearningRecordMapper.selectList(param);
    }

    @Override
    public Integer findCountByParam(StudentLearningRecordQuery param) {
        return studentLearningRecordMapper.selectCount(param);
    }

    @Override
    public PaginationResultVO<StudentLearningRecord> findListByPage(StudentLearningRecordQuery param) {
        Integer count = findCountByParam(param);
        Integer pageSize = param.getPageSize() == null ? 10 : param.getPageSize();
        int pageNo = param.getPageNo() == null ? 1 : param.getPageNo();
        SimplePage page = new SimplePage(pageNo, count, pageSize);
        param.setSimplePage(page);
        List<StudentLearningRecord> list = findListByParam(param);
        return new PaginationResultVO<>(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
    }

    @Override
    public Integer add(StudentLearningRecord bean) {
        return studentLearningRecordMapper.insert(bean);
    }

    @Override
    public Integer deleteByParam(StudentLearningRecordQuery param) {
        return studentLearningRecordMapper.deleteByParam(param);
    }
}