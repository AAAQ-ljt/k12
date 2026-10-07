package com.nexora.mappers;

import com.nexora.entity.po.StudentPointExchange;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 积分兑换记录（手写精简 mapper，与积分体系其余 mapper 同一取舍）。
 */
public interface StudentPointExchangeMapper {

    /** 插入兑换记录；主键重复返回 0 */
    Integer insert(StudentPointExchange record);

    /** 我的兑换记录（按时间倒序） */
    List<StudentPointExchange> selectByUser(@Param("userId") String userId, @Param("limit") int limit);

    /** 某商品已兑换次数（额度类限次判断） */
    Integer countByUserAndItem(@Param("userId") String userId, @Param("itemCode") String itemCode);

    /** 我兑换过的商品码（音色解锁集合） */
    List<String> selectItemCodesByUser(@Param("userId") String userId);
}
