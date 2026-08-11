package com.smart.campus.service;

import java.util.List;

import com.smart.campus.entity.query.MessageUserQuery;
import com.smart.campus.entity.po.MessageUser;
import com.smart.campus.entity.vo.PaginationResultVO;


/**
 * 用户消息收件表 业务接口
 */
public interface MessageUserService {

	/**
	 * 根据条件查询列表
	 */
	List<MessageUser> findListByParam(MessageUserQuery param);

	/**
	 * 根据条件查询列表
	 */
	Integer findCountByParam(MessageUserQuery param);

	/**
	 * 分页查询
	 */
	PaginationResultVO<MessageUser> findListByPage(MessageUserQuery param);

	/**
	 * 新增
	 */
	Integer add(MessageUser bean);

	/**
	 * 批量新增
	 */
	Integer addBatch(List<MessageUser> listBean);

	/**
	 * 批量新增/修改
	 */
	Integer addOrUpdateBatch(List<MessageUser> listBean);

	/**
	 * 多条件更新
	 */
	Integer updateByParam(MessageUser bean,MessageUserQuery param);

	/**
	 * 多条件删除
	 */
	Integer deleteByParam(MessageUserQuery param);

	/**
	 * 根据Id查询对象
	 */
	MessageUser getMessageUserById(Long id);


	/**
	 * 根据Id修改
	 */
	Integer updateMessageUserById(MessageUser bean,Long id);


	/**
	 * 根据Id删除
	 */
	Integer deleteMessageUserById(Long id);


	/**
	 * 根据MessageIdAndUserId查询对象
	 */
	MessageUser getMessageUserByMessageIdAndUserId(Long messageId,Integer userId);


	/**
	 * 根据MessageIdAndUserId修改
	 */
	Integer updateMessageUserByMessageIdAndUserId(MessageUser bean,Long messageId,Integer userId);


	/**
	 * 根据MessageIdAndUserId删除
	 */
	Integer deleteMessageUserByMessageIdAndUserId(Long messageId,Integer userId);

}