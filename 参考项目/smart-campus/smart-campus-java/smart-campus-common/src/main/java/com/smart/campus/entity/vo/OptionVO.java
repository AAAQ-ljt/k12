package com.smart.campus.entity.vo;

public class OptionVO<T> {

    private T value;

    private String label;

    public OptionVO() {
    }

    public OptionVO(T value, String label) {
        this.value = value;
        this.label = label;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
