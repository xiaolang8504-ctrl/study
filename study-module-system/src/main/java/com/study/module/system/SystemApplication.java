package com.study.module.system;

import com.spring4all.swagger.EnableSwagger2Doc;
import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 系统服务
 */
@SpringBootApplication(scanBasePackages = {"com.yunshang.budget","com.study.*"})
@MapperScan({"com.study.module.system.*.mapper"})
@EnableSwagger2Doc
@EnableDubbo
@EnableAsync
@EnableScheduling
public class SystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(SystemApplication.class, args);
    }
}
