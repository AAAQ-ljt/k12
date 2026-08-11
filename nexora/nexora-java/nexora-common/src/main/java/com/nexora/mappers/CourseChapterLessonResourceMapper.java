package com.nexora.mappers;

import org.apache.ibatis.annotations.Param;

/**
 * 课时资源关联表 数据库操作接口
 */
public interface CourseChapterLessonResourceMapper<T,P> extends BaseMapper<T,P> {

	/**
	 * 根据Id更新
	 */
	 Integer updateById(@Param("bean") T t,@Param("id") Integer id);


	/**
	 * 根据Id删除
	 */
	 Integer deleteById(@Param("id") Integer id);


	/**
	 * 根据Id获取对象
	 */
	 T selectById(@Param("id") Integer id);


}
