package com.smart.campus.service.impl;

import java.util.List;

import jakarta.annotation.Resource;

import org.springframework.stereotype.Service;

import com.smart.campus.entity.enums.PageSize;
import com.smart.campus.entity.query.CourseStudyLogQuery;
import com.smart.campus.entity.po.CourseStudyLog;
import com.smart.campus.entity.vo.PaginationResultVO;
import com.smart.campus.entity.query.SimplePage;
import com.smart.campus.mappers.CourseStudyLogMapper;
import com.smart.campus.service.CourseStudyLogService;
import com.smart.campus.utils.StringTools;


/**
 * 学生学习流水表 业务接口实现
 */
@Service("courseStudyLogService")
public class CourseStudyLogServiceImpl implements CourseStudyLogService {

	@Resource
	private CourseStudyLogMapper<CourseStudyLog, CourseStudyLogQuery> courseStudyLogMapper;

	/**
	 * 根据条件查询列表
	 */
	@Override
	public List<CourseStudyLog> findListByParam(CourseStudyLogQuery param) {
		return this.courseStudyLogMapper.selectList(param);
	}

	/**
	 * 根据条件查询列表
	 */
	@Override
	public Integer findCountByParam(CourseStudyLogQuery param) {
		return this.courseStudyLogMapper.selectCount(param);
	}

	/**
	 * 分页查询方法
	 */
	@Override
	public PaginationResultVO<CourseStudyLog> findListByPage(CourseStudyLogQuery param) {
		int count = this.findCountByParam(param);
		int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

		SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
		param.setSimplePage(page);
		List<CourseStudyLog> list = this.findListByParam(param);
		PaginationResultVO<CourseStudyLog> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result;
	}

	/**
	 * 新增
	 */
	@Override
	public Integer add(CourseStudyLog bean) {
		return this.courseStudyLogMapper.insert(bean);
	}

	/**
	 * 批量新增
	 */
	@Override
	public Integer addBatch(List<CourseStudyLog> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.courseStudyLogMapper.insertBatch(listBean);
	}

	/**
	 * 批量新增或者修改
	 */
	@Override
	public Integer addOrUpdateBatch(List<CourseStudyLog> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.courseStudyLogMapper.insertOrUpdateBatch(listBean);
	}

	/**
	 * 多条件更新
	 */
	@Override
	public Integer updateByParam(CourseStudyLog bean, CourseStudyLogQuery param) {
		StringTools.checkParam(param);
		return this.courseStudyLogMapper.updateByParam(bean, param);
	}

	/**
	 * 多条件删除
	 */
	@Override
	public Integer deleteByParam(CourseStudyLogQuery param) {
		StringTools.checkParam(param);
		return this.courseStudyLogMapper.deleteByParam(param);
	}

	/**
	 * 根据Id获取对象
	 */
	@Override
	public CourseStudyLog getCourseStudyLogById(Long id) {
		return this.courseStudyLogMapper.selectById(id);
	}

	/**
	 * 根据Id修改
	 */
	@Override
	public Integer updateCourseStudyLogById(CourseStudyLog bean, Long id) {
		return this.courseStudyLogMapper.updateById(bean, id);
	}

	/**
	 * 根据Id删除
	 */
	@Override
	public Integer deleteCourseStudyLogById(Long id) {
		return this.courseStudyLogMapper.deleteById(id);
	}

	/**
	 * 根据SessionId获取对象
	 */
	@Override
	public CourseStudyLog getCourseStudyLogBySessionId(String sessionId) {
		return this.courseStudyLogMapper.selectBySessionId(sessionId);
	}

	/**
	 * 根据SessionId修改
	 */
	@Override
	public Integer updateCourseStudyLogBySessionId(CourseStudyLog bean, String sessionId) {
		return this.courseStudyLogMapper.updateBySessionId(bean, sessionId);
	}

	/**
	 * 根据SessionId删除
	 */
	@Override
	public Integer deleteCourseStudyLogBySessionId(String sessionId) {
		return this.courseStudyLogMapper.deleteBySessionId(sessionId);
	}
}