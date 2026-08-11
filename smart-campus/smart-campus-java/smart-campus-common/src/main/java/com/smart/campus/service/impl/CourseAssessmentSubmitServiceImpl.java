package com.smart.campus.service.impl;

import java.util.List;

import jakarta.annotation.Resource;

import org.springframework.stereotype.Service;

import com.smart.campus.entity.enums.PageSize;
import com.smart.campus.entity.query.CourseAssessmentSubmitQuery;
import com.smart.campus.entity.po.CourseAssessmentSubmit;
import com.smart.campus.entity.vo.PaginationResultVO;
import com.smart.campus.entity.query.SimplePage;
import com.smart.campus.mappers.CourseAssessmentSubmitMapper;
import com.smart.campus.service.CourseAssessmentSubmitService;
import com.smart.campus.utils.StringTools;


/**
 * 课程作业/考试学生提交表 业务接口实现
 */
@Service("courseAssessmentSubmitService")
public class CourseAssessmentSubmitServiceImpl implements CourseAssessmentSubmitService {

	@Resource
	private CourseAssessmentSubmitMapper<CourseAssessmentSubmit, CourseAssessmentSubmitQuery> courseAssessmentSubmitMapper;

	/**
	 * 根据条件查询列表
	 */
	@Override
	public List<CourseAssessmentSubmit> findListByParam(CourseAssessmentSubmitQuery param) {
		return this.courseAssessmentSubmitMapper.selectList(param);
	}

	/**
	 * 根据条件查询列表
	 */
	@Override
	public Integer findCountByParam(CourseAssessmentSubmitQuery param) {
		return this.courseAssessmentSubmitMapper.selectCount(param);
	}

	/**
	 * 分页查询方法
	 */
	@Override
	public PaginationResultVO<CourseAssessmentSubmit> findListByPage(CourseAssessmentSubmitQuery param) {
		int count = this.findCountByParam(param);
		int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

		SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
		param.setSimplePage(page);
		List<CourseAssessmentSubmit> list = this.findListByParam(param);
		PaginationResultVO<CourseAssessmentSubmit> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result;
	}

	/**
	 * 新增
	 */
	@Override
	public Integer add(CourseAssessmentSubmit bean) {
		return this.courseAssessmentSubmitMapper.insert(bean);
	}

	/**
	 * 批量新增
	 */
	@Override
	public Integer addBatch(List<CourseAssessmentSubmit> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.courseAssessmentSubmitMapper.insertBatch(listBean);
	}

	/**
	 * 批量新增或者修改
	 */
	@Override
	public Integer addOrUpdateBatch(List<CourseAssessmentSubmit> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.courseAssessmentSubmitMapper.insertOrUpdateBatch(listBean);
	}

	/**
	 * 多条件更新
	 */
	@Override
	public Integer updateByParam(CourseAssessmentSubmit bean, CourseAssessmentSubmitQuery param) {
		StringTools.checkParam(param);
		return this.courseAssessmentSubmitMapper.updateByParam(bean, param);
	}

	/**
	 * 多条件删除
	 */
	@Override
	public Integer deleteByParam(CourseAssessmentSubmitQuery param) {
		StringTools.checkParam(param);
		return this.courseAssessmentSubmitMapper.deleteByParam(param);
	}

	/**
	 * 根据SubmitId获取对象
	 */
	@Override
	public CourseAssessmentSubmit getCourseAssessmentSubmitBySubmitId(Long submitId) {
		return this.courseAssessmentSubmitMapper.selectBySubmitId(submitId);
	}

	/**
	 * 根据SubmitId修改
	 */
	@Override
	public Integer updateCourseAssessmentSubmitBySubmitId(CourseAssessmentSubmit bean, Long submitId) {
		return this.courseAssessmentSubmitMapper.updateBySubmitId(bean, submitId);
	}

	/**
	 * 根据SubmitId删除
	 */
	@Override
	public Integer deleteCourseAssessmentSubmitBySubmitId(Long submitId) {
		return this.courseAssessmentSubmitMapper.deleteBySubmitId(submitId);
	}

	/**
	 * 根据TaskIdAndUserIdAndPaperId获取对象
	 */
	@Override
	public CourseAssessmentSubmit getCourseAssessmentSubmitByTaskIdAndUserIdAndPaperId(String taskId, Integer userId, String paperId) {
		return this.courseAssessmentSubmitMapper.selectByTaskIdAndUserIdAndPaperId(taskId, userId, paperId);
	}

	/**
	 * 根据TaskIdAndUserIdAndPaperId修改
	 */
	@Override
	public Integer updateCourseAssessmentSubmitByTaskIdAndUserIdAndPaperId(CourseAssessmentSubmit bean, String taskId, Integer userId, String paperId) {
		return this.courseAssessmentSubmitMapper.updateByTaskIdAndUserIdAndPaperId(bean, taskId, userId, paperId);
	}

	/**
	 * 根据TaskIdAndUserIdAndPaperId删除
	 */
	@Override
	public Integer deleteCourseAssessmentSubmitByTaskIdAndUserIdAndPaperId(String taskId, Integer userId, String paperId) {
		return this.courseAssessmentSubmitMapper.deleteByTaskIdAndUserIdAndPaperId(taskId, userId, paperId);
	}
}