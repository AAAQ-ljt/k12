package com.smart.campus.service.impl;

import java.util.List;

import jakarta.annotation.Resource;

import org.springframework.stereotype.Service;

import com.smart.campus.entity.enums.PageSize;
import com.smart.campus.entity.query.PaperQuestionQuery;
import com.smart.campus.entity.po.PaperQuestion;
import com.smart.campus.entity.vo.PaginationResultVO;
import com.smart.campus.entity.query.SimplePage;
import com.smart.campus.mappers.PaperQuestionMapper;
import com.smart.campus.service.PaperQuestionService;
import com.smart.campus.utils.StringTools;


/**
 * 试卷题目编排表 业务接口实现
 */
@Service("paperQuestionService")
public class PaperQuestionServiceImpl implements PaperQuestionService {

	@Resource
	private PaperQuestionMapper<PaperQuestion, PaperQuestionQuery> paperQuestionMapper;

	/**
	 * 根据条件查询列表
	 */
	@Override
	public List<PaperQuestion> findListByParam(PaperQuestionQuery param) {
		return this.paperQuestionMapper.selectList(param);
	}

	/**
	 * 根据条件查询列表
	 */
	@Override
	public Integer findCountByParam(PaperQuestionQuery param) {
		return this.paperQuestionMapper.selectCount(param);
	}

	/**
	 * 分页查询方法
	 */
	@Override
	public PaginationResultVO<PaperQuestion> findListByPage(PaperQuestionQuery param) {
		int count = this.findCountByParam(param);
		int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

		SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
		param.setSimplePage(page);
		List<PaperQuestion> list = this.findListByParam(param);
		PaginationResultVO<PaperQuestion> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result;
	}

	/**
	 * 新增
	 */
	@Override
	public Integer add(PaperQuestion bean) {
		return this.paperQuestionMapper.insert(bean);
	}

	/**
	 * 批量新增
	 */
	@Override
	public Integer addBatch(List<PaperQuestion> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.paperQuestionMapper.insertBatch(listBean);
	}

	/**
	 * 批量新增或者修改
	 */
	@Override
	public Integer addOrUpdateBatch(List<PaperQuestion> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.paperQuestionMapper.insertOrUpdateBatch(listBean);
	}

	/**
	 * 多条件更新
	 */
	@Override
	public Integer updateByParam(PaperQuestion bean, PaperQuestionQuery param) {
		StringTools.checkParam(param);
		return this.paperQuestionMapper.updateByParam(bean, param);
	}

	/**
	 * 多条件删除
	 */
	@Override
	public Integer deleteByParam(PaperQuestionQuery param) {
		StringTools.checkParam(param);
		return this.paperQuestionMapper.deleteByParam(param);
	}

	/**
	 * 根据Id获取对象
	 */
	@Override
	public PaperQuestion getPaperQuestionById(Integer id) {
		return this.paperQuestionMapper.selectById(id);
	}

	/**
	 * 根据Id修改
	 */
	@Override
	public Integer updatePaperQuestionById(PaperQuestion bean, Integer id) {
		return this.paperQuestionMapper.updateById(bean, id);
	}

	/**
	 * 根据Id删除
	 */
	@Override
	public Integer deletePaperQuestionById(Integer id) {
		return this.paperQuestionMapper.deleteById(id);
	}

	/**
	 * 根据PaperIdAndQuestionId获取对象
	 */
	@Override
	public PaperQuestion getPaperQuestionByPaperIdAndQuestionId(String paperId, Integer questionId) {
		return this.paperQuestionMapper.selectByPaperIdAndQuestionId(paperId, questionId);
	}

	/**
	 * 根据PaperIdAndQuestionId修改
	 */
	@Override
	public Integer updatePaperQuestionByPaperIdAndQuestionId(PaperQuestion bean, String paperId, Integer questionId) {
		return this.paperQuestionMapper.updateByPaperIdAndQuestionId(bean, paperId, questionId);
	}

	/**
	 * 根据PaperIdAndQuestionId删除
	 */
	@Override
	public Integer deletePaperQuestionByPaperIdAndQuestionId(String paperId, Integer questionId) {
		return this.paperQuestionMapper.deleteByPaperIdAndQuestionId(paperId, questionId);
	}
}