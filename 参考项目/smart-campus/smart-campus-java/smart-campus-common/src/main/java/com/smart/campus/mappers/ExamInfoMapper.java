package com.smart.campus.mappers;

import org.apache.ibatis.annotations.Param;

/**
 * 在线考试表 数据库操作接口
 */
public interface ExamInfoMapper<T,P> extends BaseMapper<T,P> {

	/**
	 * 根据ExamId更新
	 */
	 Integer updateByExamId(@Param("bean") T t,@Param("examId") String examId);


	/**
	 * 根据ExamId删除
	 */
	 Integer deleteByExamId(@Param("examId") String examId);


	/**
	 * 根据ExamId获取对象
	 */
	 T selectByExamId(@Param("examId") String examId);


}
