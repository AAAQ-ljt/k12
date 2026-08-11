package com.nexora.mappers;

import org.apache.ibatis.annotations.Param;

/**
 * 提示词模板表 数据库操作接口
 */
public interface PromptTemplateMapper<T,P> extends BaseMapper<T,P> {

	/**
	 * 根据Id更新
	 */
	 Integer updateById(@Param("bean") T t,@Param("id") Integer id);


	/**
	 * 根据Id删除
	 */
	 Integer deleteById(@Param("id") Integer id);


	/**
	 * 根据Id获取对象
	 */
	 T selectById(@Param("id") Integer id);


	/**
	 * 根据StageAndScene更新
	 */
	 Integer updateByStageAndScene(@Param("bean") T t,@Param("stage") String stage,@Param("scene") String scene);


	/**
	 * 根据StageAndScene删除
	 */
	 Integer deleteByStageAndScene(@Param("stage") String stage,@Param("scene") String scene);


	/**
	 * 根据StageAndScene获取对象
	 */
	 T selectByStageAndScene(@Param("stage") String stage,@Param("scene") String scene);


}
