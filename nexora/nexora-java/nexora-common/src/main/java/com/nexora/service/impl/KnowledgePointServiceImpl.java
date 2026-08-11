package com.nexora.service.impl;

import java.util.List;

import jakarta.annotation.Resource;

import org.springframework.stereotype.Service;

import com.nexora.entity.enums.PageSize;
import com.nexora.entity.query.KnowledgePointQuery;
import com.nexora.entity.po.KnowledgePoint;
import com.nexora.entity.vo.PaginationResultVO;
import com.nexora.entity.query.SimplePage;
import com.nexora.mappers.KnowledgePointMapper;
import com.nexora.service.KnowledgePointService;
import com.nexora.utils.StringTools;


/**
 * 知识点表（领域中心） 业务接口实现
 */
@Service("knowledgePointService")
public class KnowledgePointServiceImpl implements KnowledgePointService {

	@Resource
	private KnowledgePointMapper<KnowledgePoint, KnowledgePointQuery> knowledgePointMapper;

	/**
	 * 根据条件查询列表
	 */
	@Override
	public List<KnowledgePoint> findListByParam(KnowledgePointQuery param) {
		return this.knowledgePointMapper.selectList(param);
	}

	/**
	 * 根据条件查询列表
	 */
	@Override
	public Integer findCountByParam(KnowledgePointQuery param) {
		return this.knowledgePointMapper.selectCount(param);
	}

	/**
	 * 分页查询方法
	 */
	@Override
	public PaginationResultVO<KnowledgePoint> findListByPage(KnowledgePointQuery param) {
		int count = this.findCountByParam(param);
		int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

		SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
		param.setSimplePage(page);
		List<KnowledgePoint> list = this.findListByParam(param);
		PaginationResultVO<KnowledgePoint> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result;
	}

	/**
	 * 新增
	 */
	@Override
	public Integer add(KnowledgePoint bean) {
		return this.knowledgePointMapper.insert(bean);
	}

	/**
	 * 批量新增
	 */
	@Override
	public Integer addBatch(List<KnowledgePoint> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.knowledgePointMapper.insertBatch(listBean);
	}

	/**
	 * 批量新增或者修改
	 */
	@Override
	public Integer addOrUpdateBatch(List<KnowledgePoint> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.knowledgePointMapper.insertOrUpdateBatch(listBean);
	}

	/**
	 * 多条件更新
	 */
	@Override
	public Integer updateByParam(KnowledgePoint bean, KnowledgePointQuery param) {
		StringTools.checkParam(param);
		return this.knowledgePointMapper.updateByParam(bean, param);
	}

	/**
	 * 多条件删除
	 */
	@Override
	public Integer deleteByParam(KnowledgePointQuery param) {
		StringTools.checkParam(param);
		return this.knowledgePointMapper.deleteByParam(param);
	}

	/**
	 * 根据KnowledgePointId获取对象
	 */
	@Override
	public KnowledgePoint getKnowledgePointByKnowledgePointId(String knowledgePointId) {
		return this.knowledgePointMapper.selectByKnowledgePointId(knowledgePointId);
	}

	/**
	 * 根据KnowledgePointId修改
	 */
	@Override
	public Integer updateKnowledgePointByKnowledgePointId(KnowledgePoint bean, String knowledgePointId) {
		return this.knowledgePointMapper.updateByKnowledgePointId(bean, knowledgePointId);
	}

	/**
	 * 根据KnowledgePointId删除
	 */
	@Override
	public Integer deleteKnowledgePointByKnowledgePointId(String knowledgePointId) {
		return this.knowledgePointMapper.deleteByKnowledgePointId(knowledgePointId);
	}
}