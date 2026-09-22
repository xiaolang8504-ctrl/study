package com.study.module.system.user.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.module.system.dept.service.DeptService;
import com.study.module.system.user.config.SystemConfig;
import com.study.module.system.user.entity.User;
import com.study.module.system.user.mapper.UserMapper;
import com.study.module.system.user.service.UserDeleteService;
import com.study.module.system.user.service.UserAuthorityCacheService;
import com.study.module.system.user.service.UserRoleService;
import com.study.module.system.user.service.UserService;
import com.study.common.core.enums.ErrorCodeConstants;
import com.study.common.core.exception.LogicException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

/**
 * 系统用户表 服务实现类
 */
@Service
public class UserDeleteServiceImpl extends ServiceImpl<UserMapper, User> implements UserDeleteService {

    @Autowired
    UserService userService;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    UserRoleService userRoleService;

    @Autowired
    DeptService deptService;

    @Autowired
    SystemConfig systemConfig;

    @Autowired
    UserAuthorityCacheService userAuthorityCacheService;

    /**
     * 添加用户
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteUser(Long id) {
        // 参数逻辑校验
        deleteValidate(id);

        //数据入库
        deleteUserData(id);
        userAuthorityCacheService.invalidateUserSessionsAfterCommit(Collections.singleton(id));
    }

    /**
     * 参数校验
     */
    private void deleteValidate(Long id){
        userService.checkUserByUserId(id);
        if (systemConfig.getAdminId().equals(id)){
            throw new LogicException(ErrorCodeConstants.ADMIN_CANNOT_DELETE);
        }
    }

    /**
     * 组合数据
     */
    private void deleteUserData(Long id){
        //组合数据包
        if (!this.removeById(id)){
            throw new LogicException(ErrorCodeConstants.DELETE_USER_FAIL);
        }
        //批量删除账号和角色关联数据
        userRoleService.bathRemoveByUserId(id);
    }
}
