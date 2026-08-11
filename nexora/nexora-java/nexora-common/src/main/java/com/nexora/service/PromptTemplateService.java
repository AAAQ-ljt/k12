package com.nexora.service;

import java.util.List;

import com.nexora.entity.query.PromptTemplateQuery;
import com.nexora.entity.po.PromptTemplate;
import com.nexora.entity.vo.PaginationResultVO;


/**
 * 提示词模板表 业务接口
 */
public interface PromptTemplateService {

	/**
	 * 根据条件查询列表
	 */
	List<PromptTemplate> findListByParam(PromptTemplateQuery param);

	/**
	 * 根据条件查询列表
	 */
	Integer findCountByParam(PromptTemplateQuery param);

	/**
	 * 分页查询
	 */
	PaginationResultVO<PromptTemplate> findListByPage(PromptTemplateQuery param);

	/**
	 * 新增
	 */
	Integer add(PromptTemplate bean);

	/**
	 * 批量新增
	 */
	Integer addBatch(List<PromptTemplate> listBean);

	/**
	 * 批量新增/修改
	 */
	Integer addOrUpdateBatch(List<PromptTemplate> listBean);

	/**
	 * 多条件更新
	 */
	Integer updateByParam(PromptTemplate bean,PromptTemplateQuery param);

	/**
	 * 多条件删除
	 */
	Integer deleteByParam(PromptTemplateQuery param);

	/**
	 * 根据Id查询对象
	 */
	PromptTemplate getPromptTemplateById(Integer id);


	/**
	 * 根据Id修改
	 */
	Integer updatePromptTemplateById(PromptTemplate bean,Integer id);


	/**
	 * 根据Id删除
	 */
	Integer deletePromptTemplateById(Integer id);


	/**
	 * 根据StageAndScene查询对象
	 */
	PromptTemplate getPromptTemplateByStageAndScene(String stage,String scene);


	/**
	 * 根据StageAndScene修改
	 */
	Integer updatePromptTemplateByStageAndScene(PromptTemplate bean,String stage,String scene);


	/**
	 * 根据StageAndScene删除
	 */
	Integer deletePromptTemplateByStageAndScene(String stage,String scene);

}