package com.nexora.mappers;

import org.apache.ibatis.annotations.Param;

/**
 * 系统全局配置表 数据库操作接口
 */
public interface SystemConfigMapper<T,P> extends BaseMapper<T,P> {

	/**
	 * 根据ConfigId更新
	 */
	 Integer updateByConfigId(@Param("bean") T t,@Param("configId") Integer configId);


	/**
	 * 根据ConfigId删除
	 */
	 Integer deleteByConfigId(@Param("configId") Integer configId);


	/**
	 * 根据ConfigId获取对象
	 */
	 T selectByConfigId(@Param("configId") Integer configId);


	/**
	 * 根据ConfigGroupAndConfigKey更新
	 */
	 Integer updateByConfigGroupAndConfigKey(@Param("bean") T t,@Param("configGroup") String configGroup,@Param("configKey") String configKey);


	/**
	 * 根据ConfigGroupAndConfigKey删除
	 */
	 Integer deleteByConfigGroupAndConfigKey(@Param("configGroup") String configGroup,@Param("configKey") String configKey);


	/**
	 * 根据ConfigGroupAndConfigKey获取对象
	 */
	 T selectByConfigGroupAndConfigKey(@Param("configGroup") String configGroup,@Param("configKey") String configKey);


}
