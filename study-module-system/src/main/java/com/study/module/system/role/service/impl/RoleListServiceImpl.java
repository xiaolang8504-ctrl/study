package com.study.module.system.role.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.module.system.role.dto.request.RoleListReq;
import com.study.module.system.role.dto.response.RoleListResp;
import com.study.module.system.role.entity.Role;
import com.study.module.system.role.convert.RoleConvert;
import com.study.module.system.role.mapper.RoleMapper;
import com.study.module.system.role.service.RoleListService;
import org.springframework.stereotype.Service;
import java.util.*;

/**
 * 系统角色表 服务实现类
 */
@SuppressWarnings({"Duplicates", "SpringJavaInjectionPointsAutowiringInspection"})
@Service
public class RoleListServiceImpl extends ServiceImpl<RoleMapper, Role> implements RoleListService {

    /**
     * 角色列表带名称筛选
     */
    @Override
    public List<RoleListResp> roleList(RoleListReq request) {
        LambdaQueryWrapper<Role> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(Objects.nonNull(request.getRoleName()),Role::getRoleName,request.getRoleName())
                .eq(Objects.nonNull(request.getId()),Role::getId,request.getId())
                .orderByAsc(Role::getId);
        return RoleConvert.INSTANCE.toRoleList(this.list(queryWrapper));
    }

    /**
     * 角色IDS对应的角色列表
     */
    @Override
    public List<Role> roleListById(String ids) {
        List<String> idList = Arrays.asList(ids.split(","));
        LambdaQueryWrapper<Role> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Role::getId,idList);
        return this.list(queryWrapper);
    }
}