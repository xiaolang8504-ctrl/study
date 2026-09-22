package com.study.module.system.role.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.study.module.system.role.entity.Role;
import org.springframework.transaction.annotation.Transactional;

/**
 * 系统角色表 服务类
 */
public interface RoleDeleteService extends IService<Role> {

    /**
     * 删除角色
     */
    @Transactional(rollbackFor = Exception.class)
    void deleteRole(Integer id);
}