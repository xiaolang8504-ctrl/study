package com.study.api.provider;

import com.study.api.dto.request.MsgParam;
import com.study.common.core.exception.LogicException;

import java.util.List;

/**
 * 消息服务
 */
public interface MsgProvider {

    /**
     * 发送消息
     */
    void sendMsg(String msgType, Long receiverId, Object... params) throws LogicException;

    /**
     * 批量多人发送同一条消息
     */
    void batchSendMsg(String msgType, List<Long> receiverIdList, Object... params) throws LogicException;

    /**
     * 发送多条消息
     */
    void sendMsgParamList(String msgType, List<MsgParam> msgParam) throws LogicException;
}
