package com.smart.campus.service.impl;

import java.util.List;

import jakarta.annotation.Resource;

import org.springframework.stereotype.Service;

import com.smart.campus.entity.enums.PageSize;
import com.smart.campus.entity.query.StudyPlanItemQuery;
import com.smart.campus.entity.po.StudyPlanItem;
import com.smart.campus.entity.vo.PaginationResultVO;
import com.smart.campus.entity.query.SimplePage;
import com.smart.campus.mappers.StudyPlanItemMapper;
import com.smart.campus.service.StudyPlanItemService;
import com.smart.campus.utils.StringTools;


/**
 * 学生学习计划明细表 业务接口实现
 */
@Service("studyPlanItemService")
public class StudyPlanItemServiceImpl implements StudyPlanItemService {

	@Resource
	private StudyPlanItemMapper<StudyPlanItem, StudyPlanItemQuery> studyPlanItemMapper;

	/**
	 * 根据条件查询列表
	 */
	@Override
	public List<StudyPlanItem> findListByParam(StudyPlanItemQuery param) {
		return this.studyPlanItemMapper.selectList(param);
	}

	/**
	 * 根据条件查询列表
	 */
	@Override
	public Integer findCountByParam(StudyPlanItemQuery param) {
		return this.studyPlanItemMapper.selectCount(param);
	}

	/**
	 * 分页查询方法
	 */
	@Override
	public PaginationResultVO<StudyPlanItem> findListByPage(StudyPlanItemQuery param) {
		int count = this.findCountByParam(param);
		int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

		SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
		param.setSimplePage(page);
		List<StudyPlanItem> list = this.findListByParam(param);
		PaginationResultVO<StudyPlanItem> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result;
	}

	/**
	 * 新增
	 */
	@Override
	public Integer add(StudyPlanItem bean) {
		return this.studyPlanItemMapper.insert(bean);
	}

	/**
	 * 批量新增
	 */
	@Override
	public Integer addBatch(List<StudyPlanItem> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.studyPlanItemMapper.insertBatch(listBean);
	}

	/**
	 * 批量新增或者修改
	 */
	@Override
	public Integer addOrUpdateBatch(List<StudyPlanItem> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.studyPlanItemMapper.insertOrUpdateBatch(listBean);
	}

	/**
	 * 多条件更新
	 */
	@Override
	public Integer updateByParam(StudyPlanItem bean, StudyPlanItemQuery param) {
		StringTools.checkParam(param);
		return this.studyPlanItemMapper.updateByParam(bean, param);
	}

	/**
	 * 多条件删除
	 */
	@Override
	public Integer deleteByParam(StudyPlanItemQuery param) {
		StringTools.checkParam(param);
		return this.studyPlanItemMapper.deleteByParam(param);
	}

	/**
	 * 根据ItemId获取对象
	 */
	@Override
	public StudyPlanItem getStudyPlanItemByItemId(Long itemId) {
		return this.studyPlanItemMapper.selectByItemId(itemId);
	}

	/**
	 * 根据ItemId修改
	 */
	@Override
	public Integer updateStudyPlanItemByItemId(StudyPlanItem bean, Long itemId) {
		return this.studyPlanItemMapper.updateByItemId(bean, itemId);
	}

	/**
	 * 根据ItemId删除
	 */
	@Override
	public Integer deleteStudyPlanItemByItemId(Long itemId) {
		return this.studyPlanItemMapper.deleteByItemId(itemId);
	}
}