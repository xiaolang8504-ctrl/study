package com.study.module.system.role.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.module.system.role.entity.Role;
import com.study.module.system.role.mapper.RoleMapper;
import com.study.module.system.role.service.RoleOptionsService;
import org.springframework.stereotype.Service;
import java.util.*;

/**
 * 系统角色表 服务实现类
 */
@SuppressWarnings({"Duplicates", "SpringJavaInjectionPointsAutowiringInspection"})
@Service
public class RoleOptionServiceImpl extends ServiceImpl<RoleMapper, Role> implements RoleOptionsService {

    /**
     * 角色下拉列表
     */
    @Override
    public List<Map<String, Object>> roleOptions() {
        LambdaQueryWrapper<Role> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(Role::getId,Role::getRoleName);
        return this.listMaps(queryWrapper);
    }
}