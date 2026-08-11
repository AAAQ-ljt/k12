package com.easymall.entity.vo;

public class OrderCountVO {
    private String code;
    private Integer count;

    public OrderCountVO() {
    }

    public OrderCountVO(String code, Integer count) {
        this.code = code;
        this.count = count;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }
}
