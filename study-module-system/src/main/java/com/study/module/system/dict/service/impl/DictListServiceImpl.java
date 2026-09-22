package com.study.module.system.dict.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.module.system.dict.convert.DictConvert;
import com.study.module.system.dict.dto.request.DictPageListReq;
import com.study.module.system.dict.dto.response.DictListResp;
import com.study.module.system.dict.entity.Dict;
import com.study.module.system.dict.mapper.DictMapper;
import com.study.module.system.dict.service.DictListService;
import com.study.common.core.domain.dto.PageResult;
import com.yunshang.budget.common.mybatis.utils.PageUtils;
import org.springframework.stereotype.Service;

/**
 * 字典列表服务
 */
@Service
public class DictListServiceImpl extends ServiceImpl<DictMapper, Dict> implements DictListService {

    /**
     * 字典分页列表
     */
    @Override
    public PageResult<DictListResp> dictList(DictPageListReq request) {
        LambdaQueryWrapper<Dict> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(ObjectUtil.isNotEmpty(request.getDictType()), Dict::getDictType, request.getDictType());
        queryWrapper.like(ObjectUtil.isNotEmpty(request.getDictName()), Dict::getDictName, request.getDictName());
        queryWrapper.orderByDesc(Dict::getId);
        Page<Dict> page = new Page<>(request.getCurrent(), request.getPageSize());
        this.page(page, queryWrapper);
        return PageUtils.wrap(page, DictConvert.INSTANCE::toDictListResp);
    }
}
