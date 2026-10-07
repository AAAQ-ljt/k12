package com.nexora.entity.vo;

import com.nexora.entity.po.StudentPointExchange;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 学生端兑换页数据（商品 + 我的兑换记录 + 可用积分），二期 A-7。
 */
@Data
public class PointExchangeListVO implements Serializable {

    /** 我的可用积分 */
    private Integer availablePoints;

    /** 可兑换商品（容量类 + 音色类） */
    private List<PointExchangeItemVO> items = new ArrayList<>();

    /** 我的兑换记录（按时间倒序，最多 20 条） */
    private List<StudentPointExchange> records = new ArrayList<>();
}
