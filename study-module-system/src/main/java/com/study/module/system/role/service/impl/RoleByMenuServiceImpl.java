package com.study.module.system.role.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.common.core.enums.ErrorCodeConstants;
import com.study.common.core.exception.LogicException;
import com.study.module.system.role.dto.request.UpdateRoleMenuReq;
import com.study.module.system.role.entity.Role;
import com.study.module.system.role.mapper.RoleMapper;
import com.study.module.system.role.service.RoleByMenuService;
import com.study.module.system.role.service.RoleMenuService;
import com.study.module.system.role.service.RoleService;
import com.study.module.system.user.service.UserAuthorityCacheService;
import com.study.module.system.user.service.UserRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 系统角色表 服务实现类
 */
@SuppressWarnings({"Duplicates", "SpringJavaInjectionPointsAutowiringInspection"})
@Service
public class RoleByMenuServiceImpl extends ServiceImpl<RoleMapper, Role> implements RoleByMenuService {

    @Autowired
    RoleMenuService roleMenuService;

    @Autowired
    RoleService roleService;

    @Autowired
    UserRoleService userRoleService;

    @Autowired
    UserAuthorityCacheService userAuthorityCacheService;

    /**
     * 角色分配菜单
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void roleByMenu(UpdateRoleMenuReq request) {
        // 参数逻辑校验
        roleService.checkRoleById(request.getId());

        //数据入库
        roleByMenuData(request);
    }

    /**
     * 组合数据
     */
    private void roleByMenuData(UpdateRoleMenuReq request){
        //删除关联表的数据
        roleMenuService.deleteRoleMenuByRoleId(request.getId());
        //同步数据到菜单角色关联表
        roleMenuService.bathSaveRoleMenu(request.getId(),request.getMenuIds());
        Role role = new Role();
        role.setId(request.getId());
        role.setMenuIds(request.getMenuIds());
        role.setMenuLevel(request.getMenuLevel());
        if (!this.updateById(role)){
            throw new LogicException(ErrorCodeConstants.SET_ROLE_MENU_FAIL);
        }
        userAuthorityCacheService.refreshUserAuthorityCachesAfterCommit(
                userRoleService.listUserIdsByRoleIds(java.util.Collections.singletonList(request.getId())));
    }
}
