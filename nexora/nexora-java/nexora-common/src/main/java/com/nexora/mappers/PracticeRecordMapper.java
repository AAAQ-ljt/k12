package com.nexora.mappers;

import org.apache.ibatis.annotations.Param;

/**
 * 游戏化练习记录表 数据库操作接口
 */
public interface PracticeRecordMapper<T,P> extends BaseMapper<T,P> {

	/**
	 * 课时测验最近一次作答统计（按课时聚合：最近提交批次的总分/答对数/题数/时间）
	 */
	 java.util.List<com.nexora.entity.vo.LessonQuizAttemptStatVO> selectQuizLastAttemptStats(@Param("userId") String userId,@Param("bizIds") java.util.List<String> bizIds);

	/**
	 * 根据RecordId更新
	 */
	 Integer updateByRecordId(@Param("bean") T t,@Param("recordId") Long recordId);


	/**
	 * 根据RecordId删除
	 */
	 Integer deleteByRecordId(@Param("recordId") Long recordId);


	/**
	 * 根据RecordId获取对象
	 */
	 T selectByRecordId(@Param("recordId") Long recordId);


}
