package com.smart.campus.service.impl;

import java.util.List;

import jakarta.annotation.Resource;

import org.springframework.stereotype.Service;

import com.smart.campus.entity.enums.PageSize;
import com.smart.campus.entity.query.CourseChapterQuery;
import com.smart.campus.entity.po.CourseChapter;
import com.smart.campus.entity.vo.PaginationResultVO;
import com.smart.campus.entity.query.SimplePage;
import com.smart.campus.mappers.CourseChapterMapper;
import com.smart.campus.service.CourseChapterService;
import com.smart.campus.utils.StringTools;


/**
 * 课程章节表 业务接口实现
 */
@Service("courseChapterService")
public class CourseChapterServiceImpl implements CourseChapterService {

	@Resource
	private CourseChapterMapper<CourseChapter, CourseChapterQuery> courseChapterMapper;

	/**
	 * 根据条件查询列表
	 */
	@Override
	public List<CourseChapter> findListByParam(CourseChapterQuery param) {
		return this.courseChapterMapper.selectList(param);
	}

	/**
	 * 根据条件查询列表
	 */
	@Override
	public Integer findCountByParam(CourseChapterQuery param) {
		return this.courseChapterMapper.selectCount(param);
	}

	/**
	 * 分页查询方法
	 */
	@Override
	public PaginationResultVO<CourseChapter> findListByPage(CourseChapterQuery param) {
		int count = this.findCountByParam(param);
		int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

		SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
		param.setSimplePage(page);
		List<CourseChapter> list = this.findListByParam(param);
		PaginationResultVO<CourseChapter> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result;
	}

	/**
	 * 新增
	 */
	@Override
	public Integer add(CourseChapter bean) {
		return this.courseChapterMapper.insert(bean);
	}

	/**
	 * 批量新增
	 */
	@Override
	public Integer addBatch(List<CourseChapter> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.courseChapterMapper.insertBatch(listBean);
	}

	/**
	 * 批量新增或者修改
	 */
	@Override
	public Integer addOrUpdateBatch(List<CourseChapter> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.courseChapterMapper.insertOrUpdateBatch(listBean);
	}

	/**
	 * 多条件更新
	 */
	@Override
	public Integer updateByParam(CourseChapter bean, CourseChapterQuery param) {
		StringTools.checkParam(param);
		return this.courseChapterMapper.updateByParam(bean, param);
	}

	/**
	 * 多条件删除
	 */
	@Override
	public Integer deleteByParam(CourseChapterQuery param) {
		StringTools.checkParam(param);
		return this.courseChapterMapper.deleteByParam(param);
	}

	/**
	 * 根据ChapterId获取对象
	 */
	@Override
	public CourseChapter getCourseChapterByChapterId(String chapterId) {
		return this.courseChapterMapper.selectByChapterId(chapterId);
	}

	/**
	 * 根据ChapterId修改
	 */
	@Override
	public Integer updateCourseChapterByChapterId(CourseChapter bean, String chapterId) {
		return this.courseChapterMapper.updateByChapterId(bean, chapterId);
	}

	/**
	 * 根据ChapterId删除
	 */
	@Override
	public Integer deleteCourseChapterByChapterId(String chapterId) {
		return this.courseChapterMapper.deleteByChapterId(chapterId);
	}
}