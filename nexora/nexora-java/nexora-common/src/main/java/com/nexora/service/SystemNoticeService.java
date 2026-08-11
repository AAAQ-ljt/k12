package com.nexora.service;

import java.util.List;

import com.nexora.entity.query.SystemNoticeQuery;
import com.nexora.entity.po.SystemNotice;
import com.nexora.entity.vo.PaginationResultVO;


/**
 * 系统通知表 业务接口
 */
public interface SystemNoticeService {

	/**
	 * 根据条件查询列表
	 */
	List<SystemNotice> findListByParam(SystemNoticeQuery param);

	/**
	 * 根据条件查询列表
	 */
	Integer findCountByParam(SystemNoticeQuery param);

	/**
	 * 分页查询
	 */
	PaginationResultVO<SystemNotice> findListByPage(SystemNoticeQuery param);

	/**
	 * 新增
	 */
	Integer add(SystemNotice bean);

	/**
	 * 批量新增
	 */
	Integer addBatch(List<SystemNotice> listBean);

	/**
	 * 批量新增/修改
	 */
	Integer addOrUpdateBatch(List<SystemNotice> listBean);

	/**
	 * 多条件更新
	 */
	Integer updateByParam(SystemNotice bean,SystemNoticeQuery param);

	/**
	 * 多条件删除
	 */
	Integer deleteByParam(SystemNoticeQuery param);

	/**
	 * 根据NoticeId查询对象
	 */
	SystemNotice getSystemNoticeByNoticeId(Integer noticeId);


	/**
	 * 根据NoticeId修改
	 */
	Integer updateSystemNoticeByNoticeId(SystemNotice bean,Integer noticeId);


	/**
	 * 根据NoticeId删除
	 */
	Integer deleteSystemNoticeByNoticeId(Integer noticeId);

}