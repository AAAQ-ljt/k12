package com.nexora.service.impl;

import java.util.List;

import jakarta.annotation.Resource;

import org.springframework.stereotype.Service;

import com.nexora.entity.enums.PageSize;
import com.nexora.entity.query.AgentMessageQuery;
import com.nexora.entity.po.AgentMessage;
import com.nexora.entity.vo.PaginationResultVO;
import com.nexora.entity.query.SimplePage;
import com.nexora.mappers.AgentMessageMapper;
import com.nexora.service.AgentMessageService;
import com.nexora.utils.StringTools;


/**
 * AI消息表 业务接口实现
 */
@Service("agentMessageService")
public class AgentMessageServiceImpl implements AgentMessageService {

	@Resource
	private AgentMessageMapper<AgentMessage, AgentMessageQuery> agentMessageMapper;

	/**
	 * 根据条件查询列表
	 */
	@Override
	public List<AgentMessage> findListByParam(AgentMessageQuery param) {
		return this.agentMessageMapper.selectList(param);
	}

	/**
	 * 根据条件查询列表
	 */
	@Override
	public Integer findCountByParam(AgentMessageQuery param) {
		return this.agentMessageMapper.selectCount(param);
	}

	/**
	 * 分页查询方法
	 */
	@Override
	public PaginationResultVO<AgentMessage> findListByPage(AgentMessageQuery param) {
		int count = this.findCountByParam(param);
		int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

		SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
		param.setSimplePage(page);
		List<AgentMessage> list = this.findListByParam(param);
		PaginationResultVO<AgentMessage> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result;
	}

	/**
	 * 新增
	 */
	@Override
	public Integer add(AgentMessage bean) {
		return this.agentMessageMapper.insert(bean);
	}

	/**
	 * 批量新增
	 */
	@Override
	public Integer addBatch(List<AgentMessage> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.agentMessageMapper.insertBatch(listBean);
	}

	/**
	 * 批量新增或者修改
	 */
	@Override
	public Integer addOrUpdateBatch(List<AgentMessage> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.agentMessageMapper.insertOrUpdateBatch(listBean);
	}

	/**
	 * 多条件更新
	 */
	@Override
	public Integer updateByParam(AgentMessage bean, AgentMessageQuery param) {
		StringTools.checkParam(param);
		return this.agentMessageMapper.updateByParam(bean, param);
	}

	/**
	 * 多条件删除
	 */
	@Override
	public Integer deleteByParam(AgentMessageQuery param) {
		StringTools.checkParam(param);
		return this.agentMessageMapper.deleteByParam(param);
	}

	/**
	 * 根据MessageId获取对象
	 */
	@Override
	public AgentMessage getAgentMessageByMessageId(String messageId) {
		return this.agentMessageMapper.selectByMessageId(messageId);
	}

	/**
	 * 根据MessageId修改
	 */
	@Override
	public Integer updateAgentMessageByMessageId(AgentMessage bean, String messageId) {
		return this.agentMessageMapper.updateByMessageId(bean, messageId);
	}

	/**
	 * 根据MessageId删除
	 */
	@Override
	public Integer deleteAgentMessageByMessageId(String messageId) {
		return this.agentMessageMapper.deleteByMessageId(messageId);
	}
}