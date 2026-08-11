package com.nexora.service.impl;

import java.util.List;

import jakarta.annotation.Resource;

import org.springframework.stereotype.Service;

import com.nexora.entity.enums.PageSize;
import com.nexora.entity.query.LearningPathItemQuery;
import com.nexora.entity.po.LearningPathItem;
import com.nexora.entity.vo.PaginationResultVO;
import com.nexora.entity.query.SimplePage;
import com.nexora.mappers.LearningPathItemMapper;
import com.nexora.service.LearningPathItemService;
import com.nexora.utils.StringTools;


/**
 * 路径节点表 业务接口实现
 */
@Service("learningPathItemService")
public class LearningPathItemServiceImpl implements LearningPathItemService {

	@Resource
	private LearningPathItemMapper<LearningPathItem, LearningPathItemQuery> learningPathItemMapper;

	/**
	 * 根据条件查询列表
	 */
	@Override
	public List<LearningPathItem> findListByParam(LearningPathItemQuery param) {
		return this.learningPathItemMapper.selectList(param);
	}

	/**
	 * 根据条件查询列表
	 */
	@Override
	public Integer findCountByParam(LearningPathItemQuery param) {
		return this.learningPathItemMapper.selectCount(param);
	}

	/**
	 * 分页查询方法
	 */
	@Override
	public PaginationResultVO<LearningPathItem> findListByPage(LearningPathItemQuery param) {
		int count = this.findCountByParam(param);
		int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

		SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
		param.setSimplePage(page);
		List<LearningPathItem> list = this.findListByParam(param);
		PaginationResultVO<LearningPathItem> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result;
	}

	/**
	 * 新增
	 */
	@Override
	public Integer add(LearningPathItem bean) {
		return this.learningPathItemMapper.insert(bean);
	}

	/**
	 * 批量新增
	 */
	@Override
	public Integer addBatch(List<LearningPathItem> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.learningPathItemMapper.insertBatch(listBean);
	}

	/**
	 * 批量新增或者修改
	 */
	@Override
	public Integer addOrUpdateBatch(List<LearningPathItem> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.learningPathItemMapper.insertOrUpdateBatch(listBean);
	}

	/**
	 * 多条件更新
	 */
	@Override
	public Integer updateByParam(LearningPathItem bean, LearningPathItemQuery param) {
		StringTools.checkParam(param);
		return this.learningPathItemMapper.updateByParam(bean, param);
	}

	/**
	 * 多条件删除
	 */
	@Override
	public Integer deleteByParam(LearningPathItemQuery param) {
		StringTools.checkParam(param);
		return this.learningPathItemMapper.deleteByParam(param);
	}

	/**
	 * 根据ItemId获取对象
	 */
	@Override
	public LearningPathItem getLearningPathItemByItemId(String itemId) {
		return this.learningPathItemMapper.selectByItemId(itemId);
	}

	/**
	 * 根据ItemId修改
	 */
	@Override
	public Integer updateLearningPathItemByItemId(LearningPathItem bean, String itemId) {
		return this.learningPathItemMapper.updateByItemId(bean, itemId);
	}

	/**
	 * 根据ItemId删除
	 */
	@Override
	public Integer deleteLearningPathItemByItemId(String itemId) {
		return this.learningPathItemMapper.deleteByItemId(itemId);
	}
}