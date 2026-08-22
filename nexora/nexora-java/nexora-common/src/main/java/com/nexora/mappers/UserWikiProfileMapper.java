package com.nexora.mappers;

import org.apache.ibatis.annotations.Param;

/**
 * 学生学习档案 数据库操作接口
 */
public interface UserWikiProfileMapper<T, P> extends BaseMapper<T, P> {

    /**
     * 根据UserId更新
     */
    Integer updateByUserId(@Param("bean") T t, @Param("userId") String userId);

    /**
     * 根据UserId删除
     */
    Integer deleteByUserId(@Param("userId") String userId);

    /**
     * 根据UserId获取对象
     */
    T selectByUserId(@Param("userId") String userId);
}