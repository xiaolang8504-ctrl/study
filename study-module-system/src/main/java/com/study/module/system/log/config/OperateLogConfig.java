package com.study.module.system.log.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

/**
 * 操作日志配置
 */
@Data
@ConfigurationProperties(prefix = "log")
@Configuration
public class OperateLogConfig {

    /**
     * 操作类型配置
     */
    private Map<String, LogTextConfig> operateType;
}
