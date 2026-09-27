package com.nexora.admin.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 跨域配置 - 允许前端访问
 *
 * 注意：Vite 开发服务器代理会把浏览器的 Origin 原样透传给后端，Origin 不在白名单时
 * Spring 直接返回 403 Invalid CORS request（管理端登录进不去的常见原因）。
 * 联调/演示时前端常从局域网 IP 或机器名访问，故除本机精确 Origin 外，
 * 另按 cors.allowed-origin-patterns 放行本机与内网地址（可用环境变量覆盖）。
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Value("${cors.allowed-origin-patterns}")
    private String[] allowedOriginPatterns;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(
                    "http://localhost:3001",
                    "http://127.0.0.1:3001",
                    "http://localhost:4173",
                    "http://127.0.0.1:4173",
                    "http://localhost:5173",
                    "http://127.0.0.1:5173"
                )
                .allowedOriginPatterns(allowedOriginPatterns)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}

