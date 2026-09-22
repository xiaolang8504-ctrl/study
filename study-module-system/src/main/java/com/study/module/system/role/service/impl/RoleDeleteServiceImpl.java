package com.study.module.system.role.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.common.core.enums.ErrorCodeConstants;
import com.study.common.core.exception.LogicException;
import com.study.module.system.role.entity.Role;
import com.study.module.system.role.mapper.RoleMapper;
import com.study.module.system.role.service.RoleDeleteService;
import com.study.module.system.role.service.RoleMenuService;
import com.study.module.system.role.service.RoleService;
import com.study.module.system.user.service.UserRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 系统角色表 服务实现类
 */
@SuppressWarnings({"Duplicates", "SpringJavaInjectionPointsAutowiringInspection"})
@Service
public class RoleDeleteServiceImpl extends ServiceImpl<RoleMapper, Role> implements RoleDeleteService {

    @Autowired
    RoleMenuService roleMenuService;

    @Autowired
    RoleService roleService;

    @Autowired
    UserRoleService userRoleService;

    /**
     * 删除角色
     */
    @Override
    public void deleteRole(Integer id) {
        // 参数逻辑校验
        deleteValidate(id);

        //数据入库
        deleteRoleData(id);
    }

    /**
     * 参数逻辑校验
     */
    private void deleteValidate(Integer id){
        roleService.checkRoleById(id);
        if (userRoleService.isUserByRoleId(id)){
            throw new LogicException(ErrorCodeConstants.ROLE_USER_EXIST);
        }
    }

    /**
     * 组合数据
     */
    private void deleteRoleData(Integer id){
        //删除关联表的数据
        roleMenuService.deleteRoleMenuByRoleId(id);
        if (!this.removeById(id)){
            throw new LogicException(ErrorCodeConstants.DELETE_ROLE_FAIL);
        }
    }
}