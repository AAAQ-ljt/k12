package com.nexora.service.impl;

import java.util.List;

import jakarta.annotation.Resource;

import org.springframework.stereotype.Service;

import com.nexora.entity.enums.PageSize;
import com.nexora.entity.query.MessageUserQuery;
import com.nexora.entity.po.MessageUser;
import com.nexora.entity.vo.PaginationResultVO;
import com.nexora.entity.query.SimplePage;
import com.nexora.mappers.MessageUserMapper;
import com.nexora.service.MessageUserService;
import com.nexora.utils.StringTools;


/**
 * 用户消息关联表 业务接口实现
 */
@Service("messageUserService")
public class MessageUserServiceImpl implements MessageUserService {

	@Resource
	private MessageUserMapper<MessageUser, MessageUserQuery> messageUserMapper;

	/**
	 * 根据条件查询列表
	 */
	@Override
	public List<MessageUser> findListByParam(MessageUserQuery param) {
		return this.messageUserMapper.selectList(param);
	}

	/**
	 * 根据条件查询列表
	 */
	@Override
	public Integer findCountByParam(MessageUserQuery param) {
		return this.messageUserMapper.selectCount(param);
	}

	/**
	 * 分页查询方法
	 */
	@Override
	public PaginationResultVO<MessageUser> findListByPage(MessageUserQuery param) {
		int count = this.findCountByParam(param);
		int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

		SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
		param.setSimplePage(page);
		List<MessageUser> list = this.findListByParam(param);
		PaginationResultVO<MessageUser> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result;
	}

	/**
	 * 新增
	 */
	@Override
	public Integer add(MessageUser bean) {
		return this.messageUserMapper.insert(bean);
	}

	/**
	 * 批量新增
	 */
	@Override
	public Integer addBatch(List<MessageUser> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.messageUserMapper.insertBatch(listBean);
	}

	/**
	 * 批量新增或者修改
	 */
	@Override
	public Integer addOrUpdateBatch(List<MessageUser> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.messageUserMapper.insertOrUpdateBatch(listBean);
	}

	/**
	 * 多条件更新
	 */
	@Override
	public Integer updateByParam(MessageUser bean, MessageUserQuery param) {
		StringTools.checkParam(param);
		return this.messageUserMapper.updateByParam(bean, param);
	}

	/**
	 * 多条件删除
	 */
	@Override
	public Integer deleteByParam(MessageUserQuery param) {
		StringTools.checkParam(param);
		return this.messageUserMapper.deleteByParam(param);
	}

	/**
	 * 根据Id获取对象
	 */
	@Override
	public MessageUser getMessageUserById(Integer id) {
		return this.messageUserMapper.selectById(id);
	}

	/**
	 * 根据Id修改
	 */
	@Override
	public Integer updateMessageUserById(MessageUser bean, Integer id) {
		return this.messageUserMapper.updateById(bean, id);
	}

	/**
	 * 根据Id删除
	 */
	@Override
	public Integer deleteMessageUserById(Integer id) {
		return this.messageUserMapper.deleteById(id);
	}
}