package com.nexora.service;

import java.util.List;

import com.nexora.entity.query.CourseChapterQuery;
import com.nexora.entity.po.CourseChapter;
import com.nexora.entity.vo.PaginationResultVO;


/**
 * 章节表 业务接口
 */
public interface CourseChapterService {

	/**
	 * 根据条件查询列表
	 */
	List<CourseChapter> findListByParam(CourseChapterQuery param);

	/**
	 * 根据条件查询列表
	 */
	Integer findCountByParam(CourseChapterQuery param);

	/**
	 * 分页查询
	 */
	PaginationResultVO<CourseChapter> findListByPage(CourseChapterQuery param);

	/**
	 * 新增
	 */
	Integer add(CourseChapter bean);

	/**
	 * 批量新增
	 */
	Integer addBatch(List<CourseChapter> listBean);

	/**
	 * 批量新增/修改
	 */
	Integer addOrUpdateBatch(List<CourseChapter> listBean);

	/**
	 * 多条件更新
	 */
	Integer updateByParam(CourseChapter bean,CourseChapterQuery param);

	/**
	 * 多条件删除
	 */
	Integer deleteByParam(CourseChapterQuery param);

	/**
	 * 根据ChapterId查询对象
	 */
	CourseChapter getCourseChapterByChapterId(String chapterId);


	/**
	 * 根据ChapterId修改
	 */
	Integer updateCourseChapterByChapterId(CourseChapter bean,String chapterId);


	/**
	 * 根据ChapterId删除
	 */
	Integer deleteCourseChapterByChapterId(String chapterId);

}