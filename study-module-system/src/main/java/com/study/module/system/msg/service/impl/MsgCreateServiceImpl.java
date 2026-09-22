package com.study.module.system.msg.service.impl;

import cn.hutool.core.text.StrFormatter;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.module.system.msg.config.MessageConfig;
import com.study.module.system.msg.config.MsgConfig;
import com.study.module.system.msg.entity.Msg;
import com.study.module.system.msg.mapper.MsgMapper;
import com.study.module.system.msg.service.MsgCreateService;
import com.study.module.system.msg.service.MsgService;
import com.study.api.dto.request.MsgParam;
import com.study.common.core.enums.ErrorCodeConstants;
import com.study.common.core.exception.LogicException;
import com.study.module.system.msg.constants.Read;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 消息创建服务
 */
@Service
public class MsgCreateServiceImpl extends ServiceImpl<MsgMapper, Msg> implements MsgCreateService {

    @Autowired
    MsgService msgService;

    @Autowired
    MsgConfig msgConfig;

    /**
     * 发送消息
     */
    @Override
    public void sendMsg(String msgType, Long receiverId, Object... params) {
        MessageConfig messageConfig = msgConfig.getMsgType().get(msgType);
        if (messageConfig == null) {
            throw new LogicException(ErrorCodeConstants.MSG_TYPE_NOT_EXIST);
        }
        Msg msg = msgService.buildMsg(msgType, messageConfig, receiverId, params);
        if (!this.save(msg)) {
            throw new LogicException(ErrorCodeConstants.CREATE_MSG_FAIL);
        }
    }

    /**
     * 批量多人发送同一条消息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createMsgList(String msgType, List<Long> receiverIdList, Object... params) {
        MessageConfig messageConfig = msgConfig.getMsgType().get(msgType);
        if (messageConfig == null) {
            throw new LogicException(ErrorCodeConstants.MSG_TYPE_NOT_EXIST);
        }
        List<Msg> batchMsgList = receiverIdList.stream().map(receiverId ->
                msgService.buildMsg(msgType, messageConfig, receiverId, params)).collect(Collectors.toList());
        if (!this.saveBatch(batchMsgList)) {
            throw new LogicException(ErrorCodeConstants.BATCH_CREATE_MSG_FAIL);
        }
    }

    /**
     * 发送多条消息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createMsgParamList(String msgType, List<MsgParam> msgParamList) {
        MessageConfig messageConfig = msgConfig.getMsgType().get(msgType);
        List<Msg> msgList = msgParamList.stream().map(msgParam -> {
            Msg msg = new Msg();
            msg.setMsgType(msgType);
            msg.setMsgTypeText(messageConfig.getText());
            msg.setMsgTitle(messageConfig.getTitle());
            msg.setMsgContent(StrFormatter.format(messageConfig.getContent(), msgParam.getParams()));
            msg.setIsRead(Read.NO);
            msg.setReceiveId(msgParam.getReceiverId());
            msg.setCreateTime(LocalDateTime.now());
            msg.setUpdateTime(LocalDateTime.now());
            return msg;
        }).collect(Collectors.toList());
        if (!this.saveBatch(msgList)) {
            throw new LogicException(ErrorCodeConstants.BATCH_CREATE_MSG_FAIL);
        }
    }
}
