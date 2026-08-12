package com.nexora.exception;

import com.nexora.entity.enums.ResponseCodeEnum;
import com.nexora.entity.vo.ResponseVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器：统一把异常转为 ResponseVO，禁止默认 500 页面
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 业务异常：按异常携带的 code 返回（401 登录失效 / 600 参数错误等）
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseVO<Void> handleBusinessException(BusinessException e) {
        Integer code = e.getCode() != null ? e.getCode() : ResponseCodeEnum.CODE_600.getCode();
        return ResponseVO.error(code, e.getMessage());
    }

    /**
     * 请求体参数校验失败
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseVO<Void> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldError() != null
                ? e.getBindingResult().getFieldError().getDefaultMessage()
                : ResponseCodeEnum.CODE_600.getMsg();
        return ResponseVO.error(ResponseCodeEnum.CODE_600.getCode(), msg);
    }

    /**
     * 表单绑定校验失败
     */
    @ExceptionHandler(BindException.class)
    public ResponseVO<Void> handleBindException(BindException e) {
        String msg = e.getBindingResult().getFieldError() != null
                ? e.getBindingResult().getFieldError().getDefaultMessage()
                : ResponseCodeEnum.CODE_600.getMsg();
        return ResponseVO.error(ResponseCodeEnum.CODE_600.getCode(), msg);
    }

    /**
     * 请求体 JSON 解析失败
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseVO<Void> handleHttpMessageNotReadable(HttpMessageNotReadableException e) {
        log.warn("JSON 解析失败: {}", e.getMessage());
        return ResponseVO.error(ResponseCodeEnum.CODE_600.getCode(), "请求体格式错误");
    }

    /**
     * 缺少必需参数 / 请求头
     */
    @ExceptionHandler({MissingServletRequestParameterException.class, MissingRequestHeaderException.class})
    public ResponseVO<Void> handleMissingParam(Exception e) {
        return ResponseVO.error(ResponseCodeEnum.CODE_600.getCode(), e.getMessage());
    }

    /**
     * 兜底异常
     */
    @ExceptionHandler(Exception.class)
    public ResponseVO<Void> handleException(Exception e) {
        log.error("系统异常", e);
        return ResponseVO.error(ResponseCodeEnum.CODE_500.getCode(), ResponseCodeEnum.CODE_500.getMsg());
    }
}
