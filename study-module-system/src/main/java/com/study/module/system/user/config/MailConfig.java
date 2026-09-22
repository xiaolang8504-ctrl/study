package com.study.module.system.user.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 系统配置
 */
@Configuration
@ConfigurationProperties(prefix = "mail")
@Data
public class MailConfig {

    /**
     * 地址
     */
    private String host;

    /**
     * 端口号
     */
    private Integer port;

    /**
     * 账号
     */
    private String userName;

    /**
     * 密码
     */
    private String passWord;

    /**
     * 发送地址
     */
    private String fromAddress;
}
