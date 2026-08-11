package com.nexora.service.impl;

import java.util.List;

import jakarta.annotation.Resource;

import org.springframework.stereotype.Service;

import com.nexora.entity.enums.PageSize;
import com.nexora.entity.query.KnowledgeDocQuery;
import com.nexora.entity.po.KnowledgeDoc;
import com.nexora.entity.vo.PaginationResultVO;
import com.nexora.entity.query.SimplePage;
import com.nexora.mappers.KnowledgeDocMapper;
import com.nexora.service.KnowledgeDocService;
import com.nexora.utils.StringTools;


/**
 * 知识库文档表 业务接口实现
 */
@Service("knowledgeDocService")
public class KnowledgeDocServiceImpl implements KnowledgeDocService {

	@Resource
	private KnowledgeDocMapper<KnowledgeDoc, KnowledgeDocQuery> knowledgeDocMapper;

	/**
	 * 根据条件查询列表
	 */
	@Override
	public List<KnowledgeDoc> findListByParam(KnowledgeDocQuery param) {
		return this.knowledgeDocMapper.selectList(param);
	}

	/**
	 * 根据条件查询列表
	 */
	@Override
	public Integer findCountByParam(KnowledgeDocQuery param) {
		return this.knowledgeDocMapper.selectCount(param);
	}

	/**
	 * 分页查询方法
	 */
	@Override
	public PaginationResultVO<KnowledgeDoc> findListByPage(KnowledgeDocQuery param) {
		int count = this.findCountByParam(param);
		int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

		SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
		param.setSimplePage(page);
		List<KnowledgeDoc> list = this.findListByParam(param);
		PaginationResultVO<KnowledgeDoc> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result;
	}

	/**
	 * 新增
	 */
	@Override
	public Integer add(KnowledgeDoc bean) {
		return this.knowledgeDocMapper.insert(bean);
	}

	/**
	 * 批量新增
	 */
	@Override
	public Integer addBatch(List<KnowledgeDoc> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.knowledgeDocMapper.insertBatch(listBean);
	}

	/**
	 * 批量新增或者修改
	 */
	@Override
	public Integer addOrUpdateBatch(List<KnowledgeDoc> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.knowledgeDocMapper.insertOrUpdateBatch(listBean);
	}

	/**
	 * 多条件更新
	 */
	@Override
	public Integer updateByParam(KnowledgeDoc bean, KnowledgeDocQuery param) {
		StringTools.checkParam(param);
		return this.knowledgeDocMapper.updateByParam(bean, param);
	}

	/**
	 * 多条件删除
	 */
	@Override
	public Integer deleteByParam(KnowledgeDocQuery param) {
		StringTools.checkParam(param);
		return this.knowledgeDocMapper.deleteByParam(param);
	}

	/**
	 * 根据DocId获取对象
	 */
	@Override
	public KnowledgeDoc getKnowledgeDocByDocId(String docId) {
		return this.knowledgeDocMapper.selectByDocId(docId);
	}

	/**
	 * 根据DocId修改
	 */
	@Override
	public Integer updateKnowledgeDocByDocId(KnowledgeDoc bean, String docId) {
		return this.knowledgeDocMapper.updateByDocId(bean, docId);
	}

	/**
	 * 根据DocId删除
	 */
	@Override
	public Integer deleteKnowledgeDocByDocId(String docId) {
		return this.knowledgeDocMapper.deleteByDocId(docId);
	}
}