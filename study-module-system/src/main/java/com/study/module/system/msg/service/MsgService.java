package com.study.module.system.msg.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.study.module.system.msg.config.MessageConfig;
import com.study.module.system.msg.entity.Msg;

import java.util.List;

/**
 * 消息服务
 */
public interface MsgService extends IService<Msg> {

    /**
     * 校验消息
     */
    Msg checkMsg(Long id);

    /**
     * 校验消息
     */
    void checkMsg(List<Long> ids);

    /**
     * 构建消息入库信息
     */
    Msg buildMsg(String msgType, MessageConfig messageConfig, Long receiverId, Object... params);
}
