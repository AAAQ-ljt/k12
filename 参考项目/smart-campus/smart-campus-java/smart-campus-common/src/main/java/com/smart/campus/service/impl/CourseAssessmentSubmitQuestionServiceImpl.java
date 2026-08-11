package com.smart.campus.service.impl;

import java.util.List;

import jakarta.annotation.Resource;

import org.springframework.stereotype.Service;

import com.smart.campus.entity.enums.PageSize;
import com.smart.campus.entity.query.CourseAssessmentSubmitQuestionQuery;
import com.smart.campus.entity.po.CourseAssessmentSubmitQuestion;
import com.smart.campus.entity.vo.PaginationResultVO;
import com.smart.campus.entity.query.SimplePage;
import com.smart.campus.mappers.CourseAssessmentSubmitQuestionMapper;
import com.smart.campus.service.CourseAssessmentSubmitQuestionService;
import com.smart.campus.utils.StringTools;


/**
 * 课程作业/考试学生答题明细表 业务接口实现
 */
@Service("courseAssessmentSubmitQuestionService")
public class CourseAssessmentSubmitQuestionServiceImpl implements CourseAssessmentSubmitQuestionService {

	@Resource
	private CourseAssessmentSubmitQuestionMapper<CourseAssessmentSubmitQuestion, CourseAssessmentSubmitQuestionQuery> courseAssessmentSubmitQuestionMapper;

	/**
	 * 根据条件查询列表
	 */
	@Override
	public List<CourseAssessmentSubmitQuestion> findListByParam(CourseAssessmentSubmitQuestionQuery param) {
		return this.courseAssessmentSubmitQuestionMapper.selectList(param);
	}

	/**
	 * 根据条件查询列表
	 */
	@Override
	public Integer findCountByParam(CourseAssessmentSubmitQuestionQuery param) {
		return this.courseAssessmentSubmitQuestionMapper.selectCount(param);
	}

	/**
	 * 分页查询方法
	 */
	@Override
	public PaginationResultVO<CourseAssessmentSubmitQuestion> findListByPage(CourseAssessmentSubmitQuestionQuery param) {
		int count = this.findCountByParam(param);
		int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

		SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
		param.setSimplePage(page);
		List<CourseAssessmentSubmitQuestion> list = this.findListByParam(param);
		PaginationResultVO<CourseAssessmentSubmitQuestion> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result;
	}

	/**
	 * 新增
	 */
	@Override
	public Integer add(CourseAssessmentSubmitQuestion bean) {
		return this.courseAssessmentSubmitQuestionMapper.insert(bean);
	}

	/**
	 * 批量新增
	 */
	@Override
	public Integer addBatch(List<CourseAssessmentSubmitQuestion> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.courseAssessmentSubmitQuestionMapper.insertBatch(listBean);
	}

	/**
	 * 批量新增或者修改
	 */
	@Override
	public Integer addOrUpdateBatch(List<CourseAssessmentSubmitQuestion> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.courseAssessmentSubmitQuestionMapper.insertOrUpdateBatch(listBean);
	}

	/**
	 * 多条件更新
	 */
	@Override
	public Integer updateByParam(CourseAssessmentSubmitQuestion bean, CourseAssessmentSubmitQuestionQuery param) {
		StringTools.checkParam(param);
		return this.courseAssessmentSubmitQuestionMapper.updateByParam(bean, param);
	}

	/**
	 * 多条件删除
	 */
	@Override
	public Integer deleteByParam(CourseAssessmentSubmitQuestionQuery param) {
		StringTools.checkParam(param);
		return this.courseAssessmentSubmitQuestionMapper.deleteByParam(param);
	}

	/**
	 * 根据Id获取对象
	 */
	@Override
	public CourseAssessmentSubmitQuestion getCourseAssessmentSubmitQuestionById(Long id) {
		return this.courseAssessmentSubmitQuestionMapper.selectById(id);
	}

	/**
	 * 根据Id修改
	 */
	@Override
	public Integer updateCourseAssessmentSubmitQuestionById(CourseAssessmentSubmitQuestion bean, Long id) {
		return this.courseAssessmentSubmitQuestionMapper.updateById(bean, id);
	}

	/**
	 * 根据Id删除
	 */
	@Override
	public Integer deleteCourseAssessmentSubmitQuestionById(Long id) {
		return this.courseAssessmentSubmitQuestionMapper.deleteById(id);
	}

	/**
	 * 根据SubmitIdAndQuestionId获取对象
	 */
	@Override
	public CourseAssessmentSubmitQuestion getCourseAssessmentSubmitQuestionBySubmitIdAndQuestionId(Long submitId, Integer questionId) {
		return this.courseAssessmentSubmitQuestionMapper.selectBySubmitIdAndQuestionId(submitId, questionId);
	}

	/**
	 * 根据SubmitIdAndQuestionId修改
	 */
	@Override
	public Integer updateCourseAssessmentSubmitQuestionBySubmitIdAndQuestionId(CourseAssessmentSubmitQuestion bean, Long submitId, Integer questionId) {
		return this.courseAssessmentSubmitQuestionMapper.updateBySubmitIdAndQuestionId(bean, submitId, questionId);
	}

	/**
	 * 根据SubmitIdAndQuestionId删除
	 */
	@Override
	public Integer deleteCourseAssessmentSubmitQuestionBySubmitIdAndQuestionId(Long submitId, Integer questionId) {
		return this.courseAssessmentSubmitQuestionMapper.deleteBySubmitIdAndQuestionId(submitId, questionId);
	}
}