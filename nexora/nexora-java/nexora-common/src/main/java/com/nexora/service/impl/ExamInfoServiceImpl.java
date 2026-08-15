package com.nexora.service.impl;

import com.nexora.entity.enums.PageSize;
import com.nexora.entity.po.ExamInfo;
import com.nexora.entity.query.ExamInfoQuery;
import com.nexora.entity.query.SimplePage;
import com.nexora.entity.vo.ExamInfoVO;
import com.nexora.entity.vo.PaginationResultVO;
import com.nexora.mappers.ExamInfoMapper;
import com.nexora.service.ExamInfoService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 考试表业务接口实现
 */
@Service("examInfoService")
public class ExamInfoServiceImpl implements ExamInfoService {

    @Resource
    private ExamInfoMapper examInfoMapper;

    @Override
    public PaginationResultVO<ExamInfoVO> findListByPage(ExamInfoQuery query) {
        int count = examInfoMapper.selectCount(query);
        int pageSize = query.getPageSize() == null ? PageSize.SIZE15.getSize() : query.getPageSize();
        SimplePage page = new SimplePage(query.getPageNo(), count, pageSize);
        query.setSimplePage(page);
        List<ExamInfoVO> list = examInfoMapper.selectPage(query);
        return new PaginationResultVO<>(count, page.getPageSize(), page.getPageNo(),
                page.getPageTotal(), list);
    }

    @Override
    public ExamInfoVO getById(String examId) {
        return examInfoMapper.selectById(examId);
    }

    @Override
    public Integer insert(ExamInfo bean) {
        return examInfoMapper.insert(bean);
    }

    @Override
    public Integer update(ExamInfo bean) {
        return examInfoMapper.update(bean);
    }

    @Override
    public Integer deleteById(String examId) {
        return examInfoMapper.deleteById(examId);
    }
}
