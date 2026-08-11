package com.nexora.service.impl;

import java.util.List;

import jakarta.annotation.Resource;

import org.springframework.stereotype.Service;

import com.nexora.entity.enums.PageSize;
import com.nexora.entity.query.SystemNoticeQuery;
import com.nexora.entity.po.SystemNotice;
import com.nexora.entity.vo.PaginationResultVO;
import com.nexora.entity.query.SimplePage;
import com.nexora.mappers.SystemNoticeMapper;
import com.nexora.service.SystemNoticeService;
import com.nexora.utils.StringTools;


/**
 * 系统通知表 业务接口实现
 */
@Service("systemNoticeService")
public class SystemNoticeServiceImpl implements SystemNoticeService {

	@Resource
	private SystemNoticeMapper<SystemNotice, SystemNoticeQuery> systemNoticeMapper;

	/**
	 * 根据条件查询列表
	 */
	@Override
	public List<SystemNotice> findListByParam(SystemNoticeQuery param) {
		return this.systemNoticeMapper.selectList(param);
	}

	/**
	 * 根据条件查询列表
	 */
	@Override
	public Integer findCountByParam(SystemNoticeQuery param) {
		return this.systemNoticeMapper.selectCount(param);
	}

	/**
	 * 分页查询方法
	 */
	@Override
	public PaginationResultVO<SystemNotice> findListByPage(SystemNoticeQuery param) {
		int count = this.findCountByParam(param);
		int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

		SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
		param.setSimplePage(page);
		List<SystemNotice> list = this.findListByParam(param);
		PaginationResultVO<SystemNotice> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result;
	}

	/**
	 * 新增
	 */
	@Override
	public Integer add(SystemNotice bean) {
		return this.systemNoticeMapper.insert(bean);
	}

	/**
	 * 批量新增
	 */
	@Override
	public Integer addBatch(List<SystemNotice> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.systemNoticeMapper.insertBatch(listBean);
	}

	/**
	 * 批量新增或者修改
	 */
	@Override
	public Integer addOrUpdateBatch(List<SystemNotice> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.systemNoticeMapper.insertOrUpdateBatch(listBean);
	}

	/**
	 * 多条件更新
	 */
	@Override
	public Integer updateByParam(SystemNotice bean, SystemNoticeQuery param) {
		StringTools.checkParam(param);
		return this.systemNoticeMapper.updateByParam(bean, param);
	}

	/**
	 * 多条件删除
	 */
	@Override
	public Integer deleteByParam(SystemNoticeQuery param) {
		StringTools.checkParam(param);
		return this.systemNoticeMapper.deleteByParam(param);
	}

	/**
	 * 根据NoticeId获取对象
	 */
	@Override
	public SystemNotice getSystemNoticeByNoticeId(Integer noticeId) {
		return this.systemNoticeMapper.selectByNoticeId(noticeId);
	}

	/**
	 * 根据NoticeId修改
	 */
	@Override
	public Integer updateSystemNoticeByNoticeId(SystemNotice bean, Integer noticeId) {
		return this.systemNoticeMapper.updateByNoticeId(bean, noticeId);
	}

	/**
	 * 根据NoticeId删除
	 */
	@Override
	public Integer deleteSystemNoticeByNoticeId(Integer noticeId) {
		return this.systemNoticeMapper.deleteByNoticeId(noticeId);
	}
}