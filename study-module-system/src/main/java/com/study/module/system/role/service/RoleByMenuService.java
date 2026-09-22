package com.study.module.system.role.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.study.module.system.role.dto.request.UpdateRoleMenuReq;
import com.study.module.system.role.entity.Role;
import org.springframework.transaction.annotation.Transactional;

/**
 * 系统角色表 服务类
 */
public interface RoleByMenuService extends IService<Role> {

    /**
     * 角色分配菜单
     */
    @Transactional(rollbackFor = Exception.class)
    void roleByMenu(UpdateRoleMenuReq request);
}