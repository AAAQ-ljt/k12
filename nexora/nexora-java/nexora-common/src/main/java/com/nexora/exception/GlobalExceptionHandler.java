package com.nexora.exception;

import com.nexora.entity.enums.ResponseCodeEnum;
import com.nexora.entity.vo.ResponseVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * 全局异常处理器
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 捕获业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseVO<?> handleBusinessException(BusinessException e) {
        logger.warn("业务异常: {}", e.getMessage());
        Integer code = e.getCode() != null ? e.getCode() : ResponseCodeEnum.CODE_500.getCode();
        return error(code, e.getMessage());
    }

    /**
     * 捕获参数校验异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseVO<?> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getField)
                .collect(Collectors.joining(", "));
        logger.warn("参数校验异常: {}", message);
        return error(ResponseCodeEnum.CODE_600.getCode(), "参数校验失败: " + message);
    }

    /**
     * 捕获系统异常
     */
    @ExceptionHandler(Exception.class)
    public ResponseVO<?> handleException(Exception e) {
        logger.error("系统异常: ", e);
        return error(ResponseCodeEnum.CODE_500.getCode(), ResponseCodeEnum.CODE_500.getMsg());
    }

    /**
     * 构建错误响应
     */
    private ResponseVO<Object> error(Integer code, String info) {
        ResponseVO<Object> responseVO = new ResponseVO<>();
        responseVO.setStatus("error");
        responseVO.setCode(code);
        responseVO.setInfo(info);
        return responseVO;
    }
}
