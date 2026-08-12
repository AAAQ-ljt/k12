package com.nexora.annotation;

import org.springframework.web.bind.annotation.Mapping;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 接口拦截注解（Web 层 AOP）：标注在 Controller 方法 / 类上
 * checkLogin = true 时要求登录（默认类级开启，公开接口显式声明 false）
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Mapping
public @interface GlobalInterceptor {

    /**
     * 是否校验登录
     */
    boolean checkLogin() default false;
}
