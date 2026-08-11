package com.smart.campus.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Value("${project.folder}")
    private String projectFolder;

    @Value("${resource.upload-session-ttl-minutes:120}")
    private Long resourceUploadSessionTtlMinutes;

    public String getProjectFolder() {
        return projectFolder;
    }

    public Long getResourceUploadSessionTtlMinutes() {
        return resourceUploadSessionTtlMinutes;
    }
}
