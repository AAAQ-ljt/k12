package com.smart.campus.entity.vo;

import java.util.ArrayList;
import java.util.List;

public class ClassImportResultVO {

    private Integer successCount;

    private Integer failureCount;

    private List<String> failureMessages;

    public ClassImportResultVO() {
        this.successCount = 0;
        this.failureCount = 0;
        this.failureMessages = new ArrayList<>();
    }

    public Integer getSuccessCount() {
        return successCount;
    }

    public void setSuccessCount(Integer successCount) {
        this.successCount = successCount;
    }

    public Integer getFailureCount() {
        return failureCount;
    }

    public void setFailureCount(Integer failureCount) {
        this.failureCount = failureCount;
    }

    public List<String> getFailureMessages() {
        return failureMessages;
    }

    public void setFailureMessages(List<String> failureMessages) {
        this.failureMessages = failureMessages;
    }
}
