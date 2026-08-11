package com.smart.campus.mappers;

import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 院系表 数据库操作接口
 */
public interface DepartmentInfoMapper<T, P> extends BaseMapper<T, P> {

    Integer updateByDepartmentId(@Param("bean") T t, @Param("departmentId") Integer departmentId);

    Integer deleteByDepartmentId(@Param("departmentId") Integer departmentId);

    T selectByDepartmentId(@Param("departmentId") Integer departmentId);

    Integer updateByDepartmentCode(@Param("bean") T t, @Param("departmentCode") String departmentCode);

    Integer deleteByDepartmentCode(@Param("departmentCode") String departmentCode);

    T selectByDepartmentCode(@Param("departmentCode") String departmentCode);

    List<T> selectByDepartmentIdList(@Param("departmentIdList") List<Integer> departmentIdList);

    Integer deleteBatchByDepartmentIdList(@Param("departmentIdList") List<Integer> departmentIdList);

    Integer updateSortOrderBatch(@Param("list") List<T> list);
}
