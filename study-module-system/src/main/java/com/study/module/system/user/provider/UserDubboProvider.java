package com.study.module.system.user.provider;

import com.study.module.system.user.service.UserRoleService;
import com.study.module.system.user.service.UserService;
import com.study.api.dto.response.RoleData;
import com.study.api.dto.response.UserData;
import com.study.api.dto.response.UserInfoData;
import com.study.api.provider.UserProvider;
import com.study.common.core.exception.LogicException;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;

/**
 * 账号服务
 */
@DubboService
public class UserDubboProvider implements UserProvider {

    @Autowired
    UserService userService;

    @Autowired
    UserRoleService userRoleService;

    /**
     * 校验用户。
     */
    @Override
    public UserData checkUser(Long userId) throws LogicException {
        return userService.checkUserByProvider(userId);
    }

    /**
     * 校验用户。
     */
    @Override
    public boolean checkUserIdInRoleIds(Long userId, String roleIds) {
        return userRoleService.checkUserIdInRoleIds(userId, roleIds);
    }

    /**
     * 获取用户。
     */
    @Override
    public List<RoleData> getRoleListByUserId(Long userId) {
        return userRoleService.getRoleListByUserId(userId);
    }

    /**
     * 校验用户。
     */
    @Override
    public UserData checkUserByRealName(String realName) throws LogicException {
        return userService.checkUserByRealName(realName);
    }

    /**
     * 校验用户。
     */
    @Override
    public List<UserInfoData> checkUserListByUserIds(List<Long> userIds) throws LogicException {
        return userService.checkUserListByUserIds(userIds);
    }

    /**
     * 校验用户。
     */
    @Override
    public String checkUserNameByUserIds(List<Long> userIds) throws LogicException {
        return userService.checkUserNameByUserIds(userIds);
    }
}
