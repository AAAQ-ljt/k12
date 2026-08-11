package com.nexora.mappers;

import org.apache.ibatis.annotations.Param;

/**
 * 动画模板库 数据库操作接口
 */
public interface AnimationTemplateMapper<T,P> extends BaseMapper<T,P> {

	/**
	 * 根据TemplateId更新
	 */
	 Integer updateByTemplateId(@Param("bean") T t,@Param("templateId") Integer templateId);


	/**
	 * 根据TemplateId删除
	 */
	 Integer deleteByTemplateId(@Param("templateId") Integer templateId);


	/**
	 * 根据TemplateId获取对象
	 */
	 T selectByTemplateId(@Param("templateId") Integer templateId);


}
