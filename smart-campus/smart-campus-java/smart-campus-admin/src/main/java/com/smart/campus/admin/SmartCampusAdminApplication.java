package com.smart.campus.admin;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.smart.campus.*")
@MapperScan("com.smart.campus.mappers")
public class SmartCampusAdminApplication {
    public static void main(String[] args) {
        SpringApplication.run(SmartCampusAdminApplication.class, args);
    }
}
