package com.nexora.entity.query;

import lombok.Data;

/**
 * 积分流水分页查询条件（我的积分明细）。
 */
@Data
public class StudentPointRecordQuery {

    private String userId;

    /** 来源筛选（可空） */
    private String bizType;

    /** 创建时间起（yyyy-MM-dd，可空） */
    private String createTimeStart;

    /** 创建时间止（yyyy-MM-dd，可空） */
    private String createTimeEnd;

    private Integer pageNo = 1;

    private Integer pageSize = 20;
}
