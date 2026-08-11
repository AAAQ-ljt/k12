package com.smart.campus.service;

import java.util.List;

import com.smart.campus.entity.query.PaperQuestionQuery;
import com.smart.campus.entity.po.PaperQuestion;
import com.smart.campus.entity.vo.PaginationResultVO;


/**
 * 试卷题目编排表 业务接口
 */
public interface PaperQuestionService {

	/**
	 * 根据条件查询列表
	 */
	List<PaperQuestion> findListByParam(PaperQuestionQuery param);

	/**
	 * 根据条件查询列表
	 */
	Integer findCountByParam(PaperQuestionQuery param);

	/**
	 * 分页查询
	 */
	PaginationResultVO<PaperQuestion> findListByPage(PaperQuestionQuery param);

	/**
	 * 新增
	 */
	Integer add(PaperQuestion bean);

	/**
	 * 批量新增
	 */
	Integer addBatch(List<PaperQuestion> listBean);

	/**
	 * 批量新增/修改
	 */
	Integer addOrUpdateBatch(List<PaperQuestion> listBean);

	/**
	 * 多条件更新
	 */
	Integer updateByParam(PaperQuestion bean,PaperQuestionQuery param);

	/**
	 * 多条件删除
	 */
	Integer deleteByParam(PaperQuestionQuery param);

	/**
	 * 根据Id查询对象
	 */
	PaperQuestion getPaperQuestionById(Integer id);


	/**
	 * 根据Id修改
	 */
	Integer updatePaperQuestionById(PaperQuestion bean,Integer id);


	/**
	 * 根据Id删除
	 */
	Integer deletePaperQuestionById(Integer id);


	/**
	 * 根据PaperIdAndQuestionId查询对象
	 */
	PaperQuestion getPaperQuestionByPaperIdAndQuestionId(String paperId,Integer questionId);


	/**
	 * 根据PaperIdAndQuestionId修改
	 */
	Integer updatePaperQuestionByPaperIdAndQuestionId(PaperQuestion bean,String paperId,Integer questionId);


	/**
	 * 根据PaperIdAndQuestionId删除
	 */
	Integer deletePaperQuestionByPaperIdAndQuestionId(String paperId,Integer questionId);

}