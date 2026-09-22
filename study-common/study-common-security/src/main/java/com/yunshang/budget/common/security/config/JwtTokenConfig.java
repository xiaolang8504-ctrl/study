package com.yunshang.budget.common.security.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * token配置
 */
@Configuration
@ConfigurationProperties(prefix = "jwt.token")
@Data
public class JwtTokenConfig {

    /**
     * token前缀
     */
    public String tokenHead = "Bearer ";

    /**
     * token的key
     */
    public String tokenHeader = "Authorization";

    /**
     * token密钥
     */
    private String secretKey;

    /**
     * 过期时间
     */
    private Integer tokenExpire;

    /**
     * 刷新TOKEN过期时间
     */
    private Integer refreshTokenExpire;
}
