package com.study.module.system.msg.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.module.system.msg.entity.Msg;
import com.study.module.system.msg.mapper.MsgMapper;
import com.study.module.system.msg.service.MsgDeleteService;
import com.study.module.system.msg.service.MsgService;
import com.study.common.core.enums.ErrorCodeConstants;
import com.study.common.core.exception.LogicException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 消息删除服务
 */
@Service
public class MsgDeleteServiceImpl extends ServiceImpl<MsgMapper, Msg> implements MsgDeleteService {

    @Autowired
    MsgService msgService;

    /**
     * 批量删除消息
     */
    @Override
    public void batchDeleteMsg(List<Long> ids) {
        // 校验消息
        msgService.checkMsg(ids);
        // 批量删除消息
        if (!this.removeByIds(ids)) {
            throw new LogicException(ErrorCodeConstants.BATCH_DELETE_MSG_FAIL);
        }
    }
}
