package com.study.module.system.menu.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.module.system.menu.entity.Menu;
import com.study.module.system.menu.mapper.MenuMapper;
import com.study.module.system.menu.service.MenuDeleteService;
import com.study.module.system.menu.service.MenuService;
import com.study.common.core.enums.ErrorCodeConstants;
import com.study.common.core.exception.LogicException;
import com.study.module.system.menu.service.MenuResourceService;
import com.study.module.system.role.service.RoleMenuService;
import com.study.module.system.role.service.RoleService;
import com.study.module.system.role.entity.Role;
import com.study.module.system.role.entity.RoleMenu;
import com.study.module.system.user.service.UserAuthorityCacheService;
import com.study.module.system.user.service.UserRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.LinkedHashSet;
import java.util.stream.Collectors;

/**
 * 系统菜单表 服务实现类
 */
@SuppressWarnings({"Duplicates", "SpringJavaInjectionPointsAutowiringInspection"})
@Service
public class MenuDeleteServiceImpl extends ServiceImpl<MenuMapper, Menu> implements MenuDeleteService {

    @Autowired
    MenuResourceService menuResourceService;

    @Autowired
    MenuService menuService;

    @Autowired
    RoleMenuService roleMenuService;

    @Autowired
    RoleService roleService;

    @Autowired
    UserRoleService userRoleService;

    @Autowired
    UserAuthorityCacheService userAuthorityCacheService;

    /**
     * 删除菜单
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteMenu(Integer id) {
        // 参数逻辑校验
        deleteValidate(id);

        //数据入库
        Set<Integer> roleIds = new LinkedHashSet<>(roleMenuService.roleIdsByMenuId(Collections.singletonList(id)));
        List<Long> affectedUserIds = userRoleService.listUserIdsByRoleIds(new java.util.ArrayList<>(roleIds));
        deleteMenuData(id);
        roleMenuService.deleteRoleMenuByMenuId(id);
        refreshRoleMenuFields(roleIds, id);
        userAuthorityCacheService.refreshUserAuthorityCachesAfterCommit(affectedUserIds);
    }

    /**
     * 参数校验
     */
    private void deleteValidate(Integer id){
        menuService.checkMenuById(id);
        if (menuService.checkSonByPid(id)){
            throw new LogicException(ErrorCodeConstants.EXIST_SUB_MENU);
        }
    }

    /**
     * 组合数据
     */
    private void deleteMenuData(Integer id){
        //删除关联数据
        menuResourceService.deleteMenuResourceByMenuId(id);
        if (!this.removeById(id)){
            throw new LogicException(ErrorCodeConstants.DELETE_MENU_FAIL);
        }
    }

    /**
     * 同步角色表中冗余菜单编号与层级字段，防止已删除菜单在角色详情中残留。
     */
    private void refreshRoleMenuFields(Set<Integer> roleIds, Integer deletedMenuId) {
        String deletedMenuIdText = String.valueOf(deletedMenuId);
        for (Integer roleId : roleIds) {
            Role role = roleService.getById(roleId);
            if (role == null) {
                continue;
            }
            List<RoleMenu> remainingRelations = roleMenuService.list(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<RoleMenu>()
                            .eq(RoleMenu::getRoleId, roleId));
            role.setMenuIds(remainingRelations.stream().map(RoleMenu::getMenuId).sorted()
                    .map(String::valueOf).collect(Collectors.joining(",")));
            role.setMenuLevel(java.util.Arrays.stream(defaultText(role.getMenuLevel()).split(","))
                    .filter(item -> !item.isEmpty())
                    .filter(item -> java.util.Arrays.stream(item.split("-"))
                            .noneMatch(deletedMenuIdText::equals))
                    .collect(Collectors.joining(",")));
            if (!roleService.updateById(role)) {
                throw new LogicException(ErrorCodeConstants.DELETE_MENU_FAIL);
            }
        }
    }

    private String defaultText(String value) {
        return value == null ? "" : value;
    }
}
