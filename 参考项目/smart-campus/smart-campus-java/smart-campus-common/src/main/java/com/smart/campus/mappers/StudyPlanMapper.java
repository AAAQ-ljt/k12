package com.smart.campus.mappers;

import org.apache.ibatis.annotations.Param;

/**
 * 学生学习计划主表 数据库操作接口
 */
public interface StudyPlanMapper<T,P> extends BaseMapper<T,P> {

	/**
	 * 根据PlanId更新
	 */
	 Integer updateByPlanId(@Param("bean") T t,@Param("planId") String planId);


	/**
	 * 根据PlanId删除
	 */
	 Integer deleteByPlanId(@Param("planId") String planId);


	/**
	 * 根据PlanId获取对象
	 */
	 T selectByPlanId(@Param("planId") String planId);


}
