package com.nexora.mappers;

import org.apache.ibatis.annotations.Param;

/**
 * 用户表 数据库操作接口
 */
public interface UserInfoMapper<T,P> extends BaseMapper<T,P> {

	/**
	 * 根据UserId更新
	 */
	 Integer updateByUserId(@Param("bean") T t,@Param("userId") String userId);


	/**
	 * 根据UserId删除
	 */
	 Integer deleteByUserId(@Param("userId") String userId);


	/**
	 * 根据UserId获取对象
	 */
	 T selectByUserId(@Param("userId") String userId);


	/**
	 * 根据Username更新
	 */
	 Integer updateByUsername(@Param("bean") T t,@Param("username") String username);


	/**
	 * 根据Username删除
	 */
	 Integer deleteByUsername(@Param("username") String username);


	/**
	 * 根据Username获取对象
	 */
	 T selectByUsername(@Param("username") String username);


	/**
	 * 根据Email更新
	 */
	 Integer updateByEmail(@Param("bean") T t,@Param("email") String email);


	/**
	 * 根据Email删除
	 */
	 Integer deleteByEmail(@Param("email") String email);


	/**
	 * 根据Email获取对象
	 */
	 T selectByEmail(@Param("email") String email);


}
