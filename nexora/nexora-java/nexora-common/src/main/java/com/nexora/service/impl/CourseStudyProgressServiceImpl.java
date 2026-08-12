package com.nexora.service.impl;

import java.util.List;

import jakarta.annotation.Resource;

import org.springframework.stereotype.Service;

import com.nexora.entity.enums.PageSize;
import com.nexora.entity.query.CourseStudyProgressQuery;
import com.nexora.entity.po.CourseStudyProgress;
import com.nexora.entity.vo.PaginationResultVO;
import com.nexora.entity.query.SimplePage;
import com.nexora.mappers.CourseStudyProgressMapper;
import com.nexora.service.CourseStudyProgressService;
import com.nexora.utils.StringTools;


/**
 * 课程学习进度表 业务接口实现
 */
@Service("courseStudyProgressService")
public class CourseStudyProgressServiceImpl implements CourseStudyProgressService {

	@Resource
	private CourseStudyProgressMapper<CourseStudyProgress, CourseStudyProgressQuery> courseStudyProgressMapper;

	/**
	 * 根据条件查询列表
	 */
	@Override
	public List<CourseStudyProgress> findListByParam(CourseStudyProgressQuery param) {
		return this.courseStudyProgressMapper.selectList(param);
	}

	/**
	 * 根据条件查询列表
	 */
	@Override
	public Integer findCountByParam(CourseStudyProgressQuery param) {
		return this.courseStudyProgressMapper.selectCount(param);
	}

	/**
	 * 分页查询方法
	 */
	@Override
	public PaginationResultVO<CourseStudyProgress> findListByPage(CourseStudyProgressQuery param) {
		int count = this.findCountByParam(param);
		int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

		SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
		param.setSimplePage(page);
		List<CourseStudyProgress> list = this.findListByParam(param);
		PaginationResultVO<CourseStudyProgress> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result;
	}

	/**
	 * 新增
	 */
	@Override
	public Integer add(CourseStudyProgress bean) {
		return this.courseStudyProgressMapper.insert(bean);
	}

	/**
	 * 批量新增
	 */
	@Override
	public Integer addBatch(List<CourseStudyProgress> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.courseStudyProgressMapper.insertBatch(listBean);
	}

	/**
	 * 批量新增或者修改
	 */
	@Override
	public Integer addOrUpdateBatch(List<CourseStudyProgress> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.courseStudyProgressMapper.insertOrUpdateBatch(listBean);
	}

	/**
	 * 多条件更新
	 */
	@Override
	public Integer updateByParam(CourseStudyProgress bean, CourseStudyProgressQuery param) {
		StringTools.checkParam(param);
		return this.courseStudyProgressMapper.updateByParam(bean, param);
	}

	/**
	 * 多条件删除
	 */
	@Override
	public Integer deleteByParam(CourseStudyProgressQuery param) {
		StringTools.checkParam(param);
		return this.courseStudyProgressMapper.deleteByParam(param);
	}

	/**
	 * 根据Id获取对象
	 */
	@Override
	public CourseStudyProgress getCourseStudyProgressById(Integer id) {
		return this.courseStudyProgressMapper.selectById(id);
	}

	/**
	 * 根据Id修改
	 */
	@Override
	public Integer updateCourseStudyProgressById(CourseStudyProgress bean, Integer id) {
		return this.courseStudyProgressMapper.updateById(bean, id);
	}

	/**
	 * 根据Id删除
	 */
	@Override
	public Integer deleteCourseStudyProgressById(Integer id) {
		return this.courseStudyProgressMapper.deleteById(id);
	}

	/**
	 * 根据UserIdAndCourseId获取对象
	 */
	@Override
	public CourseStudyProgress getCourseStudyProgressByUserIdAndCourseId(String userId, String courseId) {
		return this.courseStudyProgressMapper.selectByUserIdAndCourseId(userId, courseId);
	}

	/**
	 * 根据UserIdAndCourseId修改
	 */
	@Override
	public Integer updateCourseStudyProgressByUserIdAndCourseId(CourseStudyProgress bean, String userId, String courseId) {
		return this.courseStudyProgressMapper.updateByUserIdAndCourseId(bean, userId, courseId);
	}

	/**
	 * 根据UserIdAndCourseId删除
	 */
	@Override
	public Integer deleteCourseStudyProgressByUserIdAndCourseId(String userId, String courseId) {
		return this.courseStudyProgressMapper.deleteByUserIdAndCourseId(userId, courseId);
	}
}