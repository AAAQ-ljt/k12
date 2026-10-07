package com.nexora.entity.po;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 学生积分兑换记录（二期 A-7）。
 *
 * 与积分流水的关系：每次兑换同时写一条 `student_point_record`（biz_type=EXCHANGE，points 为负，
 * 幂等键=exchange_id）和一条本表记录；流水负责账户口径审计，本表负责商品口径审计
 * （额度类商品要按 item_code 汇总）。
 */
@Data
public class StudentPointExchange implements Serializable {

    /** 兑换单号（主键，天然幂等） */
    private String exchangeId;

    /** 学生 ID */
    private String userId;

    /** 学段快照 */
    private String stage;

    /** 商品码：UPLOAD_QUOTA / VOICE_<音色码> */
    private String itemCode;

    /** 商品名（快照，便于流水展示） */
    private String itemName;

    /** 花费可用积分 */
    private Integer costPoints;

    /** 兑换后可用积分 */
    private Integer balanceAfter;

    /** 兑换时间 */
    private Date createTime;
}
