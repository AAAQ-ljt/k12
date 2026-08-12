package com.easymall.entity.dto;

public class RagDataDTO {
    private String dataId;
    private String type;

    public RagDataDTO() {
    }

    public RagDataDTO(String dataId, String type) {
        this.dataId = dataId;
        this.type = type;
    }

    public String getDataId() {
        return dataId;
    }

    public void setDataId(String dataId) {
        this.dataId = dataId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
