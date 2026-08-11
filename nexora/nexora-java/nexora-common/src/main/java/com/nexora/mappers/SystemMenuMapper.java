package com.nexora.mappers;

import org.apache.ibatis.annotations.Param;

/**
 * 系统菜单表 数据库操作接口
 */
public interface SystemMenuMapper<T,P> extends BaseMapper<T,P> {

	/**
	 * 根据MenuId更新
	 */
	 Integer updateByMenuId(@Param("bean") T t,@Param("menuId") Integer menuId);


	/**
	 * 根据MenuId删除
	 */
	 Integer deleteByMenuId(@Param("menuId") Integer menuId);


	/**
	 * 根据MenuId获取对象
	 */
	 T selectByMenuId(@Param("menuId") Integer menuId);


	/**
	 * 根据MenuCode更新
	 */
	 Integer updateByMenuCode(@Param("bean") T t,@Param("menuCode") String menuCode);


	/**
	 * 根据MenuCode删除
	 */
	 Integer deleteByMenuCode(@Param("menuCode") String menuCode);


	/**
	 * 根据MenuCode获取对象
	 */
	 T selectByMenuCode(@Param("menuCode") String menuCode);


}
