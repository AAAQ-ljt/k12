package com.nexora.entity.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 学习路径积分聚合行（二期 PATH，mapper 投影用）：
 * bizKey 是路径 ID（节点类流水按所属路径归集，完成类流水按 bizId=路径ID 归集）。
 */
@Data
public class PathPointRowVO implements Serializable {

    /** 路径 ID */
    private String bizKey;

    /** 该路径下已获得的积分合计 */
    private Integer points;
}
