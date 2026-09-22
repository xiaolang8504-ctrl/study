package com.yunshang.budget.common.security.utils;

import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 账户工具类
 */
public class AccountUtils {

    /**
     * 获取当前登陆的用户标识
     */
    public static Long getUserId() {
        Object principal = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
        if (principal instanceof Long) {
            return (Long) principal;
        }
        if (principal instanceof String) {
            return Long.valueOf((String) principal);
        }
        return Long.valueOf(principal.toString());
    }
}
