package com.study.module.system.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.module.system.user.entity.User;
import com.study.module.system.user.mapper.UserMapper;
import com.study.module.system.user.service.UserOptionsService;
import com.study.common.core.domain.KeyValue;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 系统用户表 服务实现类
 */
@Service
public class UserOptionsServiceImpl extends ServiceImpl<UserMapper, User> implements UserOptionsService {

    /**
     * 账户下拉列表
     */
    @Override
    public List<KeyValue<String, String>> userOptions() {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();;
        return this.baseMapper.selectMaps(queryWrapper).stream().map(map -> {
            KeyValue<String, String> response = new KeyValue<>();
            response.setKey(map.get("id").toString());
            response.setValue(map.get("userName").toString());
            return response;
        }).collect(Collectors.toList());
    }
}