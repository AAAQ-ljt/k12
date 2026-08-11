package com.nexora.service;

import java.util.List;

import com.nexora.entity.query.SystemRoleMenuQuery;
import com.nexora.entity.po.SystemRoleMenu;
import com.nexora.entity.vo.PaginationResultVO;


/**
 * 角色菜单关联表 业务接口
 */
public interface SystemRoleMenuService {

	/**
	 * 根据条件查询列表
	 */
	List<SystemRoleMenu> findListByParam(SystemRoleMenuQuery param);

	/**
	 * 根据条件查询列表
	 */
	Integer findCountByParam(SystemRoleMenuQuery param);

	/**
	 * 分页查询
	 */
	PaginationResultVO<SystemRoleMenu> findListByPage(SystemRoleMenuQuery param);

	/**
	 * 新增
	 */
	Integer add(SystemRoleMenu bean);

	/**
	 * 批量新增
	 */
	Integer addBatch(List<SystemRoleMenu> listBean);

	/**
	 * 批量新增/修改
	 */
	Integer addOrUpdateBatch(List<SystemRoleMenu> listBean);

	/**
	 * 多条件更新
	 */
	Integer updateByParam(SystemRoleMenu bean,SystemRoleMenuQuery param);

	/**
	 * 多条件删除
	 */
	Integer deleteByParam(SystemRoleMenuQuery param);

	/**
	 * 根据Id查询对象
	 */
	SystemRoleMenu getSystemRoleMenuById(Integer id);


	/**
	 * 根据Id修改
	 */
	Integer updateSystemRoleMenuById(SystemRoleMenu bean,Integer id);


	/**
	 * 根据Id删除
	 */
	Integer deleteSystemRoleMenuById(Integer id);

}