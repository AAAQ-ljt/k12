package com.easymall;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;


@EnableAsync
@SpringBootApplication(scanBasePackages = {"com.easymall"})
@MapperScan(basePackages = {"com.easymall.mappers"})
@EnableTransactionManagement
@EnableScheduling
public class EasyMallAdminApplication {
    public static void main(String[] args) {
        SpringApplication.run(EasyMallAdminApplication.class, args);
    }
}
