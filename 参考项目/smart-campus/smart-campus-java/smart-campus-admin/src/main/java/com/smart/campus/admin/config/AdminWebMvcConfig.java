package com.smart.campus.admin.config;

import com.smart.campus.config.AppConfig;
import com.smart.campus.utils.StringTools;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
class AdminWebMvcConfig implements WebMvcConfigurer {

    @Resource
    private AdminLoginInterceptor adminLoginInterceptor;

    @Resource
    private AppConfig appConfig;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(adminLoginInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/login/doLogin", "/login/getCaptcha", "/resourceFile/**");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String baseFolder = StringTools.trim(appConfig.getProjectFolder());
        if (baseFolder == null) {
            return;
        }
        String normalizedFolder = baseFolder.replace("\\", "/");
        if (!normalizedFolder.endsWith("/")) {
            normalizedFolder += "/";
        }
        registry.addResourceHandler("/resourceFile/**")
                .addResourceLocations("file:" + normalizedFolder);
    }
}
