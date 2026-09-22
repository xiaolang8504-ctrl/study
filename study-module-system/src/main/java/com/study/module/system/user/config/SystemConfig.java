package com.study.module.system.user.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 系统配置
 */
@Configuration
@ConfigurationProperties(prefix = "system")
@Data
public class SystemConfig {

    /**
     * 系统管理员账号
     */
    private Long adminId;

    /**
     * 是否邮箱校验
     */
    private String emailValidation;
}
