package com.study.module.system.log.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.study.module.system.log.dto.request.OperateLogPageListReq;
import com.study.module.system.log.dto.response.OperateLogListResp;
import com.study.module.system.log.entity.OperateLog;
import com.study.common.core.domain.dto.PageResult;

/**
 * 操作日志列表服务
 */
public interface OperateLogListService extends IService<OperateLog> {

    /**
     * 操作日志分页列表
     */
    PageResult<OperateLogListResp> operateLogPageList(OperateLogPageListReq request);
}
