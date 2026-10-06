package com.nexora.service.impl;

import com.nexora.entity.enums.PageSize;
import com.nexora.entity.po.CodingProblem;
import com.nexora.entity.query.CodingProblemQuery;
import com.nexora.entity.query.SimplePage;
import com.nexora.entity.vo.PaginationResultVO;
import com.nexora.mappers.CodingProblemMapper;
import com.nexora.service.CodingProblemService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 编程题库 业务接口实现
 */
@Service("codingProblemService")
public class CodingProblemServiceImpl implements CodingProblemService {

    @Resource
    private CodingProblemMapper<CodingProblem, CodingProblemQuery> codingProblemMapper;

    @Override
    public List<CodingProblem> findListByParam(CodingProblemQuery param) {
        return codingProblemMapper.selectList(param);
    }

    @Override
    public Integer findCountByParam(CodingProblemQuery param) {
        return codingProblemMapper.selectCount(param);
    }

    @Override
    public PaginationResultVO<CodingProblem> findListByPage(CodingProblemQuery param) {
        int count = findCountByParam(param);
        int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();
        SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
        param.setSimplePage(page);
        return new PaginationResultVO<>(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(),
                findListByParam(param));
    }

    @Override
    public Integer add(CodingProblem bean) {
        return codingProblemMapper.insert(bean);
    }

    @Override
    public CodingProblem getCodingProblemByProblemId(String problemId) {
        return codingProblemMapper.selectByProblemId(problemId);
    }

    @Override
    public Integer updateCodingProblemByProblemId(CodingProblem bean, String problemId) {
        return codingProblemMapper.updateByProblemId(bean, problemId);
    }

    @Override
    public Integer deleteCodingProblemByProblemId(String problemId) {
        return codingProblemMapper.deleteByProblemId(problemId);
    }
}
