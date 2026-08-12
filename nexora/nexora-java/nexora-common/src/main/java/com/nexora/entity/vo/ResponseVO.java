package com.nexora.entity.vo;


import com.nexora.entity.enums.ResponseCodeEnum;


public class ResponseVO<T> {
    private String status;
    private Integer code;
    private String info;
    private T data;

    /**
     * 构建成功响应（无数据）
     */
    public static <T> ResponseVO<T> success() {
        return success(null);
    }

    /**
     * 构建成功响应（带数据）
     */
    public static <T> ResponseVO<T> success(T data) {
        ResponseVO<T> vo = new ResponseVO<>();
        vo.setStatus("success");
        vo.setCode(ResponseCodeEnum.CODE_200.getCode());
        vo.setInfo(ResponseCodeEnum.CODE_200.getMsg());
        vo.setData(data);
        return vo;
    }

    /**
     * 构建错误响应
     */
    public static <T> ResponseVO<T> error(Integer code, String info) {
        ResponseVO<T> vo = new ResponseVO<>();
        vo.setStatus("error");
        vo.setCode(code);
        vo.setInfo(info);
        return vo;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }
}
