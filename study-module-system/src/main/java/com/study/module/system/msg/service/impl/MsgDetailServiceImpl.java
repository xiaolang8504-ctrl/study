package com.study.module.system.msg.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.module.system.msg.convert.MsgConvert;
import com.study.module.system.msg.dto.response.MsgDetailResp;
import com.study.module.system.msg.entity.Msg;
import com.study.module.system.msg.mapper.MsgMapper;
import com.study.module.system.msg.service.MsgDetailService;
import com.study.module.system.msg.service.MsgService;
import com.study.common.core.enums.ErrorCodeConstants;
import com.study.common.core.exception.LogicException;
import com.study.module.system.msg.constants.Read;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 消息详情服务
 */
@Service
public class MsgDetailServiceImpl extends ServiceImpl<MsgMapper, Msg> implements MsgDetailService {

    @Autowired
    MsgService msgService;

    /**
     * 消息详情
     */
    @Override
    public MsgDetailResp msgDetail(Long id) {
        // 校验消息
        Msg msg = msgService.checkMsg(id);
        MsgDetailResp resp = MsgConvert.INSTANCE.toMsgDetailResp(msg);
        // 消息修改已读
        if (msg.getIsRead() == Read.NO) {
            Msg updateMsg = new Msg();
            updateMsg.setIsRead(Read.YES);
            updateMsg.setId(id);
            updateMsg.setUpdateTime(LocalDateTime.now());
            if (!this.updateById(updateMsg)) {
                throw new LogicException(ErrorCodeConstants.SET_HAS_READ_FAIL);
            }
        }
        return resp;
    }
}
