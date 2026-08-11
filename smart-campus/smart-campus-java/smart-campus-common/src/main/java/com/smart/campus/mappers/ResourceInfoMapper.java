package com.smart.campus.mappers;

import org.apache.ibatis.annotations.Param;

/**
 * 教师资源信息表 数据库操作接口
 */
public interface ResourceInfoMapper<T,P> extends BaseMapper<T,P> {

	/**
	 * 根据ResourceId更新
	 */
	 Integer updateByResourceId(@Param("bean") T t,@Param("resourceId") Integer resourceId);


	/**
	 * 根据ResourceId删除
	 */
	 Integer deleteByResourceId(@Param("resourceId") Integer resourceId);


	/**
	 * 根据ResourceId获取对象
	 */
	 T selectByResourceId(@Param("resourceId") Integer resourceId);


}
