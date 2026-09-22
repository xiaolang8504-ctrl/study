package com.study.module.system.msg.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.study.module.system.msg.entity.Msg;
import com.study.api.dto.request.MsgParam;

import java.util.List;

/**
 * 消息创建服务
 */
public interface MsgCreateService extends IService<Msg> {

    /**
     * 发送消息
     */
    void sendMsg(String msgType, Long receiverId, Object... params);

    /**
     * 批量多人发送同一条消息
     */
    void createMsgList(String msgType, List<Long> receiverIdList, Object... params);

    /**
     * 多人发送多条消息
     */
    void createMsgParamList(String msgType, List<MsgParam> msgParamList);
}
