package com.study.module.system.msg.controller;

import com.study.module.system.msg.dto.request.MsgIdsRequest;
import com.study.module.system.msg.dto.response.MsgDetailResp;
import com.study.module.system.msg.dto.response.MsgPageListResp;
import com.study.module.system.msg.service.MsgDeleteService;
import com.study.module.system.msg.service.MsgDetailService;
import com.study.module.system.msg.service.MsgListService;
import com.study.module.system.msg.service.MsgUpdateService;
import com.study.common.core.domain.Result;
import com.study.common.core.domain.dto.PageResult;
import com.study.common.core.utils.ResultUtils;
import com.study.module.system.msg.dto.request.MsgIdReq;
import com.study.module.system.msg.dto.request.MsgPageListReq;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Api(tags = "消息")
@RestController
@RequestMapping("/api/msg")
public class MsgController {

    @Autowired
    MsgListService msgListService;

    @Autowired
    MsgUpdateService msgUpdateService;

    @Autowired
    MsgDetailService msgDetailService;

    @Autowired
    MsgDeleteService msgDeleteService;

    /**
     * 分页查询分页数据。
     */
    @ApiOperation("消息分页列表")
    @GetMapping(value = "/msgPageList")
    public Result<PageResult<MsgPageListResp>> msgPageList(@Validated MsgPageListReq request) {
        return ResultUtils.success(msgListService.msgPageList(request));
    }

    /**
     * 消息详情(更改已读)
     */
    @ApiOperation("消息详情(更改已读)")
    @PostMapping(value = "/msgDetails")
    public Result<MsgDetailResp> msgDetails(@Validated @RequestBody MsgIdReq request) {
        return ResultUtils.success(msgDetailService.msgDetail(request.getId()));
    }

    /**
     * 批量设置消息已读
     */
    @ApiOperation("批量设置消息已读")
    @PostMapping(value = "/batchSetMsgRead")
    public Result<Void> batchSetMsgRead(@Validated @RequestBody MsgIdsRequest request) {
        msgUpdateService.batchSetMsgRead(request.getIds());
        return ResultUtils.success();
    }

    /**
     * 批量设置消息已读
     */
    @ApiOperation("批量删除消息")
    @PostMapping(value = "/batchDeleteMsg")
    public Result<Void> batchDeleteMsg(@Validated @RequestBody MsgIdsRequest request) {
        msgDeleteService.batchDeleteMsg(request.getIds());
        return ResultUtils.success();
    }

    /**
     * 未读消息统计
     */
    @ApiOperation("未读消息统计")
    @GetMapping(value = "/unReadMsgStatistics")
    public Result<Long> unReadMsgStatistics() {
        return ResultUtils.success(msgListService.unReadMsgStatistics());
    }
}
