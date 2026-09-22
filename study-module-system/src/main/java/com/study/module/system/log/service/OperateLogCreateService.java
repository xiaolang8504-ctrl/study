package com.study.module.system.log.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.study.module.system.log.entity.OperateLog;

/**
 * 操作日志创建服务
 */
public interface OperateLogCreateService extends IService<OperateLog> {

    /**
     * 创建操作日志
     */
    void createOperateLog(String operateType, Long operateId, String ip, Object... params);
}
