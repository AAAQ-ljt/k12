package com.nexora.mappers;

import org.apache.ibatis.annotations.Param;

/**
 * 知识点掌握度表 数据库操作接口
 */
public interface KnowledgeMasteryMapper<T,P> extends BaseMapper<T,P> {

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


	/**
	 * 根据UserIdAndKnowledgePointId更新
	 */
	 Integer updateByUserIdAndKnowledgePointId(@Param("bean") T t,@Param("userId") String userId,@Param("knowledgePointId") String knowledgePointId);


	/**
	 * 根据UserIdAndKnowledgePointId删除
	 */
	 Integer deleteByUserIdAndKnowledgePointId(@Param("userId") String userId,@Param("knowledgePointId") String knowledgePointId);


	/**
	 * 根据UserIdAndKnowledgePointId获取对象
	 */
	 T selectByUserIdAndKnowledgePointId(@Param("userId") String userId,@Param("knowledgePointId") String knowledgePointId);



    /** 复习闭环（设计点④）：设置/清除静音（muted=1 长期静音；muteUntil 临时静音；两者置空即恢复提醒） */
    Integer updateReviewMute(@Param("userId") String userId,
                             @Param("knowledgePointId") String knowledgePointId,
                             @Param("muted") Integer muted,
                             @Param("muteUntil") java.util.Date muteUntil);

    /** 按学生 + 知识点批量删除掌握度（删路径时清理孤儿知识点，不在循环里查库） */
    Integer deleteByUserAndPoints(@Param("userId") String userId, @Param("pointIds") java.util.List<String> pointIds);
}
