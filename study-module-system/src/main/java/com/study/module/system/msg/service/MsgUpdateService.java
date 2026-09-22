package com.study.module.system.msg.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.study.module.system.msg.entity.Msg;

import java.util.List;

/**
 * 消息更新服务
 */
public interface MsgUpdateService extends IService<Msg> {

    /**
     * 批量设置消息
     */
    void batchSetMsgRead(List<Long> ids);
}
