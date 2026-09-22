package com.study.common.core.exception;

import com.study.common.core.domain.Result;
import com.study.common.core.enums.SysErrorCodeConstants;
import com.study.common.core.utils.ResultUtils;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;

/**
 * 接口错误统一返回
 */
@RestController
public class ErrorHandler implements ErrorController {

    private static final String ERROR_PATH = "/error";

    @RequestMapping(value = ERROR_PATH)
    public Result handle(HttpServletResponse response) {
        if (response.getStatus() == HttpStatus.NOT_FOUND.value()) {
            return ResultUtils.fail(SysErrorCodeConstants.NOT_FOUND);
        } else {
            return ResultUtils.fail(SysErrorCodeConstants.SYSTEM_ERROR);
        }
    }

    /**
     * 获取异常。
     */
    @Override
    public String getErrorPath() {
        return null;
    }
}
