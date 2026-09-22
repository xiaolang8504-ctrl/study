package com.study.module.system.role.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.module.system.role.entity.RoleMenu;
import com.study.module.system.role.mapper.RoleMenuMapper;
import com.study.module.system.role.service.RoleMenuService;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 角色菜单中间表 服务实现类
 */
@SuppressWarnings({"Duplicates", "SpringJavaInjectionPointsAutowiringInspection"})
@Service
public class RoleMenuServiceImpl extends ServiceImpl<RoleMenuMapper, RoleMenu> implements RoleMenuService {

    /**
     * 批量数据到角色菜单关联
     */
    @Override
    public boolean bathSaveRoleMenu(Integer roleId, String menuIds) {
        List<RoleMenu> list = new ArrayList<>();
        String[] ids = menuIds.split(",");
        for (int i=0;i<ids.length;i++){
            RoleMenu roleMenu = new RoleMenu();
            roleMenu.setRoleId(roleId);
            roleMenu.setMenuId(Integer.valueOf(ids[i]));
            roleMenu.setCreateTime(LocalDateTime.now());
            list.add(roleMenu);
        }
        return this.saveBatch(list);
    }

    /**
     * 删除角色关联的菜单关联数据
     */
    @Override
    public boolean deleteRoleMenuByRoleId(Integer id) {
        return this.remove(new LambdaQueryWrapper<RoleMenu>().eq(RoleMenu::getRoleId, id));
    }

    /**
     * 删除菜单对应的全部角色关联。
     */
    @Override
    public boolean deleteRoleMenuByMenuId(Integer menuId) {
        return this.remove(new LambdaQueryWrapper<RoleMenu>().eq(RoleMenu::getMenuId, menuId));
    }

    /**
     *  获取指定菜单ID对应的角色IDS
     */
    @Override
    public List<Integer> roleIdsByMenuId(List<Integer> menuIds) {
        LambdaQueryWrapper<RoleMenu> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(RoleMenu::getMenuId,menuIds);
        return this.list(queryWrapper).stream().map(RoleMenu::getRoleId).collect(Collectors.toList());
    }
}
