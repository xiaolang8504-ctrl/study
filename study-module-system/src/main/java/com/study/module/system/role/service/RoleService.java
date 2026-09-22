package com.study.module.system.role.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.study.module.system.role.entity.Role;
import com.study.api.dto.response.RoleData;

import java.util.List;

/**
 * 系统角色表 服务类
 */
public interface RoleService extends IService<Role> {
    /**
     * 检测角色ID是否存在
     */
    Role checkRoleById(Integer id);

    /**
     * 获取指定ID的角色
     */
    Role roleById(Integer id);

    /**
     * 检测指定角色名
     */
    boolean checkRoleName(String roleName);

    /**
     * 检测指定角色名和ID
     */
    boolean checkRoleNameById(Integer id, String roleName);

    /**
     * 判断当前账号是否包含管理员角色
     */
    boolean isAdminByUserId(Long userId);

    /**
     * 获取指定IDS的角色数组
     */
    List<RoleData> roleListByIds(List<Integer> roleIds);
}