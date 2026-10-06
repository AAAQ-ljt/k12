package com.nexora.mappers;

import com.nexora.entity.po.StudentPointRecord;
import com.nexora.entity.query.StudentPointRecordQuery;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * 积分流水（手写精简 mapper）。
 *
 * 幂等核心：insertIgnore 走 INSERT ... ON DUPLICATE KEY UPDATE record_id = record_id，
 * **返回 0 表示撞上 uk_user_biz（重复发分）**，调用方据此回滚本次加分。
 */
public interface StudentPointRecordMapper {

    /** 插入流水；命中唯一键（重复）时返回 0 */
    Integer insertIgnore(StudentPointRecord record);

    /** 某学生某来源是否已有流水（发分前的快速幂等判断，避免走回滚） */
    Integer countByUserAndBiz(@Param("userId") String userId,
                              @Param("bizType") String bizType,
                              @Param("bizId") String bizId);

    /** 某学生当日已获积分（用于每日上限；bizTypeList 为空表示不限来源） */
    Integer sumPointsByUserAndTimeRange(@Param("userId") String userId,
                                        @Param("start") Date start,
                                        @Param("end") Date end,
                                        @Param("bizTypeList") List<String> bizTypeList);

    /** 某学生某来源累计获得积分（徽章规则 TOTAL_POINTS/CODING_COUNT 等用） */
    Integer sumPointsByUserAndBizTypes(@Param("userId") String userId,
                                       @Param("bizTypeList") List<String> bizTypeList);

    /** 某学生某来源的流水条数（徽章规则 MASTERY_COUNT / CODING_COUNT / CREATION 等计数用） */
    Integer countByUserAndBizTypes(@Param("userId") String userId,
                                   @Param("bizTypeList") List<String> bizTypeList);

    /** 流水条数（分页总数） */
    Integer selectCountByParam(StudentPointRecordQuery query);

    /** 流水分页列表（我的积分明细） */
    List<StudentPointRecord> selectListByParam(StudentPointRecordQuery query);
}
