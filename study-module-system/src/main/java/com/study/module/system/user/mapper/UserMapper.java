package com.study.module.system.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.study.module.system.user.entity.User;
import com.study.module.system.role.entity.Role;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;
import java.util.Set;

/**
 * 系统用户表 Mapper 接口
 */
public interface UserMapper extends BaseMapper<User> {

    /**
     * 查询用户可用的角色编码
     * @param userId 用户id（用户名）
     * @return
     */
    @Select("select distinct a.role_id from sys_user_role a, sys_role b  where a.role_id = b.id and a.user_id = #{user_id}  ")
    Set<String> getUserRoles(@Param("user_id") Long userId);

    /**
     * 查询用户的菜单
     * @param userId
     * @return
     */
    @Select("SELECT distinct d.code from sys_user as a LEFT JOIN sys_user_role as b on a.id = b.user_id LEFT JOIN sys_role_menu as c on b.role_id = c.role_id LEFT JOIN sys_menu as d on c.menu_id = d.id WHERE a.id = #{user_id}")
    Set<String> getUserMenus(@Param("user_id") Long userId);

    /**
     * 查询用户可用的资源编码
     * @param userId
     * @return
     */
    @Select("select distinct d.code from sys_user_role a, sys_role_menu b, sys_menu_resource c, sys_resource d  " +
            "where a.role_id = b.role_id and b.menu_id = c.menu_id and c.resource_id = d.id and a.user_id = #{user_id} ")
    Set<String> getUserResources(@Param("user_id") Long userId);

    /**
     * 查询用户可用的角色
     * @param userId 用户id（用户名）
     * @return
     */
    @Select("select distinct b.id, b.role_name as roleName from sys_user_role a, sys_role b  where a.role_id = b.id and a.user_id = #{user_id}")
    List<Role> getUserRolesByUserId(@Param("user_id") Long userId);
}