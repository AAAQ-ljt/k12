package com.smart.campus.mappers;

import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ClassInfoMapper<T, P> extends BaseMapper<T, P> {

    Integer updateByClassId(@Param("bean") T t, @Param("classId") Integer classId);

    Integer deleteByClassId(@Param("classId") Integer classId);

    T selectByClassId(@Param("classId") Integer classId);

    List<T> selectByClassIdList(@Param("classIdList") List<Integer> classIdList);

    Integer deleteBatchByClassIdList(@Param("classIdList") List<Integer> classIdList);

    Integer updateSortOrderBatch(@Param("list") List<T> list);
}
