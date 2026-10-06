package com.nexora.service;

import com.nexora.entity.po.CodingProblem;
import com.nexora.entity.query.CodingProblemQuery;
import com.nexora.entity.vo.PaginationResultVO;

import java.util.List;

/**
 * 编程题库 业务接口
 */
public interface CodingProblemService {

	List<CodingProblem> findListByParam(CodingProblemQuery param);

	Integer findCountByParam(CodingProblemQuery param);

	PaginationResultVO<CodingProblem> findListByPage(CodingProblemQuery param);

	Integer add(CodingProblem bean);

	CodingProblem getCodingProblemByProblemId(String problemId);

	Integer updateCodingProblemByProblemId(CodingProblem bean, String problemId);

	Integer deleteCodingProblemByProblemId(String problemId);
}
