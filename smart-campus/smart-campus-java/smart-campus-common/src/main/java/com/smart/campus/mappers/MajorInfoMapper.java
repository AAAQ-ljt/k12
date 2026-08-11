package com.smart.campus.mappers;

import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 专业表 数据库操作接口
 */
public interface MajorInfoMapper<T, P> extends BaseMapper<T, P> {

    Integer updateByMajorId(@Param("bean") T t, @Param("majorId") Integer majorId);

    Integer deleteByMajorId(@Param("majorId") Integer majorId);

    T selectByMajorId(@Param("majorId") Integer majorId);

    Integer updateByMajorCode(@Param("bean") T t, @Param("majorCode") String majorCode);

    Integer deleteByMajorCode(@Param("majorCode") String majorCode);

    T selectByMajorCode(@Param("majorCode") String majorCode);

    List<T> selectByMajorIdList(@Param("majorIdList") List<Integer> majorIdList);

    Integer updateSortOrderBatch(@Param("list") List<T> list);
}
