package com.yunshang.budget.common.security.domain;

import lombok.Data;

import java.io.Serializable;

/**
 * Token信息
 */
@Data
public class TokenInfo implements Serializable {

    private static final long serialVersionUID = -590592465504216984L;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;
}
