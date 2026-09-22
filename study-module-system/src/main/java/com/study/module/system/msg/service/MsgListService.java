package com.study.module.system.msg.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.study.module.system.msg.dto.response.MsgPageListResp;
import com.study.module.system.msg.entity.Msg;
import com.study.common.core.domain.dto.PageResult;
import com.study.module.system.msg.dto.request.MsgPageListReq;

/**
 * 消息列表服务
 */
public interface MsgListService extends IService<Msg> {

    /**
     * 消息分页列表
     */
    PageResult<MsgPageListResp> msgPageList(MsgPageListReq request);

    /**
     * 未读消息统计
     */
    Long unReadMsgStatistics();
}
