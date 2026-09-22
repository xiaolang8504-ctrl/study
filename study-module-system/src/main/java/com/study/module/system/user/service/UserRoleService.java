package com.study.module.system.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.study.module.system.user.entity.UserRole;
import com.study.api.dto.response.RoleData;

import java.util.List;

/**
 * 用户角色中间表 服务类
 */
public interface UserRoleService extends IService<UserRole> {

    /**
     * 查询角色ID是否对应的账户
     */
    boolean isUserByRoleId(Integer roleId);

    /**
     * 批量入库账号和角色关联数据
     */
    boolean bathSaveUserRole(Long userId, String roleIds);

    /**
     * 批量删除账号和角色关联数据
     */
    boolean bathRemoveByUserId(Long userId);

    /**
     * 账号设置角色
     */
    void userUnionRole(Long userId, List<Integer> roleIds);

    /**
     * 查询用户ID获取对应的角色关联信息
     */
    List<UserRole> isRoleByUserId(Long userId);

    /**
     * 查询用户ID获取对应的角色IDS
     */
    List<Integer> isRoleIdsByUserId(Long userId);

    /**
     * 判断用户ID是否在指定的角色中
     */
    boolean checkUserIdInRoleIds(Long userId, String roleIds);

    /**
     * 通过用户id获取角色ID列表和角色名列表
     */
    List<RoleData> getRoleListByUserId(Long userId);

    /**
     * 查询角色IDS获取对应的用户IDS
     */
    List<Long> isUserIdsByRoleIds(String roleIds);

    /**
     * 查询角色编号集合对应的用户编号集合。
     */
    List<Long> listUserIdsByRoleIds(List<Integer> roleIds);

}
