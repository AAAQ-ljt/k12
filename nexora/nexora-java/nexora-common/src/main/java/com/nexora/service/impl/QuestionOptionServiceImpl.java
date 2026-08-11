package com.nexora.service.impl;

import java.util.List;

import jakarta.annotation.Resource;

import org.springframework.stereotype.Service;

import com.nexora.entity.enums.PageSize;
import com.nexora.entity.query.QuestionOptionQuery;
import com.nexora.entity.po.QuestionOption;
import com.nexora.entity.vo.PaginationResultVO;
import com.nexora.entity.query.SimplePage;
import com.nexora.mappers.QuestionOptionMapper;
import com.nexora.service.QuestionOptionService;
import com.nexora.utils.StringTools;


/**
 * 题目选项表 业务接口实现
 */
@Service("questionOptionService")
public class QuestionOptionServiceImpl implements QuestionOptionService {

	@Resource
	private QuestionOptionMapper<QuestionOption, QuestionOptionQuery> questionOptionMapper;

	/**
	 * 根据条件查询列表
	 */
	@Override
	public List<QuestionOption> findListByParam(QuestionOptionQuery param) {
		return this.questionOptionMapper.selectList(param);
	}

	/**
	 * 根据条件查询列表
	 */
	@Override
	public Integer findCountByParam(QuestionOptionQuery param) {
		return this.questionOptionMapper.selectCount(param);
	}

	/**
	 * 分页查询方法
	 */
	@Override
	public PaginationResultVO<QuestionOption> findListByPage(QuestionOptionQuery param) {
		int count = this.findCountByParam(param);
		int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

		SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
		param.setSimplePage(page);
		List<QuestionOption> list = this.findListByParam(param);
		PaginationResultVO<QuestionOption> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result;
	}

	/**
	 * 新增
	 */
	@Override
	public Integer add(QuestionOption bean) {
		return this.questionOptionMapper.insert(bean);
	}

	/**
	 * 批量新增
	 */
	@Override
	public Integer addBatch(List<QuestionOption> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.questionOptionMapper.insertBatch(listBean);
	}

	/**
	 * 批量新增或者修改
	 */
	@Override
	public Integer addOrUpdateBatch(List<QuestionOption> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.questionOptionMapper.insertOrUpdateBatch(listBean);
	}

	/**
	 * 多条件更新
	 */
	@Override
	public Integer updateByParam(QuestionOption bean, QuestionOptionQuery param) {
		StringTools.checkParam(param);
		return this.questionOptionMapper.updateByParam(bean, param);
	}

	/**
	 * 多条件删除
	 */
	@Override
	public Integer deleteByParam(QuestionOptionQuery param) {
		StringTools.checkParam(param);
		return this.questionOptionMapper.deleteByParam(param);
	}

	/**
	 * 根据OptionId获取对象
	 */
	@Override
	public QuestionOption getQuestionOptionByOptionId(Integer optionId) {
		return this.questionOptionMapper.selectByOptionId(optionId);
	}

	/**
	 * 根据OptionId修改
	 */
	@Override
	public Integer updateQuestionOptionByOptionId(QuestionOption bean, Integer optionId) {
		return this.questionOptionMapper.updateByOptionId(bean, optionId);
	}

	/**
	 * 根据OptionId删除
	 */
	@Override
	public Integer deleteQuestionOptionByOptionId(Integer optionId) {
		return this.questionOptionMapper.deleteByOptionId(optionId);
	}
}