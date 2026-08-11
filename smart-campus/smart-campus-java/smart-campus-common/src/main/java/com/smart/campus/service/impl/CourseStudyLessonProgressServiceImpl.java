package com.smart.campus.service.impl;

import java.util.List;

import jakarta.annotation.Resource;

import org.springframework.stereotype.Service;

import com.smart.campus.entity.enums.PageSize;
import com.smart.campus.entity.query.CourseStudyLessonProgressQuery;
import com.smart.campus.entity.po.CourseStudyLessonProgress;
import com.smart.campus.entity.vo.PaginationResultVO;
import com.smart.campus.entity.query.SimplePage;
import com.smart.campus.mappers.CourseStudyLessonProgressMapper;
import com.smart.campus.service.CourseStudyLessonProgressService;
import com.smart.campus.utils.StringTools;


/**
 * 学生课时学习进度表 业务接口实现
 */
@Service("courseStudyLessonProgressService")
public class CourseStudyLessonProgressServiceImpl implements CourseStudyLessonProgressService {

	@Resource
	private CourseStudyLessonProgressMapper<CourseStudyLessonProgress, CourseStudyLessonProgressQuery> courseStudyLessonProgressMapper;

	/**
	 * 根据条件查询列表
	 */
	@Override
	public List<CourseStudyLessonProgress> findListByParam(CourseStudyLessonProgressQuery param) {
		return this.courseStudyLessonProgressMapper.selectList(param);
	}

	/**
	 * 根据条件查询列表
	 */
	@Override
	public Integer findCountByParam(CourseStudyLessonProgressQuery param) {
		return this.courseStudyLessonProgressMapper.selectCount(param);
	}

	/**
	 * 分页查询方法
	 */
	@Override
	public PaginationResultVO<CourseStudyLessonProgress> findListByPage(CourseStudyLessonProgressQuery param) {
		int count = this.findCountByParam(param);
		int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

		SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
		param.setSimplePage(page);
		List<CourseStudyLessonProgress> list = this.findListByParam(param);
		PaginationResultVO<CourseStudyLessonProgress> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result;
	}

	/**
	 * 新增
	 */
	@Override
	public Integer add(CourseStudyLessonProgress bean) {
		return this.courseStudyLessonProgressMapper.insert(bean);
	}

	/**
	 * 批量新增
	 */
	@Override
	public Integer addBatch(List<CourseStudyLessonProgress> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.courseStudyLessonProgressMapper.insertBatch(listBean);
	}

	/**
	 * 批量新增或者修改
	 */
	@Override
	public Integer addOrUpdateBatch(List<CourseStudyLessonProgress> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.courseStudyLessonProgressMapper.insertOrUpdateBatch(listBean);
	}

	/**
	 * 多条件更新
	 */
	@Override
	public Integer updateByParam(CourseStudyLessonProgress bean, CourseStudyLessonProgressQuery param) {
		StringTools.checkParam(param);
		return this.courseStudyLessonProgressMapper.updateByParam(bean, param);
	}

	/**
	 * 多条件删除
	 */
	@Override
	public Integer deleteByParam(CourseStudyLessonProgressQuery param) {
		StringTools.checkParam(param);
		return this.courseStudyLessonProgressMapper.deleteByParam(param);
	}

	/**
	 * 根据Id获取对象
	 */
	@Override
	public CourseStudyLessonProgress getCourseStudyLessonProgressById(Long id) {
		return this.courseStudyLessonProgressMapper.selectById(id);
	}

	/**
	 * 根据Id修改
	 */
	@Override
	public Integer updateCourseStudyLessonProgressById(CourseStudyLessonProgress bean, Long id) {
		return this.courseStudyLessonProgressMapper.updateById(bean, id);
	}

	/**
	 * 根据Id删除
	 */
	@Override
	public Integer deleteCourseStudyLessonProgressById(Long id) {
		return this.courseStudyLessonProgressMapper.deleteById(id);
	}

	/**
	 * 根据UserIdAndLessonId获取对象
	 */
	@Override
	public CourseStudyLessonProgress getCourseStudyLessonProgressByUserIdAndLessonId(Integer userId, String lessonId) {
		return this.courseStudyLessonProgressMapper.selectByUserIdAndLessonId(userId, lessonId);
	}

	/**
	 * 根据UserIdAndLessonId修改
	 */
	@Override
	public Integer updateCourseStudyLessonProgressByUserIdAndLessonId(CourseStudyLessonProgress bean, Integer userId, String lessonId) {
		return this.courseStudyLessonProgressMapper.updateByUserIdAndLessonId(bean, userId, lessonId);
	}

	/**
	 * 根据UserIdAndLessonId删除
	 */
	@Override
	public Integer deleteCourseStudyLessonProgressByUserIdAndLessonId(Integer userId, String lessonId) {
		return this.courseStudyLessonProgressMapper.deleteByUserIdAndLessonId(userId, lessonId);
	}
}