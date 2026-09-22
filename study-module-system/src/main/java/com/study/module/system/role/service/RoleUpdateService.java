package com.study.module.system.role.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.study.module.system.role.dto.request.UpdateRoleReq;
import com.study.module.system.role.entity.Role;
import org.springframework.transaction.annotation.Transactional;

/**
 * 系统角色表 服务类
 */
public interface RoleUpdateService extends IService<Role> {

    /**
     * 修改角色
     */
    @Transactional(rollbackFor = Exception.class)
    void updateRole(UpdateRoleReq request);
}