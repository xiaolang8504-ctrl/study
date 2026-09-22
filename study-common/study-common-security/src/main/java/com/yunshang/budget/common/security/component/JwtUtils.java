package com.yunshang.budget.common.security.component;

import com.study.common.core.enums.ErrorCodeConstants;
import com.study.common.core.exception.LogicException;
import com.yunshang.budget.common.security.domain.TokenInfo;
import io.jsonwebtoken.*;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.Map;

/**
 * JWT工具集
 */
public class JwtUtils {

    /**
     * 30分钟（毫秒）
     */
    private static final long REFRESH_THRESHOLD_MS = 30 * 60 * 1000L;

    /**
     * 小时转毫秒系数
     */
    private static final long HOUR_TO_MS = 3600 * 1000L;

    /**
     * 判断是否是系统中登录后签发的token
     */
    public static boolean isTokenAuthorizationHeader(String authorizationHeader, String tokenHead, String secretKey) {
        if (authorizationHeader == null || authorizationHeader.isBlank()) {
            return false;
        }
        // 必须以 tokenHead 开头，否则直接判定非法
        if (!authorizationHeader.startsWith(tokenHead)) {
            return false;
        }
        try {
            String token = getRealAuthorizationToken(authorizationHeader, tokenHead);
            parserAuthenticateToken(token, secretKey);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 获取到真实TOKEN
     */
    public static String getRealAuthorizationToken(String authorizationToken, String tokenHead) {
        return authorizationToken.substring(tokenHead.length());
    }

    /**
     * 解析TOKEN
     */
    public static Claims parserAuthenticateToken(String authToken, String secretKey) {
        try {
            Jws<Claims> jws = Jwts.parser()
                    .setSigningKey(secretKey)
                    .parseClaimsJws(authToken);
            return jws.getBody();
        } catch (ExpiredJwtException e) {
            // 过期异常单独透传，由上层（JwtFilter）捕获决定是否走续签流程
            throw e;
        } catch (Exception e) {
            throw new LogicException(ErrorCodeConstants.NO_LOGIN);
        }
    }

    /**
     * 判断TOKEN是否过期
     */
    public static boolean isTokenExpired(String authToken, String secretKey) {
        try {
            Claims claims = parserAuthenticateToken(authToken, secretKey);
            return claims.getExpiration().before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        }
    }

    /**
     * 判断TOKEN三十分钟内是否过期
     */
    public static boolean isTokenExpire(String authToken, String secretKey) {
        Claims claims = parserAuthenticateToken(authToken, secretKey);
        long remaining = claims.getExpiration().getTime() - System.currentTimeMillis();
        return remaining > 0 && remaining < REFRESH_THRESHOLD_MS;
    }

    /**
     * 解析TOKEN携带信息
     */
    public static TokenInfo parserTokenInfo(String token, String secretKey) {
        return parserTokenInfo(parserAuthenticateToken(token, secretKey));
    }

    /**
     * 从已完成签名校验的声明中读取登录用户信息。
     *
     * <p>过期令牌由 JJWT 抛出 {@link ExpiredJwtException} 时仍会携带已验签的
     * claims。过滤器仅用该方法取出用户编号以定位 refresh token，不能把未经
     * 验签的 JWT 负载作为身份依据。</p>
     */
    public static TokenInfo parserTokenInfo(Claims claims) {
        if (claims == null) {
            throw new LogicException(ErrorCodeConstants.NO_LOGIN);
        }
        Map<String, Object> userClaims = (Map<String, Object>) claims.get("user");
        if (userClaims == null) {
            throw new LogicException(ErrorCodeConstants.NO_LOGIN);
        }
        TokenInfo tokenInfo = new TokenInfo();
        tokenInfo.setUserId(Long.valueOf(userClaims.get("userId").toString()));
        tokenInfo.setUsername((String) userClaims.get("username"));
        return tokenInfo;
    }

    /**
     * 生成JWT TOKEN
     */
    public static String generatorJwtToken(TokenInfo tokenInfo, String secretKey, Integer expire) {
        Date expireTime = new Date(System.currentTimeMillis() + expire * HOUR_TO_MS);
        return Jwts.builder()
                .setHeaderParam("typ", "JWT")
                .setIssuedAt(new Date())
                .setExpiration(expireTime)
                .claim("user", tokenInfo)
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    /**
     * 获取token里的userId
     */
    public static Long getUserIdByToken(HttpServletRequest request, String tokenHeader, String tokenHead, String secretKey) {
        String authHeader = request.getHeader(tokenHeader);
        if(authHeader == null || !authHeader.startsWith(tokenHead)) {
            return null;
        }
        String authToken = getRealAuthorizationToken(authHeader, tokenHead);
        String userId = parserTokenInfo(authToken, secretKey).getUserId().toString();
        return Long.parseLong(userId);
    }
}
