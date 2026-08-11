package com.smart.campus.mappers;

import java.util.List;

import org.apache.ibatis.annotations.Param;

/**
 * 课程作业/考试学生提交表 数据库操作接口
 */
public interface CourseAssessmentSubmitMapper<T,P> extends BaseMapper<T,P> {

	/**
	 * 根据SubmitId更新
	 */
	 Integer updateBySubmitId(@Param("bean") T t,@Param("submitId") Long submitId);


	/**
	 * 根据SubmitId删除
	 */
	 Integer deleteBySubmitId(@Param("submitId") Long submitId);


	/**
	 * 根据SubmitId获取对象
	 */
	 T selectBySubmitId(@Param("submitId") Long submitId);


	/**
	 * 根据TaskIdAndUserIdAndPaperId更新
	 */
	 Integer updateByTaskIdAndUserIdAndPaperId(@Param("bean") T t,@Param("taskId") String taskId,@Param("userId") Integer userId,@Param("paperId") String paperId);


	/**
	 * 根据TaskIdAndUserIdAndPaperId删除
	 */
	 Integer deleteByTaskIdAndUserIdAndPaperId(@Param("taskId") String taskId,@Param("userId") Integer userId,@Param("paperId") String paperId);


	/**
	 * 根据TaskIdAndUserIdAndPaperId获取对象
	 */
	 T selectByTaskIdAndUserIdAndPaperId(@Param("taskId") String taskId,@Param("userId") Integer userId,@Param("paperId") String paperId);

	/**
	 * 批量查询课时和学生对应的提交记录
	 */
	 List<T> selectByTaskIdListAndUserIdList(@Param("taskIdList") List<String> taskIdList,
											 @Param("userIdList") List<Integer> userIdList,
											 @Param("taskType") Integer taskType);

}
