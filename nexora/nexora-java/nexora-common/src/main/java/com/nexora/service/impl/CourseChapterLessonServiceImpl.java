package com.nexora.service.impl;

import java.util.List;

import jakarta.annotation.Resource;

import org.springframework.stereotype.Service;

import com.nexora.entity.enums.PageSize;
import com.nexora.entity.query.CourseChapterLessonQuery;
import com.nexora.entity.po.CourseChapterLesson;
import com.nexora.entity.vo.PaginationResultVO;
import com.nexora.entity.query.SimplePage;
import com.nexora.mappers.CourseChapterLessonMapper;
import com.nexora.service.CourseChapterLessonService;
import com.nexora.utils.StringTools;


/**
 * 课时表 业务接口实现
 */
@Service("courseChapterLessonService")
public class CourseChapterLessonServiceImpl implements CourseChapterLessonService {

	@Resource
	private CourseChapterLessonMapper<CourseChapterLesson, CourseChapterLessonQuery> courseChapterLessonMapper;

	/**
	 * 根据条件查询列表
	 */
	@Override
	public List<CourseChapterLesson> findListByParam(CourseChapterLessonQuery param) {
		return this.courseChapterLessonMapper.selectList(param);
	}

	/**
	 * 根据条件查询列表
	 */
	@Override
	public Integer findCountByParam(CourseChapterLessonQuery param) {
		return this.courseChapterLessonMapper.selectCount(param);
	}

	/**
	 * 分页查询方法
	 */
	@Override
	public PaginationResultVO<CourseChapterLesson> findListByPage(CourseChapterLessonQuery param) {
		int count = this.findCountByParam(param);
		int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

		SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
		param.setSimplePage(page);
		List<CourseChapterLesson> list = this.findListByParam(param);
		PaginationResultVO<CourseChapterLesson> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result;
	}

	/**
	 * 新增
	 */
	@Override
	public Integer add(CourseChapterLesson bean) {
		return this.courseChapterLessonMapper.insert(bean);
	}

	/**
	 * 批量新增
	 */
	@Override
	public Integer addBatch(List<CourseChapterLesson> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.courseChapterLessonMapper.insertBatch(listBean);
	}

	/**
	 * 批量新增或者修改
	 */
	@Override
	public Integer addOrUpdateBatch(List<CourseChapterLesson> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.courseChapterLessonMapper.insertOrUpdateBatch(listBean);
	}

	/**
	 * 多条件更新
	 */
	@Override
	public Integer updateByParam(CourseChapterLesson bean, CourseChapterLessonQuery param) {
		StringTools.checkParam(param);
		return this.courseChapterLessonMapper.updateByParam(bean, param);
	}

	/**
	 * 多条件删除
	 */
	@Override
	public Integer deleteByParam(CourseChapterLessonQuery param) {
		StringTools.checkParam(param);
		return this.courseChapterLessonMapper.deleteByParam(param);
	}

	/**
	 * 根据LessonId获取对象
	 */
	@Override
	public CourseChapterLesson getCourseChapterLessonByLessonId(String lessonId) {
		return this.courseChapterLessonMapper.selectByLessonId(lessonId);
	}

	/**
	 * 根据LessonId修改
	 */
	@Override
	public Integer updateCourseChapterLessonByLessonId(CourseChapterLesson bean, String lessonId) {
		return this.courseChapterLessonMapper.updateByLessonId(bean, lessonId);
	}

	/**
	 * 根据LessonId删除
	 */
	@Override
	public Integer deleteCourseChapterLessonByLessonId(String lessonId) {
		return this.courseChapterLessonMapper.deleteByLessonId(lessonId);
	}
}