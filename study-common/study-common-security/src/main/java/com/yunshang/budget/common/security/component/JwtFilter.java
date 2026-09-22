package com.yunshang.budget.common.security.component;

import com.alibaba.fastjson.JSONObject;
import com.study.common.core.constants.RedisKey;
import com.study.common.core.enums.ErrorCodeConstants;
import com.study.common.core.exception.LogicException;
import com.yunshang.budget.common.redis.RedisService;
import com.yunshang.budget.common.security.config.JwtTokenConfig;
import com.yunshang.budget.common.security.config.SecurityConfig;
import com.yunshang.budget.common.security.domain.TokenInfo;
import io.jsonwebtoken.ExpiredJwtException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * 无状态拦截器实现
 */
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    RedisService redisService;

    @Autowired
    JwtTokenConfig jwtTokenConfig;

    @Autowired
    SecurityConfig securityConfig;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String authHeader = request.getHeader(jwtTokenConfig.tokenHeader);
        if (!isValidAuthHeader(request, authHeader)) {
            chain.doFilter(request, response);
            return;
        }
        // 获取到真实的token
        String authToken = JwtUtils.getRealAuthorizationToken(authHeader, jwtTokenConfig.getTokenHead());
        if (authToken.isBlank()) {
            throw new LogicException(ErrorCodeConstants.NO_LOGIN);
        }

        TokenInfo tokenInfo;
        try {
            // 先验证签名和有效期；过期令牌必须在此处进入续签分支，不能在此之前解析。
            JwtUtils.parserAuthenticateToken(authToken, jwtTokenConfig.getSecretKey());
            tokenInfo = JwtUtils.parserTokenInfo(authToken, jwtTokenConfig.getSecretKey());
        } catch (ExpiredJwtException e) {
            // ExpiredJwtException 中的 claims 已通过签名校验，只用于定位同一用户的 refresh token。
            handleTokenExpired(response, JwtUtils.parserTokenInfo(e.getClaims()).getUserId());
            // 过期后续签完成，本次请求不再设置认证上下文，由客户端携带新 token 重试
            chain.doFilter(request, response);
            return;
        }

        //判断是否在安全的时间内(30分钟内就刷新token)
        if (JwtUtils.isTokenExpire(authToken, jwtTokenConfig.getSecretKey())) {
            // 重新签发 token
            buildTokenByAuthentication(response, authToken);
        }

        //构建认证过的认证对象
        buildAuthentication(request, tokenInfo.getUserId().toString());
        chain.doFilter(request, response);
    }

    // 提取和校验令牌的相关方法
    /**
     * 校验业务数据。
     */
    private boolean isValidAuthHeader(HttpServletRequest request, String authHeader) {
        if (authHeader == null || !authHeader.startsWith(jwtTokenConfig.getTokenHead())) {
            return false;
        }
        return !isIgnoreUrl(securityConfig.ignoreUrlsConfig().getUrls(), request.getRequestURI());
    }

    /**
     * 判断数组里是否包含指定字符串
     */
    public static boolean isIgnoreUrl(String[] ignoreUrl, String url) {
        for (String prefix : ignoreUrl) {
            if (url.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 重新生成token并认证
     */
    private void buildTokenByAuthentication(HttpServletResponse response, String authToken) {
        TokenInfo tokenInfo = JwtUtils.parserTokenInfo(authToken, jwtTokenConfig.getSecretKey());
        String token = JwtUtils.generatorJwtToken(tokenInfo, jwtTokenConfig.getSecretKey(), jwtTokenConfig.getTokenExpire());
        response.addHeader(jwtTokenConfig.tokenHeader, jwtTokenConfig.getTokenHead() + token);
    }

    /**
     * 处理token过期情况
     */
    private void handleTokenExpired(HttpServletResponse response, Long userId) {
        Object cached = redisService.get(RedisKey.SYSTEM_USER_REFRESH_TOKEN + ":" + userId);
        if (cached == null) {
            throw new LogicException(ErrorCodeConstants.NO_LOGIN);
        }
        // 获取刷新 token
        String refreshTokenHeader = cached.toString();
        // 检测 refresh-token 是否是我们系统中签发的
        if (!JwtUtils.isTokenAuthorizationHeader(refreshTokenHeader, jwtTokenConfig.getTokenHead(),jwtTokenConfig.getSecretKey())) {
            throw new LogicException(ErrorCodeConstants.NO_LOGIN);
        }
        // 解析 refresh-token
        String refreshToken = JwtUtils.getRealAuthorizationToken(refreshTokenHeader, jwtTokenConfig.getTokenHead());
        if (refreshToken.isBlank()) {
            throw new LogicException(ErrorCodeConstants.NO_LOGIN);
        }
        // 判断 refresh-token 是否过期
        if (JwtUtils.isTokenExpired(refreshToken, jwtTokenConfig.getSecretKey())) {
            throw new LogicException(ErrorCodeConstants.NO_LOGIN);
        }
        // 重新签发 token
        buildTokenByAuthentication(response, refreshToken);
    }

    /**
     * 构建认证对象
     */
    private void buildAuthentication(HttpServletRequest request, String username) {
        Object cached = redisService.get(RedisKey.SYSTEM_USER_TOKEN + ":" + username);
        if (cached == null) {
            throw new LogicException(ErrorCodeConstants.NO_LOGIN);
        }
        UserDetails userDetails = JSONObject.parseObject(cached.toString(), UserDetails.class);
        Set<GrantedAuthority> authorities = new HashSet<>(userDetails.getAuthorities());
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(Long.valueOf(username), null, authorities);
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
