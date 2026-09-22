package com.study.api.provider;

import com.study.common.core.exception.LogicException;

/**
 * 操作日志服务
 */
public interface OperateLogProvider {

    /**
     * 创建操作日志
     */
    void createOperateLog(String operateType, Long operateId, String ip, Object... params) throws LogicException;
}
