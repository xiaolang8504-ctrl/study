package com.study.module.system.dict.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.module.system.dict.entity.Dict;
import com.study.module.system.dict.mapper.DictMapper;
import com.study.module.system.dict.service.DictOptionsService;
import com.study.common.core.domain.KeyValue;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 字典下拉选项服务
 */
@Service
public class DictOptionsServiceImpl extends ServiceImpl<DictMapper, Dict> implements DictOptionsService {

    /**
     * 字典下拉选项
     */
    @Override
    public List<KeyValue<Long, String>> dictOptions() {
        LambdaQueryWrapper<Dict> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(Dict::getId);
        return this.baseMapper.selectMaps(queryWrapper).stream().map(map -> {
            KeyValue<Long, String> response = new KeyValue<>();
            response.setKey((Long) map.get("id"));
            response.setValue(String.valueOf(map.get("dictType")));
            return response;
        }).collect(Collectors.toList());
    }
}
