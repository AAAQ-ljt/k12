package com.nexora.admin.config;

import com.nexora.admin.interceptor.AdminInterceptor;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 管理端 Web 配置 — 注册拦截器、静态资源映射
 */
@Configuration
public class AdminWebConfig implements WebMvcConfigurer {

    @Resource
    private AdminInterceptor adminInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(adminInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/adminInfo/login");
    }
}
