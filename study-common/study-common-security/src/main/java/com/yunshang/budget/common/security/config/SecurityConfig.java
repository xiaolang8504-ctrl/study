package com.yunshang.budget.common.security.config;

import com.yunshang.budget.common.security.component.JwtAuthenticationEntryPoint;
import com.yunshang.budget.common.security.component.JwtFilter;
import com.yunshang.budget.common.security.component.RestfulAccessDeniedHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Security配置注入
 */
@Configuration
public class SecurityConfig {

    @Bean
    public JwtFilter jwtFilter() {
        return new JwtFilter();
    }

    /**
     * 执行 jwtAuthenticationEntryPoint 业务处理。
     */
    @Bean
    public JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint() {
        return new JwtAuthenticationEntryPoint();
    }

    /**
     * 执行 restfulAccessDeniedHandler 业务处理。
     */
    @Bean
    public RestfulAccessDeniedHandler restfulAccessDeniedHandler() {
        return new RestfulAccessDeniedHandler();
    }

    /**
     * 执行 passwordEncoder 业务处理。
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 执行 ignoreUrlsConfig 业务处理。
     */
    @Bean
    public IgnoreUrlsConfig ignoreUrlsConfig() {
        return new IgnoreUrlsConfig();
    }
}
