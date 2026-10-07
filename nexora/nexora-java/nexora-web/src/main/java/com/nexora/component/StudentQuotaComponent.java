package com.nexora.component;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

/**
 * 学生个人知识库配额（**唯一口径**，二期 A-7）。
 *
 * 配额 = 基础配额（配置 `resource.student-quota-mb`，默认 300MB）+ 积分兑换扩容（可多次，永久有效）。
 * 上传校验与容量展示都必须走本组件，避免两处各算一套出现「界面上还有空间、上传却被拒」这种不一致。
 */
@Component
public class StudentQuotaComponent {

    /** 基础配额（MB） */
    @Value("${resource.student-quota-mb:300}")
    private long baseQuotaMb;

    @Resource
    private PointExchangeComponent pointExchangeComponent;

    /** 该学生的配额（MB）：基础 + 已兑换扩容 */
    public long quotaMb(String userId) {
        return baseQuotaMb + pointExchangeComponent.uploadQuotaBonusMb(userId);
    }

    /** 该学生的配额（字节） */
    public long quotaBytes(String userId) {
        return quotaMb(userId) * 1024 * 1024;
    }

    /** 基础配额（MB），仅供展示「基础 + 兑换」明细 */
    public long baseQuotaMbValue() {
        return baseQuotaMb;
    }
}
