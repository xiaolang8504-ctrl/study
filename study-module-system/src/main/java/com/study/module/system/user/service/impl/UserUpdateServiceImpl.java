package com.study.module.system.user.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.module.system.dept.service.DeptService;
import com.study.module.system.user.convert.UserConvert;
import com.study.module.system.user.dto.request.UserUpdateReq;
import com.study.module.system.user.entity.User;
import com.study.module.system.user.mapper.UserMapper;
import com.study.module.system.user.service.UserRoleService;
import com.study.module.system.user.service.UserAuthorityCacheService;
import com.study.module.system.user.service.UserService;
import com.study.module.system.user.service.UserUpdateService;
import com.study.common.core.enums.ErrorCodeConstants;
import com.study.common.core.exception.LogicException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;

/**
 * 系统用户表 服务实现类
 */
@Service
public class UserUpdateServiceImpl extends ServiceImpl<UserMapper, User> implements UserUpdateService {

    @Autowired
    UserService userService;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    UserRoleService userRoleService;

    @Autowired
    DeptService deptService;

    @Autowired
    UserAuthorityCacheService userAuthorityCacheService;

    /**
     * 添加用户
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUser(UserUpdateReq request) {
        // 参数逻辑校验
        updateValidate(request);

        //数据入库
        updateUserData(request);
        // 用户资料、密码或角色可能同时改变，统一撤销旧登录态以避免旧令牌继续使用。
        userAuthorityCacheService.invalidateUserSessionsAfterCommit(Collections.singleton(request.getId()));
    }

    /**
     * 参数校验
     */
    private void updateValidate(UserUpdateReq request){
        if (userService.getUserByUserNameId(request.getId(),request.getUserName()) != null){
            throw new LogicException(ErrorCodeConstants.USER_NAME_EXIST);
        }
    }

    /**
     * 组合数据
     */
    private void updateUserData(UserUpdateReq request){
        //组合数据包
        User user = UserConvert.INSTANCE.toUser(request);
        if (request.getDeptId() != null){
            user.setDeptName(deptService.checkDeptById(request.getDeptId()).getDeptName());
        }else {
            user.setDeptName("");
        }
        user.setUpdateTime(LocalDateTime.now());
        if (!this.updateById(user)){
            throw new LogicException(ErrorCodeConstants.UPDATE_USER_FAIL);
        }
        //写入账号角色关联表
        userRoleService.bathRemoveByUserId(user.getId());
        userRoleService.bathSaveUserRole(user.getId(),request.getRoleIds());
    }
}
