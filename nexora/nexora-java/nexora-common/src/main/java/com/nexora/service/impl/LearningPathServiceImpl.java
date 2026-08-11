package com.nexora.service.impl;

import java.util.List;

import jakarta.annotation.Resource;

import org.springframework.stereotype.Service;

import com.nexora.entity.enums.PageSize;
import com.nexora.entity.query.LearningPathQuery;
import com.nexora.entity.po.LearningPath;
import com.nexora.entity.vo.PaginationResultVO;
import com.nexora.entity.query.SimplePage;
import com.nexora.mappers.LearningPathMapper;
import com.nexora.service.LearningPathService;
import com.nexora.utils.StringTools;


/**
 * 学习路径表 业务接口实现
 */
@Service("learningPathService")
public class LearningPathServiceImpl implements LearningPathService {

	@Resource
	private LearningPathMapper<LearningPath, LearningPathQuery> learningPathMapper;

	/**
	 * 根据条件查询列表
	 */
	@Override
	public List<LearningPath> findListByParam(LearningPathQuery param) {
		return this.learningPathMapper.selectList(param);
	}

	/**
	 * 根据条件查询列表
	 */
	@Override
	public Integer findCountByParam(LearningPathQuery param) {
		return this.learningPathMapper.selectCount(param);
	}

	/**
	 * 分页查询方法
	 */
	@Override
	public PaginationResultVO<LearningPath> findListByPage(LearningPathQuery param) {
		int count = this.findCountByParam(param);
		int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

		SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
		param.setSimplePage(page);
		List<LearningPath> list = this.findListByParam(param);
		PaginationResultVO<LearningPath> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result;
	}

	/**
	 * 新增
	 */
	@Override
	public Integer add(LearningPath bean) {
		return this.learningPathMapper.insert(bean);
	}

	/**
	 * 批量新增
	 */
	@Override
	public Integer addBatch(List<LearningPath> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.learningPathMapper.insertBatch(listBean);
	}

	/**
	 * 批量新增或者修改
	 */
	@Override
	public Integer addOrUpdateBatch(List<LearningPath> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.learningPathMapper.insertOrUpdateBatch(listBean);
	}

	/**
	 * 多条件更新
	 */
	@Override
	public Integer updateByParam(LearningPath bean, LearningPathQuery param) {
		StringTools.checkParam(param);
		return this.learningPathMapper.updateByParam(bean, param);
	}

	/**
	 * 多条件删除
	 */
	@Override
	public Integer deleteByParam(LearningPathQuery param) {
		StringTools.checkParam(param);
		return this.learningPathMapper.deleteByParam(param);
	}

	/**
	 * 根据PathId获取对象
	 */
	@Override
	public LearningPath getLearningPathByPathId(String pathId) {
		return this.learningPathMapper.selectByPathId(pathId);
	}

	/**
	 * 根据PathId修改
	 */
	@Override
	public Integer updateLearningPathByPathId(LearningPath bean, String pathId) {
		return this.learningPathMapper.updateByPathId(bean, pathId);
	}

	/**
	 * 根据PathId删除
	 */
	@Override
	public Integer deleteLearningPathByPathId(String pathId) {
		return this.learningPathMapper.deleteByPathId(pathId);
	}
}