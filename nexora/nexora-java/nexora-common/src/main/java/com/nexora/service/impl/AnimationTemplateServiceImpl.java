package com.nexora.service.impl;

import java.util.List;

import jakarta.annotation.Resource;

import org.springframework.stereotype.Service;

import com.nexora.entity.enums.PageSize;
import com.nexora.entity.query.AnimationTemplateQuery;
import com.nexora.entity.po.AnimationTemplate;
import com.nexora.entity.vo.PaginationResultVO;
import com.nexora.entity.query.SimplePage;
import com.nexora.mappers.AnimationTemplateMapper;
import com.nexora.service.AnimationTemplateService;
import com.nexora.utils.StringTools;


/**
 * 动画模板库 业务接口实现
 */
@Service("animationTemplateService")
public class AnimationTemplateServiceImpl implements AnimationTemplateService {

	@Resource
	private AnimationTemplateMapper<AnimationTemplate, AnimationTemplateQuery> animationTemplateMapper;

	/**
	 * 根据条件查询列表
	 */
	@Override
	public List<AnimationTemplate> findListByParam(AnimationTemplateQuery param) {
		return this.animationTemplateMapper.selectList(param);
	}

	/**
	 * 根据条件查询列表
	 */
	@Override
	public Integer findCountByParam(AnimationTemplateQuery param) {
		return this.animationTemplateMapper.selectCount(param);
	}

	/**
	 * 分页查询方法
	 */
	@Override
	public PaginationResultVO<AnimationTemplate> findListByPage(AnimationTemplateQuery param) {
		int count = this.findCountByParam(param);
		int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

		SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
		param.setSimplePage(page);
		List<AnimationTemplate> list = this.findListByParam(param);
		PaginationResultVO<AnimationTemplate> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result;
	}

	/**
	 * 新增
	 */
	@Override
	public Integer add(AnimationTemplate bean) {
		return this.animationTemplateMapper.insert(bean);
	}

	/**
	 * 批量新增
	 */
	@Override
	public Integer addBatch(List<AnimationTemplate> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.animationTemplateMapper.insertBatch(listBean);
	}

	/**
	 * 批量新增或者修改
	 */
	@Override
	public Integer addOrUpdateBatch(List<AnimationTemplate> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.animationTemplateMapper.insertOrUpdateBatch(listBean);
	}

	/**
	 * 多条件更新
	 */
	@Override
	public Integer updateByParam(AnimationTemplate bean, AnimationTemplateQuery param) {
		StringTools.checkParam(param);
		return this.animationTemplateMapper.updateByParam(bean, param);
	}

	/**
	 * 多条件删除
	 */
	@Override
	public Integer deleteByParam(AnimationTemplateQuery param) {
		StringTools.checkParam(param);
		return this.animationTemplateMapper.deleteByParam(param);
	}

	/**
	 * 根据TemplateId获取对象
	 */
	@Override
	public AnimationTemplate getAnimationTemplateByTemplateId(Integer templateId) {
		return this.animationTemplateMapper.selectByTemplateId(templateId);
	}

	/**
	 * 根据TemplateId修改
	 */
	@Override
	public Integer updateAnimationTemplateByTemplateId(AnimationTemplate bean, Integer templateId) {
		return this.animationTemplateMapper.updateByTemplateId(bean, templateId);
	}

	/**
	 * 根据TemplateId删除
	 */
	@Override
	public Integer deleteAnimationTemplateByTemplateId(Integer templateId) {
		return this.animationTemplateMapper.deleteByTemplateId(templateId);
	}
}