package com.nexora.service;

import java.util.List;

import com.nexora.entity.query.QuestionOptionQuery;
import com.nexora.entity.po.QuestionOption;
import com.nexora.entity.vo.PaginationResultVO;


/**
 * 题目选项表 业务接口
 */
public interface QuestionOptionService {

	/**
	 * 根据条件查询列表
	 */
	List<QuestionOption> findListByParam(QuestionOptionQuery param);

	/**
	 * 根据条件查询列表
	 */
	Integer findCountByParam(QuestionOptionQuery param);

	/**
	 * 分页查询
	 */
	PaginationResultVO<QuestionOption> findListByPage(QuestionOptionQuery param);

	/**
	 * 新增
	 */
	Integer add(QuestionOption bean);

	/**
	 * 批量新增
	 */
	Integer addBatch(List<QuestionOption> listBean);

	/**
	 * 批量新增/修改
	 */
	Integer addOrUpdateBatch(List<QuestionOption> listBean);

	/**
	 * 多条件更新
	 */
	Integer updateByParam(QuestionOption bean,QuestionOptionQuery param);

	/**
	 * 多条件删除
	 */
	Integer deleteByParam(QuestionOptionQuery param);

	/**
	 * 根据OptionId查询对象
	 */
	QuestionOption getQuestionOptionByOptionId(Integer optionId);


	/**
	 * 根据OptionId修改
	 */
	Integer updateQuestionOptionByOptionId(QuestionOption bean,Integer optionId);


	/**
	 * 根据OptionId删除
	 */
	Integer deleteQuestionOptionByOptionId(Integer optionId);

}