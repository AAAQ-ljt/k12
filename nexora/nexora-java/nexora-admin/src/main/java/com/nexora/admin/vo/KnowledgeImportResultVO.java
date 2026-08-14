package com.nexora.admin.vo;

import java.util.ArrayList;
import java.util.List;

/**
 * knowledge/ 目录批量导入结果。
 */
public class KnowledgeImportResultVO {

    private Integer successCount = 0;
    private Integer failedCount = 0;
    private List<String> errors = new ArrayList<>();

    public Integer getSuccessCount() {
        return successCount;
    }

    public void setSuccessCount(Integer successCount) {
        this.successCount = successCount;
    }

    public Integer getFailedCount() {
        return failedCount;
    }

    public void setFailedCount(Integer failedCount) {
        this.failedCount = failedCount;
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }
}
