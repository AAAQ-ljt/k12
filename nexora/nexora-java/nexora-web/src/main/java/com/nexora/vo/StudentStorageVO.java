package com.nexora.vo;

/**
 * 学生个人知识库存储信息
 */
public class StudentStorageVO {

    /**
     * 已用空间（字节）
     */
    private Long usedBytes;

    /**
     * 总额度（字节）
     */
    private Long quotaBytes;

    /**
     * 剩余空间（字节）
     */
    private Long remainingBytes;

    /**
     * 是否已初始化个人知识库
     */
    private Boolean initialized;

    public Long getUsedBytes() {
        return usedBytes;
    }

    public void setUsedBytes(Long usedBytes) {
        this.usedBytes = usedBytes;
    }

    public Long getQuotaBytes() {
        return quotaBytes;
    }

    public void setQuotaBytes(Long quotaBytes) {
        this.quotaBytes = quotaBytes;
    }

    public Long getRemainingBytes() {
        return remainingBytes;
    }

    public void setRemainingBytes(Long remainingBytes) {
        this.remainingBytes = remainingBytes;
    }

    public Boolean getInitialized() {
        return initialized;
    }

    public void setInitialized(Boolean initialized) {
        this.initialized = initialized;
    }
}
