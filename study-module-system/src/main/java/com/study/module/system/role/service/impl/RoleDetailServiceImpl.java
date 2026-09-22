package com.study.module.system.role.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.module.system.role.dto.request.RoleDetailReq;
import com.study.module.system.role.dto.response.RoleDetailResp;
import com.study.module.system.role.entity.Role;
import com.study.module.system.role.convert.RoleConvert;
import com.study.module.system.role.mapper.RoleMapper;
import com.study.module.system.role.service.RoleDetailService;
import com.study.module.system.role.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 系统角色表 服务实现类
 */
@SuppressWarnings({"Duplicates", "SpringJavaInjectionPointsAutowiringInspection"})
@Service
public class RoleDetailServiceImpl extends ServiceImpl<RoleMapper, Role> implements RoleDetailService {

    @Autowired
    RoleService roleService;

    /**
     * 角色详情
     */
    @Override
    public RoleDetailResp roleDetail(RoleDetailReq request) {
        return RoleConvert.INSTANCE.toRoleDetail(roleService.checkRoleById(request.getId()));
    }
}
