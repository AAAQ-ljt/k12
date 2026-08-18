package com.nexora.admin.interceptor;

import jakarta.annotation.Resource;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class WebAppConfigurer implements WebMvcConfigurer {

    @Resource
    private AppInterceptor appInterceptor;

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {

    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(appInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/account/login",
                        "/account/logout",
                        "/error",
                        "/resourceInfo/video/**",
                        "/resourceInfo/image/**",
                        "/resourceInfo/file/**",
                        "/resourceInfo/download/**",
                        "/resourceInfo/studentVideo/**",
                        "/resourceInfo/studentImage/**",
                        "/resourceInfo/studentFile/**",
                        "/resourceInfo/studentDownload/**"
                );
    }

    /**
     * 配置静态资源
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
    }
}
