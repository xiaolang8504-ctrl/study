package com.study.module.system.resource.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.study.module.system.resource.entity.Resource;
import com.study.module.system.resource.mapper.ResourceMapper;
import com.study.module.system.resource.service.ResourceDeleteService;
import com.study.common.core.enums.ErrorCodeConstants;
import com.study.common.core.exception.LogicException;
import com.study.module.system.resource.service.ResourceService;
import com.study.module.system.menu.service.MenuResourceService;
import com.study.module.system.menu.service.MenuService;
import com.study.module.system.menu.entity.Menu;
import com.study.module.system.menu.entity.MenuResource;
import com.study.module.system.role.service.RoleMenuService;
import com.study.module.system.role.service.RoleService;
import com.study.module.system.role.entity.Role;
import com.study.module.system.user.service.UserAuthorityCacheService;
import com.study.module.system.user.service.UserRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 系统资源表 服务实现类
 */
@SuppressWarnings({"Duplicates", "SpringJavaInjectionPointsAutowiringInspection"})
@Service
public class ResourceDeleteServiceImpl extends ServiceImpl<ResourceMapper, Resource> implements ResourceDeleteService {

    @Autowired
    ResourceService resourceService;

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
     * 删除资源
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteResource(Integer id) {
        // 参数逻辑校验
        deleteValidate(id);

        //数据入库
        List<Integer> menuIds = menuResourceService.getMenuResourceByResourceId(id);
        // 先清理关联并同步菜单中的冗余资源字段，避免已删除资源仍在菜单详情中出现。
        menuResourceService.deleteMenuResourceByResourceId(id);
        refreshMenuResourceFields(menuIds);
        if (!this.removeById(id)){
            throw new LogicException(ErrorCodeConstants.DELETE_RES_FAIL);
        }
        Set<Integer> roleIds = menuIds.isEmpty() ? new HashSet<>()
                : new HashSet<>(roleMenuService.roleIdsByMenuId(menuIds));
        roleService.list(new LambdaQueryWrapper<Role>().eq(Role::getIsSystem, 1))
                .forEach(role -> roleIds.add(role.getId()));
        userAuthorityCacheService.refreshUserAuthorityCachesAfterCommit(
                userRoleService.listUserIdsByRoleIds(new ArrayList<>(roleIds)));
    }

    /**
     * 参数逻辑校验
     */
    private void deleteValidate(Integer id){
        resourceService.checkResourceById(id);
        if (resourceService.checkSonByPid(id)){
            throw new LogicException(ErrorCodeConstants.EXIST_SUB_RES);
        }
    }

    /**
     * 同步被影响菜单的冗余资源信息。
     */
    private void refreshMenuResourceFields(List<Integer> menuIds) {
        for (Integer menuId : menuIds) {
            Menu menu = menuService.getById(menuId);
            if (menu == null) {
                continue;
            }
            List<MenuResource> relations = menuResourceService.list(new LambdaQueryWrapper<MenuResource>()
                    .eq(MenuResource::getMenuId, menuId));
            menu.setResourceIds(relations.stream().map(MenuResource::getResourceId).sorted()
                    .map(String::valueOf).collect(Collectors.joining(",")));
            menu.setResourceLevel(relations.stream().sorted(java.util.Comparator.comparing(MenuResource::getResourceId))
                    .map(MenuResource::getResourceLevel).collect(Collectors.joining(",")));
            if (!menuService.updateById(menu)) {
                throw new LogicException(ErrorCodeConstants.DELETE_RES_FAIL);
            }
        }
    }
}
