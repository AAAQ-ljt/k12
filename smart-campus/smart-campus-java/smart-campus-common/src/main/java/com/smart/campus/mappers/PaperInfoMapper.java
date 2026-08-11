package com.smart.campus.mappers;

import org.apache.ibatis.annotations.Param;

/**
 * 试卷信息表 数据库操作接口
 */
public interface PaperInfoMapper<T,P> extends BaseMapper<T,P> {

	/**
	 * 根据PaperId更新
	 */
	 Integer updateByPaperId(@Param("bean") T t,@Param("paperId") String paperId);


	/**
	 * 根据PaperId删除
	 */
	 Integer deleteByPaperId(@Param("paperId") String paperId);


	/**
	 * 根据PaperId获取对象
	 */
	 T selectByPaperId(@Param("paperId") String paperId);


}
