package com.nexora.service;

import java.util.List;

import com.nexora.entity.query.AnimationTemplateQuery;
import com.nexora.entity.po.AnimationTemplate;
import com.nexora.entity.vo.PaginationResultVO;


/**
 * 动画模板库 业务接口
 */
public interface AnimationTemplateService {

	/**
	 * 根据条件查询列表
	 */
	List<AnimationTemplate> findListByParam(AnimationTemplateQuery param);

	/**
	 * 根据条件查询列表
	 */
	Integer findCountByParam(AnimationTemplateQuery param);

	/**
	 * 分页查询
	 */
	PaginationResultVO<AnimationTemplate> findListByPage(AnimationTemplateQuery param);

	/**
	 * 新增
	 */
	Integer add(AnimationTemplate bean);

	/**
	 * 批量新增
	 */
	Integer addBatch(List<AnimationTemplate> listBean);

	/**
	 * 批量新增/修改
	 */
	Integer addOrUpdateBatch(List<AnimationTemplate> listBean);

	/**
	 * 多条件更新
	 */
	Integer updateByParam(AnimationTemplate bean,AnimationTemplateQuery param);

	/**
	 * 多条件删除
	 */
	Integer deleteByParam(AnimationTemplateQuery param);

	/**
	 * 根据TemplateId查询对象
	 */
	AnimationTemplate getAnimationTemplateByTemplateId(Integer templateId);


	/**
	 * 根据TemplateId修改
	 */
	Integer updateAnimationTemplateByTemplateId(AnimationTemplate bean,Integer templateId);


	/**
	 * 根据TemplateId删除
	 */
	Integer deleteAnimationTemplateByTemplateId(Integer templateId);

}