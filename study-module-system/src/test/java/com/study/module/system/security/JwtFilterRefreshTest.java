package com.study.module.system.security;

import com.study.common.core.constants.RedisKey;
import com.yunshang.budget.common.redis.RedisService;
import com.yunshang.budget.common.security.component.JwtFilter;
import com.yunshang.budget.common.security.component.JwtUtils;
import com.yunshang.budget.common.security.config.IgnoreUrlsConfig;
import com.yunshang.budget.common.security.config.JwtTokenConfig;
import com.yunshang.budget.common.security.config.SecurityConfig;
import com.yunshang.budget.common.security.domain.TokenInfo;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.util.ReflectionTestUtils;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 已过期访问令牌必须能够使用同一账号的有效 refresh token 续签。
 */
@ExtendWith(MockitoExtension.class)
class JwtFilterRefreshTest {

    private static final String SECRET = "p0-jwt-refresh-regression-secret";

    @Mock
    private RedisService redisService;
    @Mock
    private SecurityConfig securityConfig;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private FilterChain filterChain;

    @AfterEach
    void clearSecurityContext() {
        org.springframework.security.core.context.SecurityContextHolder.clearContext();
    }

    @Test
    void shouldRefreshExpiredAccessTokenBeforeParsingUserContext() throws ServletException, IOException {
        JwtTokenConfig tokenConfig = new JwtTokenConfig();
        tokenConfig.setSecretKey(SECRET);
        tokenConfig.setTokenExpire(1);
        tokenConfig.setRefreshTokenExpire(24);
        IgnoreUrlsConfig ignoreUrlsConfig = new IgnoreUrlsConfig();
        ignoreUrlsConfig.setUrls(new String[0]);
        when(securityConfig.ignoreUrlsConfig()).thenReturn(ignoreUrlsConfig);
        when(request.getRequestURI()).thenReturn("/api/wrongQuestion/wrongQuestionPageList");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + expiredToken(7L));
        when(redisService.get(RedisKey.SYSTEM_USER_REFRESH_TOKEN + ":7"))
                .thenReturn("Bearer " + JwtUtils.generatorJwtToken(tokenInfo(7L), SECRET, 24));

        TestJwtFilter filter = new TestJwtFilter();
        ReflectionTestUtils.setField(filter, "redisService", redisService);
        ReflectionTestUtils.setField(filter, "jwtTokenConfig", tokenConfig);
        ReflectionTestUtils.setField(filter, "securityConfig", securityConfig);

        filter.apply(request, response, filterChain);

        verify(response).addHeader(eq("Authorization"), startsWith("Bearer "));
        verify(filterChain).doFilter(request, response);
        assertNull(org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication());
    }

    private String expiredToken(Long userId) {
        return Jwts.builder()
                .claim("user", tokenInfo(userId))
                .setIssuedAt(new Date(System.currentTimeMillis() - 120_000L))
                .setExpiration(new Date(System.currentTimeMillis() - 60_000L))
                .signWith(SignatureAlgorithm.HS256, SECRET)
                .compact();
    }

    private TokenInfo tokenInfo(Long userId) {
        TokenInfo tokenInfo = new TokenInfo();
        tokenInfo.setUserId(userId);
        tokenInfo.setUsername("student" + userId);
        return tokenInfo;
    }

    private static class TestJwtFilter extends JwtFilter {
        void apply(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
                throws ServletException, IOException {
            doFilterInternal(request, response, chain);
        }
    }
}
