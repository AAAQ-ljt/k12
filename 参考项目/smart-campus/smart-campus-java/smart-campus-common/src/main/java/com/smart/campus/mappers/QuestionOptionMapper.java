package com.smart.campus.mappers;

import org.apache.ibatis.annotations.Param;

/**
 * 习题选项表 数据库操作接口
 */
public interface QuestionOptionMapper<T,P> extends BaseMapper<T,P> {

	/**
	 * 根据OptionId更新
	 */
	 Integer updateByOptionId(@Param("bean") T t,@Param("optionId") Integer optionId);


	/**
	 * 根据OptionId删除
	 */
	 Integer deleteByOptionId(@Param("optionId") Integer optionId);


	/**
	 * 根据OptionId获取对象
	 */
	 T selectByOptionId(@Param("optionId") Integer optionId);


}
