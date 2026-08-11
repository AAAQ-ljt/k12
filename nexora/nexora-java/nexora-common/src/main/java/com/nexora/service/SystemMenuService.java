package com.nexora.service;

import java.util.List;

import com.nexora.entity.query.SystemMenuQuery;
import com.nexora.entity.po.SystemMenu;
import com.nexora.entity.vo.PaginationResultVO;


/**
 * 系统菜单表 业务接口
 */
public interface SystemMenuService {

	/**
	 * 根据条件查询列表
	 */
	List<SystemMenu> findListByParam(SystemMenuQuery param);

	/**
	 * 根据条件查询列表
	 */
	Integer findCountByParam(SystemMenuQuery param);

	/**
	 * 分页查询
	 */
	PaginationResultVO<SystemMenu> findListByPage(SystemMenuQuery param);

	/**
	 * 新增
	 */
	Integer add(SystemMenu bean);

	/**
	 * 批量新增
	 */
	Integer addBatch(List<SystemMenu> listBean);

	/**
	 * 批量新增/修改
	 */
	Integer addOrUpdateBatch(List<SystemMenu> listBean);

	/**
	 * 多条件更新
	 */
	Integer updateByParam(SystemMenu bean,SystemMenuQuery param);

	/**
	 * 多条件删除
	 */
	Integer deleteByParam(SystemMenuQuery param);

	/**
	 * 根据MenuId查询对象
	 */
	SystemMenu getSystemMenuByMenuId(Integer menuId);


	/**
	 * 根据MenuId修改
	 */
	Integer updateSystemMenuByMenuId(SystemMenu bean,Integer menuId);


	/**
	 * 根据MenuId删除
	 */
	Integer deleteSystemMenuByMenuId(Integer menuId);


	/**
	 * 根据MenuCode查询对象
	 */
	SystemMenu getSystemMenuByMenuCode(String menuCode);


	/**
	 * 根据MenuCode修改
	 */
	Integer updateSystemMenuByMenuCode(SystemMenu bean,String menuCode);


	/**
	 * 根据MenuCode删除
	 */
	Integer deleteSystemMenuByMenuCode(String menuCode);

}