package com.study.module.system.msg.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.study.module.system.msg.entity.Msg;

import java.util.List;

/**
 * 消息删除服务
 */
public interface MsgDeleteService extends IService<Msg> {

    /**
     * 批量删除消息
     */
    void batchDeleteMsg(List<Long> ids);
}
