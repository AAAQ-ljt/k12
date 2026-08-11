package com.smart.campus.service.impl;

import java.util.List;

import jakarta.annotation.Resource;

import org.springframework.stereotype.Service;

import com.smart.campus.entity.enums.PageSize;
import com.smart.campus.entity.query.CourseClassQuery;
import com.smart.campus.entity.po.CourseClass;
import com.smart.campus.entity.vo.PaginationResultVO;
import com.smart.campus.entity.query.SimplePage;
import com.smart.campus.mappers.CourseClassMapper;
import com.smart.campus.service.CourseClassService;
import com.smart.campus.utils.StringTools;


/**
 * 课程班级关联表 业务接口实现
 */
@Service("courseClassService")
public class CourseClassServiceImpl implements CourseClassService {

	@Resource
	private CourseClassMapper<CourseClass, CourseClassQuery> courseClassMapper;

	/**
	 * 根据条件查询列表
	 */
	@Override
	public List<CourseClass> findListByParam(CourseClassQuery param) {
		return this.courseClassMapper.selectList(param);
	}

	/**
	 * 根据条件查询列表
	 */
	@Override
	public Integer findCountByParam(CourseClassQuery param) {
		return this.courseClassMapper.selectCount(param);
	}

	/**
	 * 分页查询方法
	 */
	@Override
	public PaginationResultVO<CourseClass> findListByPage(CourseClassQuery param) {
		int count = this.findCountByParam(param);
		int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

		SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
		param.setSimplePage(page);
		List<CourseClass> list = this.findListByParam(param);
		PaginationResultVO<CourseClass> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result;
	}

	/**
	 * 新增
	 */
	@Override
	public Integer add(CourseClass bean) {
		return this.courseClassMapper.insert(bean);
	}

	/**
	 * 批量新增
	 */
	@Override
	public Integer addBatch(List<CourseClass> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.courseClassMapper.insertBatch(listBean);
	}

	/**
	 * 批量新增或者修改
	 */
	@Override
	public Integer addOrUpdateBatch(List<CourseClass> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.courseClassMapper.insertOrUpdateBatch(listBean);
	}

	/**
	 * 多条件更新
	 */
	@Override
	public Integer updateByParam(CourseClass bean, CourseClassQuery param) {
		StringTools.checkParam(param);
		return this.courseClassMapper.updateByParam(bean, param);
	}

	/**
	 * 多条件删除
	 */
	@Override
	public Integer deleteByParam(CourseClassQuery param) {
		StringTools.checkParam(param);
		return this.courseClassMapper.deleteByParam(param);
	}

	/**
	 * 根据CourseIdAndClassId获取对象
	 */
	@Override
	public CourseClass getCourseClassByCourseIdAndClassId(String courseId, Integer classId) {
		return this.courseClassMapper.selectByCourseIdAndClassId(courseId, classId);
	}

	/**
	 * 根据CourseIdAndClassId修改
	 */
	@Override
	public Integer updateCourseClassByCourseIdAndClassId(CourseClass bean, String courseId, Integer classId) {
		return this.courseClassMapper.updateByCourseIdAndClassId(bean, courseId, classId);
	}

	/**
	 * 根据CourseIdAndClassId删除
	 */
	@Override
	public Integer deleteCourseClassByCourseIdAndClassId(String courseId, Integer classId) {
		return this.courseClassMapper.deleteByCourseIdAndClassId(courseId, classId);
	}
}