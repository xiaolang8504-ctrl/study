package com.study.module.system.log.controller;

import com.study.module.system.log.dto.request.OperateLogPageListReq;
import com.study.module.system.log.dto.response.OperateLogListResp;
import com.study.module.system.log.service.OperateLogListService;
import com.study.common.core.domain.Result;
import com.study.common.core.domain.dto.PageResult;
import com.study.common.core.utils.ResultUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 操作日志控制层
 */
@Api(tags = "操作日志")
@RestController
@RequestMapping("/api/operateLog")
public class OperateLogController {

    @Autowired
    OperateLogListService operateLogListService;

    /**
     * 分页查询操作日志。
     */
    @ApiOperation("操作日志分页列表")
    @GetMapping(value = "/operateLogPageList")
    public Result<PageResult<OperateLogListResp>> operateLogPageList(@Validated OperateLogPageListReq request) {
        return ResultUtils.success(operateLogListService.operateLogPageList(request));
    }
}
