package com.smart.campus.service.impl;

import java.util.List;

import jakarta.annotation.Resource;

import org.springframework.stereotype.Service;

import com.smart.campus.entity.enums.PageSize;
import com.smart.campus.entity.query.ExamInfoQuery;
import com.smart.campus.entity.po.ExamInfo;
import com.smart.campus.entity.vo.PaginationResultVO;
import com.smart.campus.entity.query.SimplePage;
import com.smart.campus.mappers.ExamInfoMapper;
import com.smart.campus.service.ExamInfoService;
import com.smart.campus.utils.StringTools;


/**
 * 在线考试表 业务接口实现
 */
@Service("examInfoService")
public class ExamInfoServiceImpl implements ExamInfoService {

	@Resource
	private ExamInfoMapper<ExamInfo, ExamInfoQuery> examInfoMapper;

	/**
	 * 根据条件查询列表
	 */
	@Override
	public List<ExamInfo> findListByParam(ExamInfoQuery param) {
		return this.examInfoMapper.selectList(param);
	}

	/**
	 * 根据条件查询列表
	 */
	@Override
	public Integer findCountByParam(ExamInfoQuery param) {
		return this.examInfoMapper.selectCount(param);
	}

	/**
	 * 分页查询方法
	 */
	@Override
	public PaginationResultVO<ExamInfo> findListByPage(ExamInfoQuery param) {
		int count = this.findCountByParam(param);
		int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

		SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
		param.setSimplePage(page);
		List<ExamInfo> list = this.findListByParam(param);
		PaginationResultVO<ExamInfo> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result;
	}

	/**
	 * 新增
	 */
	@Override
	public Integer add(ExamInfo bean) {
		return this.examInfoMapper.insert(bean);
	}

	/**
	 * 批量新增
	 */
	@Override
	public Integer addBatch(List<ExamInfo> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.examInfoMapper.insertBatch(listBean);
	}

	/**
	 * 批量新增或者修改
	 */
	@Override
	public Integer addOrUpdateBatch(List<ExamInfo> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.examInfoMapper.insertOrUpdateBatch(listBean);
	}

	/**
	 * 多条件更新
	 */
	@Override
	public Integer updateByParam(ExamInfo bean, ExamInfoQuery param) {
		StringTools.checkParam(param);
		return this.examInfoMapper.updateByParam(bean, param);
	}

	/**
	 * 多条件删除
	 */
	@Override
	public Integer deleteByParam(ExamInfoQuery param) {
		StringTools.checkParam(param);
		return this.examInfoMapper.deleteByParam(param);
	}

	/**
	 * 根据ExamId获取对象
	 */
	@Override
	public ExamInfo getExamInfoByExamId(String examId) {
		return this.examInfoMapper.selectByExamId(examId);
	}

	/**
	 * 根据ExamId修改
	 */
	@Override
	public Integer updateExamInfoByExamId(ExamInfo bean, String examId) {
		return this.examInfoMapper.updateByExamId(bean, examId);
	}

	/**
	 * 根据ExamId删除
	 */
	@Override
	public Integer deleteExamInfoByExamId(String examId) {
		return this.examInfoMapper.deleteByExamId(examId);
	}
}