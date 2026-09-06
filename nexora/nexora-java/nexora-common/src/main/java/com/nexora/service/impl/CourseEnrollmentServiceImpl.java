package com.nexora.service.impl;

import java.util.List;

import jakarta.annotation.Resource;

import org.springframework.stereotype.Service;

import com.nexora.entity.enums.PageSize;
import com.nexora.entity.query.CourseEnrollmentQuery;
import com.nexora.entity.po.CourseEnrollment;
import com.nexora.entity.vo.PaginationResultVO;
import com.nexora.entity.query.SimplePage;
import com.nexora.mappers.CourseEnrollmentMapper;
import com.nexora.service.CourseEnrollmentService;
import com.nexora.utils.StringTools;


/**
 * 学生加入课程记录表 业务接口实现
 */
@Service("courseEnrollmentService")
public class CourseEnrollmentServiceImpl implements CourseEnrollmentService {

	@Resource
	private CourseEnrollmentMapper<CourseEnrollment, CourseEnrollmentQuery> courseEnrollmentMapper;

	/**
	 * 根据条件查询列表
	 */
	@Override
	public List<CourseEnrollment> findListByParam(CourseEnrollmentQuery param) {
		return this.courseEnrollmentMapper.selectList(param);
	}

	/**
	 * 根据条件查询列表
	 */
	@Override
	public Integer findCountByParam(CourseEnrollmentQuery param) {
		return this.courseEnrollmentMapper.selectCount(param);
	}

	/**
	 * 分页查询方法
	 */
	@Override
	public PaginationResultVO<CourseEnrollment> findListByPage(CourseEnrollmentQuery param) {
		int count = this.findCountByParam(param);
		int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

		SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
		param.setSimplePage(page);
		List<CourseEnrollment> list = this.findListByParam(param);
		PaginationResultVO<CourseEnrollment> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result;
	}

	/**
	 * 新增
	 */
	@Override
	public Integer add(CourseEnrollment bean) {
		return this.courseEnrollmentMapper.insert(bean);
	}

	/**
	 * 批量新增
	 */
	@Override
	public Integer addBatch(List<CourseEnrollment> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.courseEnrollmentMapper.insertBatch(listBean);
	}

	/**
	 * 批量新增或者修改
	 */
	@Override
	public Integer addOrUpdateBatch(List<CourseEnrollment> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.courseEnrollmentMapper.insertOrUpdateBatch(listBean);
	}

	/**
	 * 多条件更新
	 */
	@Override
	public Integer updateByParam(CourseEnrollment bean, CourseEnrollmentQuery param) {
		StringTools.checkParam(param);
		return this.courseEnrollmentMapper.updateByParam(bean, param);
	}

	/**
	 * 多条件删除
	 */
	@Override
	public Integer deleteByParam(CourseEnrollmentQuery param) {
		StringTools.checkParam(param);
		return this.courseEnrollmentMapper.deleteByParam(param);
	}

	/**
	 * 根据Id获取对象
	 */
	@Override
	public CourseEnrollment getCourseEnrollmentById(Long id) {
		return this.courseEnrollmentMapper.selectById(id);
	}

	/**
	 * 根据Id修改
	 */
	@Override
	public Integer updateCourseEnrollmentById(CourseEnrollment bean, Long id) {
		return this.courseEnrollmentMapper.updateById(bean, id);
	}

	/**
	 * 根据Id删除
	 */
	@Override
	public Integer deleteCourseEnrollmentById(Long id) {
		return this.courseEnrollmentMapper.deleteById(id);
	}

	/**
	 * 根据UserIdAndCourseId获取对象
	 */
	@Override
	public CourseEnrollment getCourseEnrollmentByUserIdAndCourseId(String userId, String courseId) {
		return this.courseEnrollmentMapper.selectByUserIdAndCourseId(userId, courseId);
	}

	/**
	 * 根据UserIdAndCourseId修改
	 */
	@Override
	public Integer updateCourseEnrollmentByUserIdAndCourseId(CourseEnrollment bean, String userId, String courseId) {
		return this.courseEnrollmentMapper.updateByUserIdAndCourseId(bean, userId, courseId);
	}

	/**
	 * 根据UserIdAndCourseId删除
	 */
	@Override
	public Integer deleteCourseEnrollmentByUserIdAndCourseId(String userId, String courseId) {
		return this.courseEnrollmentMapper.deleteByUserIdAndCourseId(userId, courseId);
	}

}
