package com.nexora.service.impl;

import com.nexora.entity.enums.PageSize;
import com.nexora.entity.po.PaperGroup;
import com.nexora.entity.po.PaperInfo;
import com.nexora.entity.po.PaperQuestion;
import com.nexora.entity.query.PaperInfoQuery;
import com.nexora.entity.query.SimplePage;
import com.nexora.entity.vo.PaginationResultVO;
import com.nexora.mappers.PaperInfoMapper;
import com.nexora.service.PaperInfoService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 试卷表业务接口实现
 */
@Service("paperInfoService")
public class PaperInfoServiceImpl implements PaperInfoService {

    @Resource
    private PaperInfoMapper paperInfoMapper;

    @Override
    public PaginationResultVO<PaperInfo> findListByPage(PaperInfoQuery query) {
        int count = paperInfoMapper.selectCount(query);
        int pageSize = query.getPageSize() == null ? PageSize.SIZE15.getSize() : query.getPageSize();
        SimplePage page = new SimplePage(query.getPageNo(), count, pageSize);
        query.setSimplePage(page);
        List<PaperInfo> list = paperInfoMapper.selectPage(query);
        return new PaginationResultVO<>(count, page.getPageSize(), page.getPageNo(),
                page.getPageTotal(), list);
    }

    @Override
    public PaperInfo getById(String paperId) {
        return paperInfoMapper.selectById(paperId);
    }

    @Override
    public Integer insert(PaperInfo bean) {
        return paperInfoMapper.insert(bean);
    }

    @Override
    public Integer update(PaperInfo bean) {
        return paperInfoMapper.update(bean);
    }

    @Override
    public Integer deleteById(String paperId) {
        return paperInfoMapper.deleteById(paperId);
    }

    @Override
    public List<PaperGroup> selectGroups(String paperId) {
        return paperInfoMapper.selectGroups(paperId);
    }

    @Override
    public Integer insertGroup(PaperGroup group) {
        return paperInfoMapper.insertGroup(group);
    }

    @Override
    public Integer deleteGroups(String paperId) {
        return paperInfoMapper.deleteGroups(paperId);
    }

    @Override
    public List<PaperQuestion> selectQuestions(String paperId) {
        return paperInfoMapper.selectQuestions(paperId);
    }

    @Override
    public Integer insertQuestion(PaperQuestion question) {
        return paperInfoMapper.insertQuestion(question);
    }

    @Override
    public Integer deleteQuestions(String paperId) {
        return paperInfoMapper.deleteQuestions(paperId);
    }
}
