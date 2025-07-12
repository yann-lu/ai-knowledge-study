package com.memory.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@ComponentScan(basePackages = {"com.memory.app"})  // 只扫描com.memory.app包
@EntityScan(basePackages = {"com.memory.app.model"})  // 只扫描com.memory.app.model包中的实体
@EnableJpaRepositories(basePackages = {"com.memory.app.repository"})  // 只扫描com.memory.app.repository包中的存储库
public class EbbinghausMemoryApplication {

    public static void main(String[] args) {
        SpringApplication.run(EbbinghausMemoryApplication.class, args);
    }
}