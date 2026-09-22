package com.study.module.system.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.module.system.user.entity.UserRole;
import com.study.module.system.user.mapper.UserRoleMapper;
import com.study.module.system.user.service.UserAuthorityCacheService;
import com.study.module.system.user.service.UserRoleService;
import com.study.api.dto.response.RoleData;
import com.study.common.core.enums.ErrorCodeConstants;
import com.study.common.core.exception.LogicException;
import com.study.module.system.role.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户角色中间表 服务实现类
 */
@Service
public class UserRoleServiceImpl extends ServiceImpl<UserRoleMapper, UserRole> implements UserRoleService {

    @Autowired
    RoleService roleService;

    @Autowired
    UserAuthorityCacheService userAuthorityCacheService;

    /**
     * 查询角色ID是否对应的账户
     */
    @Override
    public boolean isUserByRoleId(Integer roleId) {
        if (this.getOne(new LambdaQueryWrapper<UserRole>().eq(UserRole::getRoleId, roleId)) != null){
            return true;
        }
        return false;
    }

    /**
     * 批量入库账号和角色关联数据
     */
    @Override
    public boolean bathSaveUserRole(Long userId, String roleIds) {
        //写入账号角色关联表
        List userRoLi = new ArrayList<>();
        String[] roles = roleIds.split(",");
        for (int i = 0;i<roles.length;i++){
            UserRole userRole = new UserRole();
            userRole.setRoleId(Integer.valueOf(roles[i]));
            userRole.setUserId(userId);
            userRole.setCreateTime(LocalDateTime.now());
            userRoLi.add(userRole);
        }
        return this.saveBatch(userRoLi);
    }

    /**
     * 批量删除账号和角色关联数据
     */
    @Override
    public boolean bathRemoveByUserId(Long userId) {
        return this.remove(new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserId, userId));
    }

    /**
     * 账号设置角色
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void userUnionRole(Long userId, List<Integer> roleIds) {
        List<UserRole> list = new ArrayList<>();
        //删除旧数据
        bathRemoveByUserId(userId);
        roleIds.forEach((item)-> {
            UserRole userRole = new UserRole();
            userRole.setUserId(userId);
            userRole.setRoleId(item);
            userRole.setCreateTime(LocalDateTime.now());
            list.add(userRole);
        });
        this.saveBatch(list);
        userAuthorityCacheService.refreshUserAuthorityCachesAfterCommit(Collections.singleton(userId));
    }

    /**
     * 查询用户ID获取对应的角色关联信息
     */
    @Override
    public List<UserRole> isRoleByUserId(Long userId) {
        return this.list(new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserId, userId));
    }

    /**
     * 查询用户ID获取对应的角色关联信息
     */
    @Override
    public List<Integer> isRoleIdsByUserId(Long userId) {
        return this.list(new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserId, userId)).stream().map(UserRole::getRoleId).collect(Collectors.toList());
    }

    /**
     * 判断用户ID是否在指定的角色中
     */
    @Override
    public boolean checkUserIdInRoleIds(Long userId, String roleIds) {
        List<Long> ids =  isUserIdsByRoleIds(roleIds);
        return ids.contains(userId);
    }

    /**
     * 通过用户id获取角色ID列表和角色名列表
     */
    @Override
    public List<RoleData> getRoleListByUserId(Long userId) {
        List<Integer> roleIds = isRoleIdsByUserId(userId);
        if (roleIds.isEmpty()){
            return Collections.emptyList();
        }
        return roleService.roleListByIds(roleIds);
    }

    /**
     * 获取角色IDS对应的账户IDS
     */
    @Override
    public List<Long> isUserIdsByRoleIds(String roleIds) {
        List<UserRole> list = this.list(new LambdaQueryWrapper<UserRole>().in(UserRole::getRoleId, roleIds.split(",")));
        if(list == null || list.size() == 0){
            throw new LogicException(ErrorCodeConstants.ROLE_USER_NAME_EXIST);
        }
        return list.stream().map(UserRole::getUserId).collect(Collectors.toList());
    }

    /**
     * 查询角色编号集合对应的用户编号集合；没有关联账号时返回空集合。
     */
    @Override
    public List<Long> listUserIdsByRoleIds(List<Integer> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return Collections.emptyList();
        }
        return this.list(new LambdaQueryWrapper<UserRole>().in(UserRole::getRoleId, roleIds)).stream()
                .map(UserRole::getUserId).distinct().collect(Collectors.toList());
    }

}
