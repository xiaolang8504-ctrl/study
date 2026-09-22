package com.study.api.provider;

import com.study.api.dto.response.RoleData;
import com.study.api.dto.response.UserData;
import com.study.api.dto.response.UserInfoData;
import com.study.common.core.exception.LogicException;
import java.util.List;

/**
 * 账号服务
 */
public interface UserProvider {

    /**
     * 用户信息
     */
    UserData checkUser(Long userId) throws LogicException;

    /**
     * 判断用户ID是否在指定的角色中
     */
    boolean checkUserIdInRoleIds(Long userId, String roleIds);

    /**
     * 通过用户id获取角色ID列表和角色名列表
     */
    List<RoleData> getRoleListByUserId(Long userId);

    /**
     * 用户姓名获取用户信息
     */
    UserData checkUserByRealName(String realName) throws LogicException;

    /**
     * 用户姓名获取用户信息
     */
    List<UserInfoData> checkUserListByUserIds(List<Long> userIds) throws LogicException;

    /**
     * 用户IDS获取用户姓名
     */
    String checkUserNameByUserIds(List<Long> userIds) throws LogicException;
}
