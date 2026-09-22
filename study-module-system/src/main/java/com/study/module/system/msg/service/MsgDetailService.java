package com.study.module.system.msg.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.study.module.system.msg.dto.response.MsgDetailResp;
import com.study.module.system.msg.entity.Msg;

/**
 * 消息详情服务
 */
public interface MsgDetailService extends IService<Msg> {

    /**
     * 消息详情
     */
    MsgDetailResp msgDetail(Long id);
}
