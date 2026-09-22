package com.yunshang.budget.common.security.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * 安全模块配置
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    SecurityConfig securityConfig;

    /**
     * 配置 HTTP 安全策略。
     */
    @Override
    protected void configure(HttpSecurity httpSecurity) throws Exception {
        // 关闭CSRF安全校验
        httpSecurity.cors().and().csrf().disable();
        // 禁用 session
        httpSecurity.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        // 禁用匿名访问
//        httpSecurity.anonymous().disable();
        // 禁用记住我
        httpSecurity.rememberMe().disable();
        // 过滤规则
        httpSecurity.authorizeRequests()
                // 账号
                .antMatchers(securityConfig.ignoreUrlsConfig().getUrls()).permitAll()
                // 已签发的下载签名可供浏览器直接使用；签名生成、上传和下载地址生成仍要求登录。
                .antMatchers("/api/file/downloadFile").permitAll()
                .anyRequest().authenticated();
        // 放入自定义拦截器
        httpSecurity.addFilterBefore(securityConfig.jwtFilter(), UsernamePasswordAuthenticationFilter.class);
        // 安全认证，异常管控
        httpSecurity.exceptionHandling()
                .authenticationEntryPoint(securityConfig.jwtAuthenticationEntryPoint())
                .accessDeniedHandler(securityConfig.restfulAccessDeniedHandler());
        // 头部信息
        httpSecurity.headers().cacheControl();
    }
}
