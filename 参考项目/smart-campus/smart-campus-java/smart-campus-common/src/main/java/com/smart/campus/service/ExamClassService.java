package com.smart.campus.service;

import java.util.List;

import com.smart.campus.entity.query.ExamClassQuery;
import com.smart.campus.entity.po.ExamClass;
import com.smart.campus.entity.vo.PaginationResultVO;


/**
 * 考试班级关联表 业务接口
 */
public interface ExamClassService {

	/**
	 * 根据条件查询列表
	 */
	List<ExamClass> findListByParam(ExamClassQuery param);

	/**
	 * 根据条件查询列表
	 */
	Integer findCountByParam(ExamClassQuery param);

	/**
	 * 分页查询
	 */
	PaginationResultVO<ExamClass> findListByPage(ExamClassQuery param);

	/**
	 * 新增
	 */
	Integer add(ExamClass bean);

	/**
	 * 批量新增
	 */
	Integer addBatch(List<ExamClass> listBean);

	/**
	 * 批量新增/修改
	 */
	Integer addOrUpdateBatch(List<ExamClass> listBean);

	/**
	 * 多条件更新
	 */
	Integer updateByParam(ExamClass bean,ExamClassQuery param);

	/**
	 * 多条件删除
	 */
	Integer deleteByParam(ExamClassQuery param);

	/**
	 * 根据ExamIdAndClassId查询对象
	 */
	ExamClass getExamClassByExamIdAndClassId(String examId,Integer classId);


	/**
	 * 根据ExamIdAndClassId修改
	 */
	Integer updateExamClassByExamIdAndClassId(ExamClass bean,String examId,Integer classId);


	/**
	 * 根据ExamIdAndClassId删除
	 */
	Integer deleteExamClassByExamIdAndClassId(String examId,Integer classId);

}