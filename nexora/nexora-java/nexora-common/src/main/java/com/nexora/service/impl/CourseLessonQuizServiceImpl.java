package com.nexora.service.impl;

import java.util.List;

import jakarta.annotation.Resource;

import org.springframework.stereotype.Service;

import com.nexora.entity.enums.PageSize;
import com.nexora.entity.query.CourseLessonQuizQuery;
import com.nexora.entity.po.CourseLessonQuiz;
import com.nexora.entity.vo.PaginationResultVO;
import com.nexora.entity.query.SimplePage;
import com.nexora.mappers.CourseLessonQuizMapper;
import com.nexora.service.CourseLessonQuizService;
import com.nexora.utils.StringTools;

/**
 * 课时通关测验配置表 业务接口实现
 */
@Service("courseLessonQuizService")
public class CourseLessonQuizServiceImpl implements CourseLessonQuizService {

	@Resource
	private CourseLessonQuizMapper<CourseLessonQuiz, CourseLessonQuizQuery> courseLessonQuizMapper;

	@Override
	public List<CourseLessonQuiz> findListByParam(CourseLessonQuizQuery param) {
		return this.courseLessonQuizMapper.selectList(param);
	}

	@Override
	public Integer findCountByParam(CourseLessonQuizQuery param) {
		return this.courseLessonQuizMapper.selectCount(param);
	}

	@Override
	public PaginationResultVO<CourseLessonQuiz> findListByPage(CourseLessonQuizQuery param) {
		int count = this.findCountByParam(param);
		int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();
		SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
		param.setSimplePage(page);
		List<CourseLessonQuiz> list = this.findListByParam(param);
		PaginationResultVO<CourseLessonQuiz> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result;
	}

	@Override
	public Integer add(CourseLessonQuiz bean) {
		return this.courseLessonQuizMapper.insert(bean);
	}

	@Override
	public Integer addBatch(List<CourseLessonQuiz> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.courseLessonQuizMapper.insertBatch(listBean);
	}

	@Override
	public Integer addOrUpdateBatch(List<CourseLessonQuiz> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.courseLessonQuizMapper.insertOrUpdateBatch(listBean);
	}

	@Override
	public Integer updateByParam(CourseLessonQuiz bean, CourseLessonQuizQuery param) {
		StringTools.checkParam(param);
		return this.courseLessonQuizMapper.updateByParam(bean, param);
	}

	@Override
	public Integer deleteByParam(CourseLessonQuizQuery param) {
		StringTools.checkParam(param);
		return this.courseLessonQuizMapper.deleteByParam(param);
	}

	@Override
	public CourseLessonQuiz getCourseLessonQuizByLessonId(String lessonId) {
		return this.courseLessonQuizMapper.selectByLessonId(lessonId);
	}

	@Override
	public Integer updateCourseLessonQuizByLessonId(CourseLessonQuiz bean, String lessonId) {
		return this.courseLessonQuizMapper.updateByLessonId(bean, lessonId);
	}

	@Override
	public Integer deleteCourseLessonQuizByLessonId(String lessonId) {
		return this.courseLessonQuizMapper.deleteByLessonId(lessonId);
	}
}