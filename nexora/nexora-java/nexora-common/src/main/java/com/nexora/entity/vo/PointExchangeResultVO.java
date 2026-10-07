package com.nexora.entity.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 兑换结果（二期 A-7）：用于学生端兑换成功后的即时反馈。
 */
@Data
public class PointExchangeResultVO implements Serializable {

    /** 兑换单号（可对上管理端流水/兑换记录） */
    private String exchangeId;

    /** 商品名 */
    private String itemName;

    /** 本次花费可用积分 */
    private Integer costPoints;

    /** 兑换后可用积分 */
    private Integer availablePoints;

    /** 提示文案，如「已扩容 200MB，去知识中心上传吧」 */
    private String tip;
}
