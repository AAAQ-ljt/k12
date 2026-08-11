package com.smart.campus.service.impl;

import java.util.List;

import jakarta.annotation.Resource;

import org.springframework.stereotype.Service;

import com.smart.campus.entity.enums.PageSize;
import com.smart.campus.entity.query.StudyPlanQuery;
import com.smart.campus.entity.po.StudyPlan;
import com.smart.campus.entity.vo.PaginationResultVO;
import com.smart.campus.entity.query.SimplePage;
import com.smart.campus.mappers.StudyPlanMapper;
import com.smart.campus.service.StudyPlanService;
import com.smart.campus.utils.StringTools;


/**
 * 学生学习计划主表 业务接口实现
 */
@Service("studyPlanService")
public class StudyPlanServiceImpl implements StudyPlanService {

	@Resource
	private StudyPlanMapper<StudyPlan, StudyPlanQuery> studyPlanMapper;

	/**
	 * 根据条件查询列表
	 */
	@Override
	public List<StudyPlan> findListByParam(StudyPlanQuery param) {
		return this.studyPlanMapper.selectList(param);
	}

	/**
	 * 根据条件查询列表
	 */
	@Override
	public Integer findCountByParam(StudyPlanQuery param) {
		return this.studyPlanMapper.selectCount(param);
	}

	/**
	 * 分页查询方法
	 */
	@Override
	public PaginationResultVO<StudyPlan> findListByPage(StudyPlanQuery param) {
		int count = this.findCountByParam(param);
		int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

		SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
		param.setSimplePage(page);
		List<StudyPlan> list = this.findListByParam(param);
		PaginationResultVO<StudyPlan> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result;
	}

	/**
	 * 新增
	 */
	@Override
	public Integer add(StudyPlan bean) {
		return this.studyPlanMapper.insert(bean);
	}

	/**
	 * 批量新增
	 */
	@Override
	public Integer addBatch(List<StudyPlan> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.studyPlanMapper.insertBatch(listBean);
	}

	/**
	 * 批量新增或者修改
	 */
	@Override
	public Integer addOrUpdateBatch(List<StudyPlan> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.studyPlanMapper.insertOrUpdateBatch(listBean);
	}

	/**
	 * 多条件更新
	 */
	@Override
	public Integer updateByParam(StudyPlan bean, StudyPlanQuery param) {
		StringTools.checkParam(param);
		return this.studyPlanMapper.updateByParam(bean, param);
	}

	/**
	 * 多条件删除
	 */
	@Override
	public Integer deleteByParam(StudyPlanQuery param) {
		StringTools.checkParam(param);
		return this.studyPlanMapper.deleteByParam(param);
	}

	/**
	 * 根据PlanId获取对象
	 */
	@Override
	public StudyPlan getStudyPlanByPlanId(String planId) {
		return this.studyPlanMapper.selectByPlanId(planId);
	}

	/**
	 * 根据PlanId修改
	 */
	@Override
	public Integer updateStudyPlanByPlanId(StudyPlan bean, String planId) {
		return this.studyPlanMapper.updateByPlanId(bean, planId);
	}

	/**
	 * 根据PlanId删除
	 */
	@Override
	public Integer deleteStudyPlanByPlanId(String planId) {
		return this.studyPlanMapper.deleteByPlanId(planId);
	}
}