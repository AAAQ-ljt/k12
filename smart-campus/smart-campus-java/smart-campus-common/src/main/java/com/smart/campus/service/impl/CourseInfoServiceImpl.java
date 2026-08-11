package com.smart.campus.service.impl;

import java.util.List;

import jakarta.annotation.Resource;

import org.springframework.stereotype.Service;

import com.smart.campus.entity.enums.PageSize;
import com.smart.campus.entity.query.CourseInfoQuery;
import com.smart.campus.entity.po.CourseInfo;
import com.smart.campus.entity.vo.PaginationResultVO;
import com.smart.campus.entity.query.SimplePage;
import com.smart.campus.mappers.CourseInfoMapper;
import com.smart.campus.service.CourseInfoService;
import com.smart.campus.utils.StringTools;


/**
 * 课程表 业务接口实现
 */
@Service("courseInfoService")
public class CourseInfoServiceImpl implements CourseInfoService {

	@Resource
	private CourseInfoMapper<CourseInfo, CourseInfoQuery> courseInfoMapper;

	/**
	 * 根据条件查询列表
	 */
	@Override
	public List<CourseInfo> findListByParam(CourseInfoQuery param) {
		return this.courseInfoMapper.selectList(param);
	}

	/**
	 * 根据条件查询列表
	 */
	@Override
	public Integer findCountByParam(CourseInfoQuery param) {
		return this.courseInfoMapper.selectCount(param);
	}

	/**
	 * 分页查询方法
	 */
	@Override
	public PaginationResultVO<CourseInfo> findListByPage(CourseInfoQuery param) {
		int count = this.findCountByParam(param);
		int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

		SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
		param.setSimplePage(page);
		List<CourseInfo> list = this.findListByParam(param);
		PaginationResultVO<CourseInfo> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result;
	}

	/**
	 * 新增
	 */
	@Override
	public Integer add(CourseInfo bean) {
		return this.courseInfoMapper.insert(bean);
	}

	/**
	 * 批量新增
	 */
	@Override
	public Integer addBatch(List<CourseInfo> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.courseInfoMapper.insertBatch(listBean);
	}

	/**
	 * 批量新增或者修改
	 */
	@Override
	public Integer addOrUpdateBatch(List<CourseInfo> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.courseInfoMapper.insertOrUpdateBatch(listBean);
	}

	/**
	 * 多条件更新
	 */
	@Override
	public Integer updateByParam(CourseInfo bean, CourseInfoQuery param) {
		StringTools.checkParam(param);
		return this.courseInfoMapper.updateByParam(bean, param);
	}

	/**
	 * 多条件删除
	 */
	@Override
	public Integer deleteByParam(CourseInfoQuery param) {
		StringTools.checkParam(param);
		return this.courseInfoMapper.deleteByParam(param);
	}

	/**
	 * 根据CourseId获取对象
	 */
	@Override
	public CourseInfo getCourseInfoByCourseId(String courseId) {
		return this.courseInfoMapper.selectByCourseId(courseId);
	}

	/**
	 * 根据CourseId修改
	 */
	@Override
	public Integer updateCourseInfoByCourseId(CourseInfo bean, String courseId) {
		return this.courseInfoMapper.updateByCourseId(bean, courseId);
	}

	/**
	 * 根据CourseId删除
	 */
	@Override
	public Integer deleteCourseInfoByCourseId(String courseId) {
		return this.courseInfoMapper.deleteByCourseId(courseId);
	}
}