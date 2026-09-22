package com.study.module.system.menu.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.module.system.menu.dto.request.UpdateMenuResourceReq;
import com.study.module.system.menu.entity.Menu;
import com.study.module.system.menu.mapper.MenuMapper;
import com.study.module.system.menu.service.MenuResourceService;
import com.study.module.system.menu.service.MenuResourceUpdateService;
import com.study.module.system.menu.service.MenuService;
import com.study.module.system.resource.service.ResourceService;
import com.study.module.system.role.service.RoleMenuService;
import com.study.module.system.user.service.UserAuthorityCacheService;
import com.study.module.system.user.service.UserRoleService;
import com.study.common.core.enums.ErrorCodeConstants;
import com.study.common.core.exception.LogicException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Stream;
import java.util.stream.Collectors;

/**
 * 菜单关联API更新服务实现
 */
@Service
public class MenuResourceUpdateServiceImpl extends ServiceImpl<MenuMapper, Menu>
        implements MenuResourceUpdateService {

    @Autowired
    MenuService menuService;

    @Autowired
    MenuResourceService menuResourceService;

    @Autowired
    ResourceService resourceService;

    @Autowired
    RoleMenuService roleMenuService;

    @Autowired
    UserRoleService userRoleService;

    @Autowired
    UserAuthorityCacheService userAuthorityCacheService;

    /**
     * 更新菜单资源关联
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateMenuResource(UpdateMenuResourceReq request) {
        menuService.checkMenuById(request.getId());
        List<Integer> resourceIds = request.getResourceIds().stream().distinct().collect(Collectors.toList());
        List<Integer> resourceLevelIds = request.getResourceLevelIds().stream().distinct().collect(Collectors.toList());
        Stream.concat(resourceIds.stream(), resourceLevelIds.stream())
                .distinct()
                .forEach(resourceService::checkResourceById);

        String resourceIdText = joinIds(resourceIds);
        String resourceLevelText = joinIds(resourceLevelIds);
        menuResourceService.deleteMenuResourceByMenuId(request.getId());
        if (!resourceIds.isEmpty()
                && !menuResourceService.batchSaveMenuResource(request.getId(), resourceIds, resourceLevelText)) {
            throw new LogicException(ErrorCodeConstants.UPDATE_MENU_FAIL);
        }

        Menu menu = new Menu();
        menu.setId(request.getId());
        menu.setResourceIds(resourceIdText);
        menu.setResourceLevel(resourceLevelText);
        if (!this.updateById(menu)) {
            throw new LogicException(ErrorCodeConstants.UPDATE_MENU_FAIL);
        }
        userAuthorityCacheService.refreshUserAuthorityCachesAfterCommit(userRoleService.listUserIdsByRoleIds(
                roleMenuService.roleIdsByMenuId(java.util.Collections.singletonList(request.getId()))));
    }

    /**
     * 拼接编号列表
     */
    private String joinIds(List<Integer> ids) {
        return ids.stream().map(String::valueOf).collect(Collectors.joining(","));
    }
}
