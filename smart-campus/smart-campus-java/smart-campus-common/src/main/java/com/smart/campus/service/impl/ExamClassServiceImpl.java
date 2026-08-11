package com.smart.campus.service.impl;

import java.util.List;

import jakarta.annotation.Resource;

import org.springframework.stereotype.Service;

import com.smart.campus.entity.enums.PageSize;
import com.smart.campus.entity.query.ExamClassQuery;
import com.smart.campus.entity.po.ExamClass;
import com.smart.campus.entity.vo.PaginationResultVO;
import com.smart.campus.entity.query.SimplePage;
import com.smart.campus.mappers.ExamClassMapper;
import com.smart.campus.service.ExamClassService;
import com.smart.campus.utils.StringTools;


/**
 * 考试班级关联表 业务接口实现
 */
@Service("examClassService")
public class ExamClassServiceImpl implements ExamClassService {

	@Resource
	private ExamClassMapper<ExamClass, ExamClassQuery> examClassMapper;

	/**
	 * 根据条件查询列表
	 */
	@Override
	public List<ExamClass> findListByParam(ExamClassQuery param) {
		return this.examClassMapper.selectList(param);
	}

	/**
	 * 根据条件查询列表
	 */
	@Override
	public Integer findCountByParam(ExamClassQuery param) {
		return this.examClassMapper.selectCount(param);
	}

	/**
	 * 分页查询方法
	 */
	@Override
	public PaginationResultVO<ExamClass> findListByPage(ExamClassQuery param) {
		int count = this.findCountByParam(param);
		int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

		SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
		param.setSimplePage(page);
		List<ExamClass> list = this.findListByParam(param);
		PaginationResultVO<ExamClass> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result;
	}

	/**
	 * 新增
	 */
	@Override
	public Integer add(ExamClass bean) {
		return this.examClassMapper.insert(bean);
	}

	/**
	 * 批量新增
	 */
	@Override
	public Integer addBatch(List<ExamClass> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.examClassMapper.insertBatch(listBean);
	}

	/**
	 * 批量新增或者修改
	 */
	@Override
	public Integer addOrUpdateBatch(List<ExamClass> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.examClassMapper.insertOrUpdateBatch(listBean);
	}

	/**
	 * 多条件更新
	 */
	@Override
	public Integer updateByParam(ExamClass bean, ExamClassQuery param) {
		StringTools.checkParam(param);
		return this.examClassMapper.updateByParam(bean, param);
	}

	/**
	 * 多条件删除
	 */
	@Override
	public Integer deleteByParam(ExamClassQuery param) {
		StringTools.checkParam(param);
		return this.examClassMapper.deleteByParam(param);
	}

	/**
	 * 根据ExamIdAndClassId获取对象
	 */
	@Override
	public ExamClass getExamClassByExamIdAndClassId(String examId, Integer classId) {
		return this.examClassMapper.selectByExamIdAndClassId(examId, classId);
	}

	/**
	 * 根据ExamIdAndClassId修改
	 */
	@Override
	public Integer updateExamClassByExamIdAndClassId(ExamClass bean, String examId, Integer classId) {
		return this.examClassMapper.updateByExamIdAndClassId(bean, examId, classId);
	}

	/**
	 * 根据ExamIdAndClassId删除
	 */
	@Override
	public Integer deleteExamClassByExamIdAndClassId(String examId, Integer classId) {
		return this.examClassMapper.deleteByExamIdAndClassId(examId, classId);
	}
}