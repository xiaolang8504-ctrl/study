package com.yunshang.budget.common.security.base;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;

/**
 * 安全用户模型
 */
public class JwtUser implements UserDetails {

    private String username;

    private String password;

    private Collection<? extends GrantedAuthority> authorities;

    private String state;

    /**
     * 用户信息
     */
    public JwtUser(String username, String password, Collection<? extends GrantedAuthority> authorities, String state) {
        this.username = username;
        this.password = password;
        this.authorities = authorities;
        this.state = state;
    }

    /**
     * 获取用户。
     */
    @Override
    public String getUsername() {
        return username;
    }

    /**
     * 获取相关业务数据。
     */
    @Override
    public String getPassword() {
        return password;
    }

    /**
     * 返回用户权限集合
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    /**
     * 账号是否没有过期
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * 账号是否没有锁定
     */
    @Override
    public boolean isAccountNonLocked() {
        return "0".equals(state);
    }

    /**
     * 账号密码是否没有过期
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * 账号是否启用
     */
    @JsonIgnore
    @Override
    public boolean isEnabled() {
        return true;
    }

}
