package com.nexora.service;

import java.util.List;

import com.nexora.entity.query.CourseChapterLessonResourceQuery;
import com.nexora.entity.po.CourseChapterLessonResource;
import com.nexora.entity.vo.PaginationResultVO;


/**
 * 课时资源关联表 业务接口
 */
public interface CourseChapterLessonResourceService {

	/**
	 * 根据条件查询列表
	 */
	List<CourseChapterLessonResource> findListByParam(CourseChapterLessonResourceQuery param);

	/**
	 * 根据条件查询列表
	 */
	Integer findCountByParam(CourseChapterLessonResourceQuery param);

	/**
	 * 分页查询
	 */
	PaginationResultVO<CourseChapterLessonResource> findListByPage(CourseChapterLessonResourceQuery param);

	/**
	 * 新增
	 */
	Integer add(CourseChapterLessonResource bean);

	/**
	 * 批量新增
	 */
	Integer addBatch(List<CourseChapterLessonResource> listBean);

	/**
	 * 批量新增/修改
	 */
	Integer addOrUpdateBatch(List<CourseChapterLessonResource> listBean);

	/**
	 * 多条件更新
	 */
	Integer updateByParam(CourseChapterLessonResource bean,CourseChapterLessonResourceQuery param);

	/**
	 * 多条件删除
	 */
	Integer deleteByParam(CourseChapterLessonResourceQuery param);

	/**
	 * 根据Id查询对象
	 */
	CourseChapterLessonResource getCourseChapterLessonResourceById(Integer id);


	/**
	 * 根据Id修改
	 */
	Integer updateCourseChapterLessonResourceById(CourseChapterLessonResource bean,Integer id);


	/**
	 * 根据Id删除
	 */
	Integer deleteCourseChapterLessonResourceById(Integer id);

}