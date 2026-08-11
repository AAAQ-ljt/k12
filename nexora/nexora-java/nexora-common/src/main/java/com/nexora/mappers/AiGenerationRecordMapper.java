package com.nexora.mappers;

import org.apache.ibatis.annotations.Param;

/**
 * AI生成记录表 数据库操作接口
 */
public interface AiGenerationRecordMapper<T,P> extends BaseMapper<T,P> {

	/**
	 * 根据RecordId更新
	 */
	 Integer updateByRecordId(@Param("bean") T t,@Param("recordId") String recordId);


	/**
	 * 根据RecordId删除
	 */
	 Integer deleteByRecordId(@Param("recordId") String recordId);


	/**
	 * 根据RecordId获取对象
	 */
	 T selectByRecordId(@Param("recordId") String recordId);


}
