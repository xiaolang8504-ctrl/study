package com.study.module.system.resource.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.study.common.core.enums.ErrorCodeConstants;
import com.study.common.core.exception.LogicException;
import com.study.module.system.resource.dto.request.UpdateResourceReq;
import com.study.module.system.resource.entity.Resource;
import com.study.module.system.resource.convert.ResourceConvert;
import com.study.module.system.resource.mapper.ResourceMapper;
import com.study.module.system.resource.service.ResourceService;
import com.study.module.system.resource.service.ResourceUpdateService;
import com.study.module.system.menu.service.MenuResourceService;
import com.study.module.system.role.service.RoleMenuService;
import com.study.module.system.role.service.RoleService;
import com.study.module.system.role.entity.Role;
import com.study.module.system.user.service.UserAuthorityCacheService;
import com.study.module.system.user.service.UserRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 系统资源表 服务实现类
 */
@SuppressWarnings({"Duplicates", "SpringJavaInjectionPointsAutowiringInspection"})
@Service
public class ResourceUpdateServiceImpl extends ServiceImpl<ResourceMapper, Resource> implements ResourceUpdateService {

    @Autowired
    ResourceService resourceService;

    @Autowired
    MenuResourceService menuResourceService;

    @Autowired
    RoleMenuService roleMenuService;

    @Autowired
    RoleService roleService;

    @Autowired
    UserRoleService userRoleService;

    @Autowired
    UserAuthorityCacheService userAuthorityCacheService;

    /**
     * 修改资源
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateResource(UpdateResourceReq request) {
        // 参数逻辑校验
        updateValidate(request);

        //数据入库
        updateResourceData(request);
        refreshUserAuthoritiesByResourceId(request.getId());
    }

    /**
     * 更新排序字段值
     */
    @Override
    public void updateResourceSort(Integer id) {
        Resource resources = new Resource();
        resources.setSort(id);
        resources.setId(id);
        this.updateById(resources);
    }

    /**
     * 参数逻辑校验
     */
    private void updateValidate(UpdateResourceReq request){
        resourceService.checkResourceById(request.getId());
        if (resourceService.checkResourceCodeById(request.getId(), request.getCode())){
            throw new LogicException(ErrorCodeConstants.RES_CODE_EXIST);
        }
    }

    /**
     * 组合数据
     */
    private void updateResourceData(UpdateResourceReq request){
        Resource resource = ResourceConvert.INSTANCE.toResource(request);
        if (!this.updateById(resource)){
            throw new LogicException(ErrorCodeConstants.UPDATE_RES_FAIL);
        }
    }

    /**
     * 刷新已关联该资源的菜单所覆盖用户的权限缓存。
     */
    private void refreshUserAuthoritiesByResourceId(Integer resourceId) {
        List<Integer> menuIds = menuResourceService.getMenuResourceByResourceId(resourceId);
        Set<Integer> roleIds = menuIds.isEmpty() ? new HashSet<>()
                : new HashSet<>(roleMenuService.roleIdsByMenuId(menuIds));
        roleService.list(new LambdaQueryWrapper<Role>().eq(Role::getIsSystem, 1))
                .forEach(role -> roleIds.add(role.getId()));
        userAuthorityCacheService.refreshUserAuthorityCachesAfterCommit(
                userRoleService.listUserIdsByRoleIds(new ArrayList<>(roleIds)));
    }
}
