package com.smart.campus.service.impl;

import java.util.List;

import jakarta.annotation.Resource;

import org.springframework.stereotype.Service;

import com.smart.campus.entity.enums.PageSize;
import com.smart.campus.entity.query.CourseUserCollectionQuery;
import com.smart.campus.entity.po.CourseUserCollection;
import com.smart.campus.entity.vo.PaginationResultVO;
import com.smart.campus.entity.query.SimplePage;
import com.smart.campus.mappers.CourseUserCollectionMapper;
import com.smart.campus.service.CourseUserCollectionService;
import com.smart.campus.utils.StringTools;


/**
 * 课程收藏 业务接口实现
 */
@Service("courseUserCollectionService")
public class CourseUserCollectionServiceImpl implements CourseUserCollectionService {

	@Resource
	private CourseUserCollectionMapper<CourseUserCollection, CourseUserCollectionQuery> courseUserCollectionMapper;

	/**
	 * 根据条件查询列表
	 */
	@Override
	public List<CourseUserCollection> findListByParam(CourseUserCollectionQuery param) {
		return this.courseUserCollectionMapper.selectList(param);
	}

	/**
	 * 根据条件查询列表
	 */
	@Override
	public Integer findCountByParam(CourseUserCollectionQuery param) {
		return this.courseUserCollectionMapper.selectCount(param);
	}

	/**
	 * 分页查询方法
	 */
	@Override
	public PaginationResultVO<CourseUserCollection> findListByPage(CourseUserCollectionQuery param) {
		int count = this.findCountByParam(param);
		int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

		SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
		param.setSimplePage(page);
		List<CourseUserCollection> list = this.findListByParam(param);
		PaginationResultVO<CourseUserCollection> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result;
	}

	/**
	 * 新增
	 */
	@Override
	public Integer add(CourseUserCollection bean) {
		return this.courseUserCollectionMapper.insert(bean);
	}

	/**
	 * 批量新增
	 */
	@Override
	public Integer addBatch(List<CourseUserCollection> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.courseUserCollectionMapper.insertBatch(listBean);
	}

	/**
	 * 批量新增或者修改
	 */
	@Override
	public Integer addOrUpdateBatch(List<CourseUserCollection> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.courseUserCollectionMapper.insertOrUpdateBatch(listBean);
	}

	/**
	 * 多条件更新
	 */
	@Override
	public Integer updateByParam(CourseUserCollection bean, CourseUserCollectionQuery param) {
		StringTools.checkParam(param);
		return this.courseUserCollectionMapper.updateByParam(bean, param);
	}

	/**
	 * 多条件删除
	 */
	@Override
	public Integer deleteByParam(CourseUserCollectionQuery param) {
		StringTools.checkParam(param);
		return this.courseUserCollectionMapper.deleteByParam(param);
	}

	/**
	 * 根据CourseIdAndUserId获取对象
	 */
	@Override
	public CourseUserCollection getCourseUserCollectionByCourseIdAndUserId(String courseId, Integer userId) {
		return this.courseUserCollectionMapper.selectByCourseIdAndUserId(courseId, userId);
	}

	/**
	 * 根据CourseIdAndUserId修改
	 */
	@Override
	public Integer updateCourseUserCollectionByCourseIdAndUserId(CourseUserCollection bean, String courseId, Integer userId) {
		return this.courseUserCollectionMapper.updateByCourseIdAndUserId(bean, courseId, userId);
	}

	/**
	 * 根据CourseIdAndUserId删除
	 */
	@Override
	public Integer deleteCourseUserCollectionByCourseIdAndUserId(String courseId, Integer userId) {
		return this.courseUserCollectionMapper.deleteByCourseIdAndUserId(courseId, userId);
	}
}