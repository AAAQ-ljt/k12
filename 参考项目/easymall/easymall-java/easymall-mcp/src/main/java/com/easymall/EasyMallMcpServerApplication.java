package com.easymall;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableAsync
@MapperScan(basePackages = {"com.easymall.mappers"})
@EnableTransactionManagement
@SpringBootApplication(scanBasePackages = {"com.easymall"})
public class EasyMallMcpServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(EasyMallMcpServerApplication.class, args);
    }

}