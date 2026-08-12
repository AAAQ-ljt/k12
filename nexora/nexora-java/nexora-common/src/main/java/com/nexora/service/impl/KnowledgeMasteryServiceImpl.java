package com.nexora.service.impl;

import java.util.List;

import jakarta.annotation.Resource;

import org.springframework.stereotype.Service;

import com.nexora.entity.enums.PageSize;
import com.nexora.entity.query.KnowledgeMasteryQuery;
import com.nexora.entity.po.KnowledgeMastery;
import com.nexora.entity.vo.PaginationResultVO;
import com.nexora.entity.query.SimplePage;
import com.nexora.mappers.KnowledgeMasteryMapper;
import com.nexora.service.KnowledgeMasteryService;
import com.nexora.utils.StringTools;


/**
 * 知识点掌握度表 业务接口实现
 */
@Service("knowledgeMasteryService")
public class KnowledgeMasteryServiceImpl implements KnowledgeMasteryService {

	@Resource
	private KnowledgeMasteryMapper<KnowledgeMastery, KnowledgeMasteryQuery> knowledgeMasteryMapper;

	/**
	 * 根据条件查询列表
	 */
	@Override
	public List<KnowledgeMastery> findListByParam(KnowledgeMasteryQuery param) {
		return this.knowledgeMasteryMapper.selectList(param);
	}

	/**
	 * 根据条件查询列表
	 */
	@Override
	public Integer findCountByParam(KnowledgeMasteryQuery param) {
		return this.knowledgeMasteryMapper.selectCount(param);
	}

	/**
	 * 分页查询方法
	 */
	@Override
	public PaginationResultVO<KnowledgeMastery> findListByPage(KnowledgeMasteryQuery param) {
		int count = this.findCountByParam(param);
		int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

		SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
		param.setSimplePage(page);
		List<KnowledgeMastery> list = this.findListByParam(param);
		PaginationResultVO<KnowledgeMastery> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result;
	}

	/**
	 * 新增
	 */
	@Override
	public Integer add(KnowledgeMastery bean) {
		return this.knowledgeMasteryMapper.insert(bean);
	}

	/**
	 * 批量新增
	 */
	@Override
	public Integer addBatch(List<KnowledgeMastery> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.knowledgeMasteryMapper.insertBatch(listBean);
	}

	/**
	 * 批量新增或者修改
	 */
	@Override
	public Integer addOrUpdateBatch(List<KnowledgeMastery> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.knowledgeMasteryMapper.insertOrUpdateBatch(listBean);
	}

	/**
	 * 多条件更新
	 */
	@Override
	public Integer updateByParam(KnowledgeMastery bean, KnowledgeMasteryQuery param) {
		StringTools.checkParam(param);
		return this.knowledgeMasteryMapper.updateByParam(bean, param);
	}

	/**
	 * 多条件删除
	 */
	@Override
	public Integer deleteByParam(KnowledgeMasteryQuery param) {
		StringTools.checkParam(param);
		return this.knowledgeMasteryMapper.deleteByParam(param);
	}

	/**
	 * 根据Id获取对象
	 */
	@Override
	public KnowledgeMastery getKnowledgeMasteryById(Integer id) {
		return this.knowledgeMasteryMapper.selectById(id);
	}

	/**
	 * 根据Id修改
	 */
	@Override
	public Integer updateKnowledgeMasteryById(KnowledgeMastery bean, Integer id) {
		return this.knowledgeMasteryMapper.updateById(bean, id);
	}

	/**
	 * 根据Id删除
	 */
	@Override
	public Integer deleteKnowledgeMasteryById(Integer id) {
		return this.knowledgeMasteryMapper.deleteById(id);
	}

	/**
	 * 根据UserIdAndKnowledgePointId获取对象
	 */
	@Override
	public KnowledgeMastery getKnowledgeMasteryByUserIdAndKnowledgePointId(String userId, String knowledgePointId) {
		return this.knowledgeMasteryMapper.selectByUserIdAndKnowledgePointId(userId, knowledgePointId);
	}

	/**
	 * 根据UserIdAndKnowledgePointId修改
	 */
	@Override
	public Integer updateKnowledgeMasteryByUserIdAndKnowledgePointId(KnowledgeMastery bean, String userId, String knowledgePointId) {
		return this.knowledgeMasteryMapper.updateByUserIdAndKnowledgePointId(bean, userId, knowledgePointId);
	}

	/**
	 * 根据UserIdAndKnowledgePointId删除
	 */
	@Override
	public Integer deleteKnowledgeMasteryByUserIdAndKnowledgePointId(String userId, String knowledgePointId) {
		return this.knowledgeMasteryMapper.deleteByUserIdAndKnowledgePointId(userId, knowledgePointId);
	}
}