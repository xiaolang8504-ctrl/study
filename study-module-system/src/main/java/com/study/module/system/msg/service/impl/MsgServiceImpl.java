package com.study.module.system.msg.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.StrFormatter;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.module.system.msg.config.MessageConfig;
import com.study.module.system.msg.entity.Msg;
import com.study.module.system.msg.mapper.MsgMapper;
import com.study.module.system.msg.service.MsgService;
import com.study.common.core.enums.ErrorCodeConstants;
import com.study.common.core.exception.LogicException;
import com.study.module.system.msg.constants.Read;
import org.springframework.stereotype.Service;
import com.yunshang.budget.common.security.utils.AccountUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 消息服务
 */
@Service
public class MsgServiceImpl extends ServiceImpl<MsgMapper, Msg> implements MsgService {

    /**
     * 校验消息
     */
    @Override
    public Msg checkMsg(Long id) {
        Msg msg = this.lambdaQuery()
                .eq(Msg::getId, id)
                .eq(Msg::getReceiveId, AccountUtils.getUserId())
                .one();
        if (null == msg) {
            throw new LogicException(ErrorCodeConstants.MSG_NOT_EXIST);
        }
        return msg;
    }

    /**
     * 校验消息
     */
    @Override
    public void checkMsg(List<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            throw new LogicException(ErrorCodeConstants.MSG_NOT_EXIST);
        }
        Set<Long> distinctIds = ids.stream().collect(Collectors.toSet());
        List<Msg> msgList = this.lambdaQuery()
                .in(Msg::getId, distinctIds)
                .eq(Msg::getReceiveId, AccountUtils.getUserId())
                .list();
        if (msgList.size() != distinctIds.size()) {
            throw new LogicException(ErrorCodeConstants.MSG_NOT_EXIST);
        }
    }

    /**
     * 构建消息入库信息
     */
    @Override
    public Msg buildMsg(String msgType, MessageConfig messageConfig, Long receiverId, Object... params) {
        Msg msg = new Msg();
        msg.setMsgType(msgType);
        msg.setMsgTypeText(messageConfig.getText());
        msg.setMsgTitle(messageConfig.getTitle());
        msg.setMsgContent(StrFormatter.format(messageConfig.getContent(), params));
        msg.setIsRead(Read.NO);
        msg.setReceiveId(receiverId);
        msg.setCreateTime(LocalDateTime.now());
        msg.setUpdateTime(LocalDateTime.now());
        return msg;
    }
}
