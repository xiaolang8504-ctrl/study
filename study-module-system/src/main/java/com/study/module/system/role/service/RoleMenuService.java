package com.study.module.system.role.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.study.module.system.role.entity.RoleMenu;

import java.util.List;

/**
 * 角色菜单中间表 服务类
 */
public interface RoleMenuService extends IService<RoleMenu> {

    /**
     * 批量数据到角色菜单关联
     */
    boolean bathSaveRoleMenu(Integer roleId, String menuIds);

    /**
     * 删除角色关联的菜单关联数据
     */
    boolean deleteRoleMenuByRoleId(Integer id);

    /**
     * 删除菜单对应的全部角色关联。
     */
    boolean deleteRoleMenuByMenuId(Integer menuId);

    /**
     *  获取指定菜单ID对应的角色IDS
     */
    List<Integer> roleIdsByMenuId(List<Integer> menuIds);
}
