package com.smart.campus.web.config;

import com.smart.campus.config.AppConfig;
import com.smart.campus.utils.StringTools;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebWebMvcConfig implements WebMvcConfigurer {

    @Resource
    private AppConfig appConfig;

    private final WebLoginInterceptor webLoginInterceptor;

    public WebWebMvcConfig(WebLoginInterceptor webLoginInterceptor) {
        this.webLoginInterceptor = webLoginInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(webLoginInterceptor)
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
