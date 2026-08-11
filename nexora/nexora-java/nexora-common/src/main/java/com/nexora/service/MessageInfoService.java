package com.nexora.service;

import java.util.List;

import com.nexora.entity.query.MessageInfoQuery;
import com.nexora.entity.po.MessageInfo;
import com.nexora.entity.vo.PaginationResultVO;


/**
 * 消息主表 业务接口
 */
public interface MessageInfoService {

	/**
	 * 根据条件查询列表
	 */
	List<MessageInfo> findListByParam(MessageInfoQuery param);

	/**
	 * 根据条件查询列表
	 */
	Integer findCountByParam(MessageInfoQuery param);

	/**
	 * 分页查询
	 */
	PaginationResultVO<MessageInfo> findListByPage(MessageInfoQuery param);

	/**
	 * 新增
	 */
	Integer add(MessageInfo bean);

	/**
	 * 批量新增
	 */
	Integer addBatch(List<MessageInfo> listBean);

	/**
	 * 批量新增/修改
	 */
	Integer addOrUpdateBatch(List<MessageInfo> listBean);

	/**
	 * 多条件更新
	 */
	Integer updateByParam(MessageInfo bean,MessageInfoQuery param);

	/**
	 * 多条件删除
	 */
	Integer deleteByParam(MessageInfoQuery param);

	/**
	 * 根据MessageId查询对象
	 */
	MessageInfo getMessageInfoByMessageId(Integer messageId);


	/**
	 * 根据MessageId修改
	 */
	Integer updateMessageInfoByMessageId(MessageInfo bean,Integer messageId);


	/**
	 * 根据MessageId删除
	 */
	Integer deleteMessageInfoByMessageId(Integer messageId);

}