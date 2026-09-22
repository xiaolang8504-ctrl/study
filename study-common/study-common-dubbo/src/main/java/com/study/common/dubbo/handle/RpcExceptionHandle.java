package com.study.common.dubbo.handle;

import com.study.common.core.domain.Result;
import com.study.common.core.enums.SysErrorCodeConstants;
import com.study.common.core.utils.ResultUtils;
import org.apache.dubbo.rpc.RpcException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * RPC 异常
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class RpcExceptionHandle {

    /**
     * RPC 异常
     */
    @ExceptionHandler(value = RpcException.class)
    public Result rpcDeniedException(RpcException e) {
        e.printStackTrace();
        return ResultUtils.fail(SysErrorCodeConstants.INVOKE_SERVICE_FAILED, e.getMessage());
    }
}
