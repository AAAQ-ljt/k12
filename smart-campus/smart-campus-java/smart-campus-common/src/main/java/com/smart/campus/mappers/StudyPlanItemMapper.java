package com.smart.campus.mappers;

import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 学生学习计划明细表 数据库操作接口
 */
public interface StudyPlanItemMapper<T,P> extends BaseMapper<T,P> {

	/**
	 * 根据ItemId更新
	 */
	 Integer updateByItemId(@Param("bean") T t,@Param("itemId") Long itemId);


	/**
	 * 根据ItemId删除
	 */
	 Integer deleteByItemId(@Param("itemId") Long itemId);


	/**
	 * 根据ItemId获取对象
	 */
	 T selectByItemId(@Param("itemId") Long itemId);

	/**
	 * 根据PlanId列表查询
	 */
	 List<T> selectByPlanIdList(@Param("planIdList") List<String> planIdList);


}
