package com.nexora.service;

import java.util.List;

import com.nexora.entity.query.AgentSessionQuery;
import com.nexora.entity.po.AgentSession;
import com.nexora.entity.vo.PaginationResultVO;


/**
 * AI会话表 业务接口
 */
public interface AgentSessionService {

	/**
	 * 根据条件查询列表
	 */
	List<AgentSession> findListByParam(AgentSessionQuery param);

	/**
	 * 根据条件查询列表
	 */
	Integer findCountByParam(AgentSessionQuery param);

	/**
	 * 分页查询
	 */
	PaginationResultVO<AgentSession> findListByPage(AgentSessionQuery param);

	/**
	 * 新增
	 */
	Integer add(AgentSession bean);

	/**
	 * 批量新增
	 */
	Integer addBatch(List<AgentSession> listBean);

	/**
	 * 批量新增/修改
	 */
	Integer addOrUpdateBatch(List<AgentSession> listBean);

	/**
	 * 多条件更新
	 */
	Integer updateByParam(AgentSession bean,AgentSessionQuery param);

	/**
	 * 多条件删除
	 */
	Integer deleteByParam(AgentSessionQuery param);

	/**
	 * 根据SessionId查询对象
	 */
	AgentSession getAgentSessionBySessionId(String sessionId);


	/**
	 * 根据SessionId修改
	 */
	Integer updateAgentSessionBySessionId(AgentSession bean,String sessionId);


	/**
	 * 根据SessionId删除
	 */
	Integer deleteAgentSessionBySessionId(String sessionId);

}