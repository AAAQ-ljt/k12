package com.easymall.service;

import com.easymall.entity.po.AgentMessage;
import com.easymall.entity.query.AgentMessageQuery;
import com.easymall.entity.vo.PaginationResultVO;

import java.util.List;


/**
 * 业务接口
 */
public interface AgentMessageService {

    /**
     * 根据条件查询列表
     */
    List<AgentMessage> findListByParam(AgentMessageQuery param);

    /**
     * 根据条件查询列表
     */
    Integer findCountByParam(AgentMessageQuery param);

    /**
     * 分页查询
     */
    PaginationResultVO<AgentMessage> findListByPage(AgentMessageQuery param);

    /**
     * 新增
     */
    Integer add(AgentMessage bean);

    /**
     * 批量新增
     */
    Integer addBatch(List<AgentMessage> listBean);

    /**
     * 批量新增/修改
     */
    Integer addOrUpdateBatch(List<AgentMessage> listBean);

    /**
     * 多条件更新
     */
    Integer updateByParam(AgentMessage bean, AgentMessageQuery param);

    /**
     * 多条件删除
     */
    Integer deleteByParam(AgentMessageQuery param);

    /**
     * 根据MessageId查询对象
     */
    AgentMessage getAgentMessageByMessageId(Integer messageId);


    /**
     * 根据MessageId修改
     */
    Integer updateAgentMessageByMessageId(AgentMessage bean, Integer messageId);


    /**
     * 根据MessageId删除
     */
    Integer deleteAgentMessageByMessageId(Integer messageId);

    AgentMessage saveMessage(String userId, String userMessage);

    void cancelMessage(String userId, Integer messageId);

    void completeMessage(Integer messageId, String bizType, String assistantMessage, String bizData);
}