package com.nexora.service;

import com.nexora.entity.po.CodingContest;
import com.nexora.entity.po.CodingContestProblem;
import com.nexora.entity.po.CodingContestRecord;
import com.nexora.entity.query.CodingContestProblemQuery;
import com.nexora.entity.query.CodingContestQuery;
import com.nexora.entity.query.CodingContestRecordQuery;
import com.nexora.entity.vo.PaginationResultVO;

import java.util.List;

/**
 * 编程比赛 业务接口（含赛题编排与报名/成绩）
 */
public interface CodingContestService {

	List<CodingContest> findListByParam(CodingContestQuery param);

	Integer findCountByParam(CodingContestQuery param);

	PaginationResultVO<CodingContest> findListByPage(CodingContestQuery param);

	CodingContest getCodingContestByContestId(String contestId);

	Integer add(CodingContest bean);

	Integer updateCodingContestByContestId(CodingContest bean, String contestId);

	Integer deleteCodingContestByContestId(String contestId);

	/** 赛题编排：先清空该场赛题再按顺序写入 */
	void saveContestProblems(String contestId, List<String> problemIds, List<Integer> scoreOverrides);

	List<CodingContestProblem> findProblemsByContestId(String contestId);

	List<CodingContestProblem> findProblemsByContestIds(List<String> contestIds);

	List<CodingContestRecord> findRecordsByParam(CodingContestRecordQuery param);

	Integer countRecordsByParam(CodingContestRecordQuery param);

	CodingContestRecord getRecord(String contestId, String userId);

	Integer addRecord(CodingContestRecord record);

	Integer updateRecord(CodingContestRecord record, String contestId, String userId);
}
