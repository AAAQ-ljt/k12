package com.nexora.service.impl;

import java.util.List;

import jakarta.annotation.Resource;

import org.springframework.stereotype.Service;

import com.nexora.entity.enums.PageSize;
import com.nexora.entity.query.AgentSessionQuery;
import com.nexora.entity.po.AgentSession;
import com.nexora.entity.vo.PaginationResultVO;
import com.nexora.entity.query.SimplePage;
import com.nexora.mappers.AgentSessionMapper;
import com.nexora.service.AgentSessionService;
import com.nexora.utils.StringTools;


/**
 * AI会话表 业务接口实现
 */
@Service("agentSessionService")
public class AgentSessionServiceImpl implements AgentSessionService {

	@Resource
	private AgentSessionMapper<AgentSession, AgentSessionQuery> agentSessionMapper;

	/**
	 * 根据条件查询列表
	 */
	@Override
	public List<AgentSession> findListByParam(AgentSessionQuery param) {
		return this.agentSessionMapper.selectList(param);
	}

	/**
	 * 根据条件查询列表
	 */
	@Override
	public Integer findCountByParam(AgentSessionQuery param) {
		return this.agentSessionMapper.selectCount(param);
	}

	/**
	 * 分页查询方法
	 */
	@Override
	public PaginationResultVO<AgentSession> findListByPage(AgentSessionQuery param) {
		int count = this.findCountByParam(param);
		int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

		SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
		param.setSimplePage(page);
		List<AgentSession> list = this.findListByParam(param);
		PaginationResultVO<AgentSession> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result;
	}

	/**
	 * 新增
	 */
	@Override
	public Integer add(AgentSession bean) {
		return this.agentSessionMapper.insert(bean);
	}

	/**
	 * 批量新增
	 */
	@Override
	public Integer addBatch(List<AgentSession> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.agentSessionMapper.insertBatch(listBean);
	}

	/**
	 * 批量新增或者修改
	 */
	@Override
	public Integer addOrUpdateBatch(List<AgentSession> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.agentSessionMapper.insertOrUpdateBatch(listBean);
	}

	/**
	 * 多条件更新
	 */
	@Override
	public Integer updateByParam(AgentSession bean, AgentSessionQuery param) {
		StringTools.checkParam(param);
		return this.agentSessionMapper.updateByParam(bean, param);
	}

	/**
	 * 多条件删除
	 */
	@Override
	public Integer deleteByParam(AgentSessionQuery param) {
		StringTools.checkParam(param);
		return this.agentSessionMapper.deleteByParam(param);
	}

	/**
	 * 根据SessionId获取对象
	 */
	@Override
	public AgentSession getAgentSessionBySessionId(String sessionId) {
		return this.agentSessionMapper.selectBySessionId(sessionId);
	}

	/**
	 * 根据SessionId修改
	 */
	@Override
	public Integer updateAgentSessionBySessionId(AgentSession bean, String sessionId) {
		return this.agentSessionMapper.updateBySessionId(bean, sessionId);
	}

	/**
	 * 根据SessionId删除
	 */
	@Override
	public Integer deleteAgentSessionBySessionId(String sessionId) {
		return this.agentSessionMapper.deleteBySessionId(sessionId);
	}
}