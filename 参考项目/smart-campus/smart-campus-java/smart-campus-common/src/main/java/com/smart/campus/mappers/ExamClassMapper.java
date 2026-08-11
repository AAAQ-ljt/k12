package com.smart.campus.mappers;

import org.apache.ibatis.annotations.Param;

/**
 * 考试班级关联表 数据库操作接口
 */
public interface ExamClassMapper<T,P> extends BaseMapper<T,P> {

	/**
	 * 根据ExamIdAndClassId更新
	 */
	 Integer updateByExamIdAndClassId(@Param("bean") T t,@Param("examId") String examId,@Param("classId") Integer classId);


	/**
	 * 根据ExamIdAndClassId删除
	 */
	 Integer deleteByExamIdAndClassId(@Param("examId") String examId,@Param("classId") Integer classId);


	/**
	 * 根据ExamIdAndClassId获取对象
	 */
	 T selectByExamIdAndClassId(@Param("examId") String examId,@Param("classId") Integer classId);


}
