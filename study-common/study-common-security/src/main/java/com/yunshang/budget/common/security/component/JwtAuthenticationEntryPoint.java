package com.yunshang.budget.common.security.component;

import com.alibaba.fastjson.JSON;
import com.study.common.core.enums.ErrorCodeConstants;
import com.study.common.core.utils.ResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * 用来解决匿名用户访问的异常
 */
@Component
@Slf4j
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException e) {
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-Type", "application/json");
        response.setHeader("Access-Control-Max-Age", "3600");
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter writer = null;
        try {
            writer = response.getWriter();
            writer.write(JSON.toJSONString(ResultUtils.fail(ErrorCodeConstants.NO_LOGIN)));
            writer.flush();
        } catch (IOException ex) {
            log.error(ex.getMessage());
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }
}