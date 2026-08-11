package com.nexora.service;

import java.util.List;

import com.nexora.entity.query.MessageUserQuery;
import com.nexora.entity.po.MessageUser;
import com.nexora.entity.vo.PaginationResultVO;


/**
 * 用户消息关联表 业务接口
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
	MessageUser getMessageUserById(Integer id);


	/**
	 * 根据Id修改
	 */
	Integer updateMessageUserById(MessageUser bean,Integer id);


	/**
	 * 根据Id删除
	 */
	Integer deleteMessageUserById(Integer id);

}