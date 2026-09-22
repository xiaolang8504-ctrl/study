package com.study.module.system.user.service;

import com.study.module.system.user.dto.request.LoginForm;
import com.study.module.system.user.dto.response.LoginResp;

import javax.servlet.http.HttpServletRequest;

public interface LoginService {

    /**
     * 用户登录
     */
    LoginResp login(LoginForm request) ;

    /**
     * 退出登录
     */
    void outLogin(HttpServletRequest request) ;

}
