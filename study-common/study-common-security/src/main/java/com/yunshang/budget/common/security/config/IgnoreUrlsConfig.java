package com.yunshang.budget.common.security.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 权限放行URL
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "security.ignore")
public class IgnoreUrlsConfig {

    private String[] urls = new String[]{};
}
