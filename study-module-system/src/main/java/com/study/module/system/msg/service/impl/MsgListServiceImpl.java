package com.study.module.system.msg.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.module.system.msg.convert.MsgConvert;
import com.study.module.system.msg.dto.response.MsgPageListResp;
import com.study.module.system.msg.entity.Msg;
import com.study.module.system.msg.mapper.MsgMapper;
import com.study.module.system.msg.service.MsgListService;
import com.study.common.core.domain.dto.PageResult;
import com.yunshang.budget.common.mybatis.utils.PageUtils;
import com.yunshang.budget.common.security.utils.AccountUtils;
import com.study.module.system.msg.constants.Read;
import com.study.module.system.msg.dto.request.MsgPageListReq;
import org.springframework.stereotype.Service;

/**
 * 消息列表服务
 */
@Service
public class MsgListServiceImpl extends ServiceImpl<MsgMapper, Msg> implements MsgListService {

    /**
     * 消息分页列表
     */
    @Override
    public PageResult<MsgPageListResp> msgPageList(MsgPageListReq request) {
        LambdaQueryWrapper<Msg> queryWrapper = getMsgListQueryWrapper(request);
        Page<Msg> page = new Page<>(request.getCurrent(), request.getPageSize());
        this.page(page, queryWrapper);
        return PageUtils.wrap(page, MsgConvert.INSTANCE::toMsgPageListResp);
    }

    /**
     * 未读消息统计
     */
    @Override
    public Long unReadMsgStatistics() {
        LambdaQueryWrapper<Msg> queryWrapper = new LambdaQueryWrapper<>();
        Long userId = AccountUtils.getUserId();
        queryWrapper.eq(Msg::getReceiveId, userId);
        queryWrapper.eq(Msg::getIsRead, Read.NO);
        return this.count(queryWrapper);
    }

    /**
     * 获取列表查询
     */
    private LambdaQueryWrapper<Msg> getMsgListQueryWrapper(MsgPageListReq request) {
        LambdaQueryWrapper<Msg> queryWrapper = new LambdaQueryWrapper<>();
        Long userId = AccountUtils.getUserId();
        queryWrapper.eq(Msg::getReceiveId, userId);
        if (request.getIsRead() != null) {
            queryWrapper.eq(Msg::getIsRead, request.getIsRead());
        }
        // 查询条件
        queryWrapper.orderByDesc(Msg::getId);
        return queryWrapper;
    }
}
