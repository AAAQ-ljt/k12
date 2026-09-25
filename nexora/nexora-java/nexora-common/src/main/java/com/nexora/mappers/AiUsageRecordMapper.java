package com.nexora.mappers;

import org.apache.ibatis.annotations.Param;

/**
 * AI 消耗统计查询
 */
public interface AiUsageRecordMapper {

    /**
     * 累计一次 AI 消耗：同一天 + 同一类型 + 同一模型命中唯一键 uk_date_type_model 时相加，
     * 不落明细行，避免统计表随调用量膨胀。
     */
    Integer upsertUsage(@Param("usageType") String usageType,
                        @Param("model") String model,
                        @Param("callCount") Long callCount,
                        @Param("promptTokens") Long promptTokens,
                        @Param("completionTokens") Long completionTokens);
}
