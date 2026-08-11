package com.smart.campus.web;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.smart.campus.*")
@MapperScan("com.smart.campus.mappers")
@EnableScheduling
public class SmartCampusWebApplication {
    public static void main(String[] args) {
        SpringApplication.run(SmartCampusWebApplication.class, args);
    }
}
