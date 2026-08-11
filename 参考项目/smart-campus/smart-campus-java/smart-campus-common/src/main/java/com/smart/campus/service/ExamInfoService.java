package com.smart.campus.service;

import java.util.List;

import com.smart.campus.entity.query.ExamInfoQuery;
import com.smart.campus.entity.po.ExamInfo;
import com.smart.campus.entity.vo.PaginationResultVO;


/**
 * 在线考试表 业务接口
 */
public interface ExamInfoService {

	/**
	 * 根据条件查询列表
	 */
	List<ExamInfo> findListByParam(ExamInfoQuery param);

	/**
	 * 根据条件查询列表
	 */
	Integer findCountByParam(ExamInfoQuery param);

	/**
	 * 分页查询
	 */
	PaginationResultVO<ExamInfo> findListByPage(ExamInfoQuery param);

	/**
	 * 新增
	 */
	Integer add(ExamInfo bean);

	/**
	 * 批量新增
	 */
	Integer addBatch(List<ExamInfo> listBean);

	/**
	 * 批量新增/修改
	 */
	Integer addOrUpdateBatch(List<ExamInfo> listBean);

	/**
	 * 多条件更新
	 */
	Integer updateByParam(ExamInfo bean,ExamInfoQuery param);

	/**
	 * 多条件删除
	 */
	Integer deleteByParam(ExamInfoQuery param);

	/**
	 * 根据ExamId查询对象
	 */
	ExamInfo getExamInfoByExamId(String examId);


	/**
	 * 根据ExamId修改
	 */
	Integer updateExamInfoByExamId(ExamInfo bean,String examId);


	/**
	 * 根据ExamId删除
	 */
	Integer deleteExamInfoByExamId(String examId);

}