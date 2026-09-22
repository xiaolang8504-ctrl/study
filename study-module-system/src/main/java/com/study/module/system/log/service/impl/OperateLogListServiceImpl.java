package com.study.module.system.log.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.module.system.log.convert.OperateLogConvert;
import com.study.module.system.log.dto.request.OperateLogPageListReq;
import com.study.module.system.log.dto.response.OperateLogListResp;
import com.study.module.system.log.entity.OperateLog;
import com.study.module.system.log.mapper.OperateLogMapper;
import com.study.module.system.log.service.OperateLogListService;
import com.study.common.core.domain.dto.PageResult;
import com.yunshang.budget.common.mybatis.utils.PageUtils;
import org.springframework.stereotype.Service;

/**
 * 操作日志列表服务
 */
@Service
public class OperateLogListServiceImpl extends ServiceImpl<OperateLogMapper, OperateLog> implements OperateLogListService {

    /**
     * 操作日志分页列表
     */
    @Override
    public PageResult<OperateLogListResp> operateLogPageList(OperateLogPageListReq request) {
        LambdaQueryWrapper<OperateLog> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(ObjectUtil.isNotEmpty(request.getOperateName()), OperateLog::getOperateName, request.getOperateName());
        queryWrapper.orderByDesc(OperateLog::getId);
        Page<OperateLog> page = new Page<>(request.getCurrent(), request.getPageSize());
        Page<OperateLog> operateLogPage = this.page(page, queryWrapper);
        return PageUtils.wrap(operateLogPage, OperateLogConvert.INSTANCE::toOperateLogListResp);
    }
}
