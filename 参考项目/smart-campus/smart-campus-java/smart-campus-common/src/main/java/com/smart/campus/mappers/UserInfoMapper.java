package com.smart.campus.mappers;

import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface UserInfoMapper<T, P> extends BaseMapper<T, P> {

    Integer updateByUserId(@Param("bean") T t, @Param("userId") Integer userId);

    Integer deleteByUserId(@Param("userId") Integer userId);

    T selectByUserId(@Param("userId") Integer userId);

    List<T> selectByUserIdList(@Param("userIdList") List<Integer> userIdList);

    Integer deleteBatchByUserIdList(@Param("userIdList") List<Integer> userIdList);

    Integer updateByUserNo(@Param("bean") T t, @Param("userNo") String userNo);

    Integer deleteByUserNo(@Param("userNo") String userNo);

    T selectByUserNo(@Param("userNo") String userNo);

    Integer updateByPhone(@Param("bean") T t, @Param("phone") String phone);

    Integer deleteByPhone(@Param("phone") String phone);

    T selectByPhone(@Param("phone") String phone);

    List<Integer> selectUsedClassIdList(@Param("classIdList") List<Integer> classIdList);
}
