package com.study.module.system.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.study.module.system.user.dto.request.UserUnionRoleReq;
import com.study.module.system.user.dto.response.UserDetailResp;
import com.study.module.system.user.dto.response.UserInfoResp;
import com.study.module.system.user.entity.User;
import com.study.api.dto.response.UserData;
import com.study.api.dto.response.UserInfoData;

import java.util.List;

/**
 * 系统用户表 服务类
 */
public interface UserService extends IService<User> {

    /**
     * 获得当前登录用户信息
     */
    UserInfoResp getCurrentUserInfo();

    /**
     * 检测用户ID
     */
    User checkUserByUserId(Long id);

    /**
     * 检测用户名
     */
    User checkUserByUserName(String userName);

    /**
     * 通过用户ID获取用户信息
     */
    UserDetailResp getUserDetailByUserId(Long id);

    /**
     * 用户名查询账号信息
     */
    User getUserByUserName(String userName);

    /**
     * 用户名ID查询账号信息
     */
    User getUserByUserNameId(Long id, String userName);

    /**
     * 账号设置角色
     */
    void userUnionRole(UserUnionRoleReq request);

    /**
     * 用户ID获取用户信息(对外)
     */
    UserData checkUserByProvider(Long id);

    /**
     * 查询部门ID对应的用户
     */
    User checkUserByDeptId(Integer deptId);

    /**
     * 用户ID获取用户信息(对外)
     */
    UserData checkUserByRealName(String realName);

    /**
     * 用户IDS获取用户信息(对外)
     */
    List<UserInfoData> checkUserListByUserIds(List<Long> userIds);

    /**
     * 查询部门ID对应的用户IDS
     */
    List<Long> getUserIdsByDeptId(Integer deptId);

    /**
     * 用户IDS获取用户姓名
     */
    String checkUserNameByUserIds(List<Long> userIds);

    /**
     * 从请求TOKEN中获取当前账号ID
     */
    Long getUserIdByToken();
}
