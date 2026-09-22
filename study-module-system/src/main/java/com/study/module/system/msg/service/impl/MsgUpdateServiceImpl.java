package com.study.module.system.msg.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.module.system.msg.entity.Msg;
import com.study.module.system.msg.mapper.MsgMapper;
import com.study.module.system.msg.service.MsgService;
import com.study.module.system.msg.service.MsgUpdateService;
import com.study.common.core.enums.ErrorCodeConstants;
import com.study.common.core.exception.LogicException;
import com.study.module.system.msg.constants.Read;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 消息更新服务
 */
@Service
public class MsgUpdateServiceImpl extends ServiceImpl<MsgMapper, Msg> implements MsgUpdateService {

    @Autowired
    MsgService msgService;

    /**
     * 批量设置消息
     */
    @Override
    public void batchSetMsgRead(List<Long> ids) {
        // 校验消息
        msgService.checkMsg(ids);
        for (Long id : ids) {
            Msg updateMsg = new Msg();
            updateMsg.setIsRead(Read.YES);
            updateMsg.setId(id);
            updateMsg.setUpdateTime(LocalDateTime.now());
            if (!this.updateById(updateMsg)) {
                throw new LogicException(ErrorCodeConstants.SET_HAS_READ_FAIL);
            }
        }
    }
}
