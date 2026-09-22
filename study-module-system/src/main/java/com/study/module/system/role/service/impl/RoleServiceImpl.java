package com.study.module.system.role.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.api.dto.response.RoleData;
import com.study.common.core.enums.ErrorCodeConstants;
import com.study.common.core.exception.LogicException;
import com.study.module.system.menu.service.MenuService;
import com.study.module.system.role.convert.RoleConvert;
import com.study.module.system.role.entity.Role;
import com.study.module.system.role.mapper.RoleMapper;
import com.study.module.system.role.service.RoleService;
import com.study.module.system.user.service.UserRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.*;

/**
 * 系统角色表 服务实现类
 */
@SuppressWarnings({"Duplicates", "SpringJavaInjectionPointsAutowiringInspection"})
@Service
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements RoleService {

    @Autowired
    MenuService menuService;

    @Autowired
    UserRoleService userRoleService;

    /**
     * 检测指定角色名
     */
    @Override
    public boolean checkRoleName(String roleName){
        if ((this.getOne(new LambdaQueryWrapper<Role>().eq(Role::getRoleName, roleName)) != null)){
            return true;
        }
        return false;
    }

    /**
     * 检测指定角色名和ID
     */
    @Override
    public boolean checkRoleNameById(Integer id,String roleName) {
        if ((this.getOne(new LambdaQueryWrapper<Role>().eq(Role::getRoleName, roleName).ne(Role::getId,id)) != null)){
            return true;
        }
        return false;
    }

    /**
     * 判断当前账号是否包含管理员角色
     */
    @Override
    public boolean isAdminByUserId(Long userId) {
        List<Integer> roleIds = userRoleService.isRoleIdsByUserId(userId);
        if (roleIds.isEmpty()){
            return false;
        }
        LambdaQueryWrapper<Role> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Role::getId,roleIds);
        List<Role> list = this.list(queryWrapper);
        for (Role role : list) {
            if (role.getIsSystem().equals(1)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取指定IDS的角色数组
     */
    @Override
    public List<RoleData> roleListByIds(List<Integer> roleIds) {
        LambdaQueryWrapper<Role> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Role::getId,roleIds);
        return RoleConvert.INSTANCE.toRoleData(this.list(queryWrapper));
    }

    /**
     * 检测角色ID是否存在
     */
    @Override
    public Role checkRoleById(Integer id) {
        Role role = roleById(id);
        if (role == null){
            throw new LogicException(ErrorCodeConstants.ROLE_NOT_EXIST);
        }
        return role;
    }

    /**
     * 获取指定ID的角色
     */
    @Override
    public Role roleById(Integer id) {
        return this.getOne(new LambdaQueryWrapper<Role>().eq(Role::getId,id).last("limit 1"));
    }
}