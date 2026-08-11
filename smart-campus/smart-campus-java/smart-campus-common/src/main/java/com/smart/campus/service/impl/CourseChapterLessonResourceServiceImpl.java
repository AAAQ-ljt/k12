package com.smart.campus.service.impl;

import java.util.List;

import jakarta.annotation.Resource;

import org.springframework.stereotype.Service;

import com.smart.campus.entity.enums.PageSize;
import com.smart.campus.entity.query.CourseChapterLessonResourceQuery;
import com.smart.campus.entity.po.CourseChapterLessonResource;
import com.smart.campus.entity.vo.PaginationResultVO;
import com.smart.campus.entity.query.SimplePage;
import com.smart.campus.mappers.CourseChapterLessonResourceMapper;
import com.smart.campus.service.CourseChapterLessonResourceService;
import com.smart.campus.utils.StringTools;


/**
 * 课时资源关联表 业务接口实现
 */
@Service("courseChapterLessonResourceService")
public class CourseChapterLessonResourceServiceImpl implements CourseChapterLessonResourceService {

	@Resource
	private CourseChapterLessonResourceMapper<CourseChapterLessonResource, CourseChapterLessonResourceQuery> courseChapterLessonResourceMapper;

	/**
	 * 根据条件查询列表
	 */
	@Override
	public List<CourseChapterLessonResource> findListByParam(CourseChapterLessonResourceQuery param) {
		return this.courseChapterLessonResourceMapper.selectList(param);
	}

	/**
	 * 根据条件查询列表
	 */
	@Override
	public Integer findCountByParam(CourseChapterLessonResourceQuery param) {
		return this.courseChapterLessonResourceMapper.selectCount(param);
	}

	/**
	 * 分页查询方法
	 */
	@Override
	public PaginationResultVO<CourseChapterLessonResource> findListByPage(CourseChapterLessonResourceQuery param) {
		int count = this.findCountByParam(param);
		int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

		SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
		param.setSimplePage(page);
		List<CourseChapterLessonResource> list = this.findListByParam(param);
		PaginationResultVO<CourseChapterLessonResource> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result;
	}

	/**
	 * 新增
	 */
	@Override
	public Integer add(CourseChapterLessonResource bean) {
		return this.courseChapterLessonResourceMapper.insert(bean);
	}

	/**
	 * 批量新增
	 */
	@Override
	public Integer addBatch(List<CourseChapterLessonResource> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.courseChapterLessonResourceMapper.insertBatch(listBean);
	}

	/**
	 * 批量新增或者修改
	 */
	@Override
	public Integer addOrUpdateBatch(List<CourseChapterLessonResource> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.courseChapterLessonResourceMapper.insertOrUpdateBatch(listBean);
	}

	/**
	 * 多条件更新
	 */
	@Override
	public Integer updateByParam(CourseChapterLessonResource bean, CourseChapterLessonResourceQuery param) {
		StringTools.checkParam(param);
		return this.courseChapterLessonResourceMapper.updateByParam(bean, param);
	}

	/**
	 * 多条件删除
	 */
	@Override
	public Integer deleteByParam(CourseChapterLessonResourceQuery param) {
		StringTools.checkParam(param);
		return this.courseChapterLessonResourceMapper.deleteByParam(param);
	}

	/**
	 * 根据Id获取对象
	 */
	@Override
	public CourseChapterLessonResource getCourseChapterLessonResourceById(Integer id) {
		return this.courseChapterLessonResourceMapper.selectById(id);
	}

	/**
	 * 根据Id修改
	 */
	@Override
	public Integer updateCourseChapterLessonResourceById(CourseChapterLessonResource bean, Integer id) {
		return this.courseChapterLessonResourceMapper.updateById(bean, id);
	}

	/**
	 * 根据Id删除
	 */
	@Override
	public Integer deleteCourseChapterLessonResourceById(Integer id) {
		return this.courseChapterLessonResourceMapper.deleteById(id);
	}
}