package com.study.module.system.dict.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.module.system.dict.convert.DictConvert;
import com.study.module.system.dict.dto.request.DictDataPageListReq;
import com.study.module.system.dict.dto.response.DictDataListResp;
import com.study.module.system.dict.entity.DictData;
import com.study.module.system.dict.mapper.DictDataMapper;
import com.study.module.system.dict.service.DictDataListService;
import com.study.common.core.domain.dto.PageResult;
import com.yunshang.budget.common.mybatis.utils.PageUtils;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * 字典数据服务
 */
@Service
public class DictDataListServiceImpl extends ServiceImpl<DictDataMapper, DictData> implements DictDataListService {

    /**
     * 字典数据分页列表
     */
    @Override
    public PageResult<DictDataListResp> dictDataPageList(DictDataPageListReq request) {
        LambdaQueryWrapper<DictData> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(ObjectUtil.isNotEmpty(request.getDictType()), DictData::getDictType, request.getDictType());
        queryWrapper.like(ObjectUtil.isNotEmpty(request.getDictLabel()), DictData::getDictLabel, request.getDictLabel());
        queryWrapper.eq(Objects.nonNull(request.getIsEnable()), DictData::getIsEnable, request.getIsEnable());
        queryWrapper.orderByDesc(DictData::getDictDataSort);
        Page<DictData> page = new Page<>(request.getCurrent(), request.getPageSize());
        this.page(page, queryWrapper);
        return PageUtils.wrap(page, DictConvert.INSTANCE::toDictDataListResp);
    }
}
