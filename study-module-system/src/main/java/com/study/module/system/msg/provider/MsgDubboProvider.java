package com.study.module.system.msg.provider;

import com.study.module.system.msg.service.MsgCreateService;
import com.study.api.dto.request.MsgParam;
import com.study.api.provider.MsgProvider;
import com.study.common.core.exception.LogicException;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * 消息服务
 */
@Service
public class MsgDubboProvider implements MsgProvider {

    @Autowired
    MsgCreateService msgCreateService;

    /**
     * 发送消息
     */
    @Override
    public void sendMsg(String msgType, Long receiverId, Object... params) throws LogicException {
        msgCreateService.sendMsg(msgType, receiverId, params);
    }

    /**
     * 批量多人发送同一条消息
     */
    @Override
    public void batchSendMsg(String msgType, List<Long> receiverIdList, Object... params) throws LogicException {
        msgCreateService.createMsgList(msgType, receiverIdList, params);
    }

    /**
     * 发送多条消息
     */
    @Override
    public void sendMsgParamList(String msgType, List<MsgParam> msgParamList) throws LogicException {
        msgCreateService.createMsgParamList(msgType, msgParamList);
    }
}
