package com.nexora;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;


@EnableAsync
@SpringBootApplication(scanBasePackages = {"com.nexora"})
@MapperScan(basePackages = {"com.nexora.mappers"})
@EnableTransactionManagement
@EnableScheduling
public class NexoraAdminApplication {
    public static void main(String[] args) {
        SpringApplication.run(NexoraAdminApplication.class, args);
    }
}
