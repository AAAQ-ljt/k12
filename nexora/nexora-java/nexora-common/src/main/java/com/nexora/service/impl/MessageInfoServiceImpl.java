package com.nexora.service.impl;

import java.util.List;

import jakarta.annotation.Resource;

import org.springframework.stereotype.Service;

import com.nexora.entity.enums.PageSize;
import com.nexora.entity.query.MessageInfoQuery;
import com.nexora.entity.po.MessageInfo;
import com.nexora.entity.vo.PaginationResultVO;
import com.nexora.entity.query.SimplePage;
import com.nexora.mappers.MessageInfoMapper;
import com.nexora.service.MessageInfoService;
import com.nexora.utils.StringTools;


/**
 * 消息主表 业务接口实现
 */
@Service("messageInfoService")
public class MessageInfoServiceImpl implements MessageInfoService {

	@Resource
	private MessageInfoMapper<MessageInfo, MessageInfoQuery> messageInfoMapper;

	/**
	 * 根据条件查询列表
	 */
	@Override
	public List<MessageInfo> findListByParam(MessageInfoQuery param) {
		return this.messageInfoMapper.selectList(param);
	}

	/**
	 * 根据条件查询列表
	 */
	@Override
	public Integer findCountByParam(MessageInfoQuery param) {
		return this.messageInfoMapper.selectCount(param);
	}

	/**
	 * 分页查询方法
	 */
	@Override
	public PaginationResultVO<MessageInfo> findListByPage(MessageInfoQuery param) {
		int count = this.findCountByParam(param);
		int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

		SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
		param.setSimplePage(page);
		List<MessageInfo> list = this.findListByParam(param);
		PaginationResultVO<MessageInfo> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result;
	}

	/**
	 * 新增
	 */
	@Override
	public Integer add(MessageInfo bean) {
		return this.messageInfoMapper.insert(bean);
	}

	/**
	 * 批量新增
	 */
	@Override
	public Integer addBatch(List<MessageInfo> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.messageInfoMapper.insertBatch(listBean);
	}

	/**
	 * 批量新增或者修改
	 */
	@Override
	public Integer addOrUpdateBatch(List<MessageInfo> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.messageInfoMapper.insertOrUpdateBatch(listBean);
	}

	/**
	 * 多条件更新
	 */
	@Override
	public Integer updateByParam(MessageInfo bean, MessageInfoQuery param) {
		StringTools.checkParam(param);
		return this.messageInfoMapper.updateByParam(bean, param);
	}

	/**
	 * 多条件删除
	 */
	@Override
	public Integer deleteByParam(MessageInfoQuery param) {
		StringTools.checkParam(param);
		return this.messageInfoMapper.deleteByParam(param);
	}

	/**
	 * 根据MessageId获取对象
	 */
	@Override
	public MessageInfo getMessageInfoByMessageId(Integer messageId) {
		return this.messageInfoMapper.selectByMessageId(messageId);
	}

	/**
	 * 根据MessageId修改
	 */
	@Override
	public Integer updateMessageInfoByMessageId(MessageInfo bean, Integer messageId) {
		return this.messageInfoMapper.updateByMessageId(bean, messageId);
	}

	/**
	 * 根据MessageId删除
	 */
	@Override
	public Integer deleteMessageInfoByMessageId(Integer messageId) {
		return this.messageInfoMapper.deleteByMessageId(messageId);
	}
}