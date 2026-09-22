package com.study.module.system.log.provider;

import com.study.module.system.log.service.OperateLogCreateService;
import com.study.api.provider.OperateLogProvider;
import com.study.common.core.exception.LogicException;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 操作日志 前端控制器(对外)
 */
@DubboService
public class OperateDubboLogProvider implements OperateLogProvider {

    @Autowired
    OperateLogCreateService operateLogCreateService;

    /**
     * 创建操作日志
     */
    @Override
    public void createOperateLog(String operateType, Long operateId, String ip, Object... params) throws LogicException {
        operateLogCreateService.createOperateLog(operateType, operateId, ip, params);
    }
}
