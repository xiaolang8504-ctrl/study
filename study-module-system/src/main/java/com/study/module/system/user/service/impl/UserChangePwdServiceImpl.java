package com.study.module.system.user.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.module.system.user.dto.request.ChangePwdForm;
import com.study.module.system.user.dto.request.ChangeUserPwdForm;
import com.study.module.system.user.entity.User;
import com.study.module.system.user.mapper.UserMapper;
import com.study.module.system.user.service.UserChangePwdService;
import com.study.module.system.user.service.UserAuthorityCacheService;
import com.study.module.system.user.service.UserService;
import com.study.common.core.enums.ErrorCodeConstants;
import com.study.common.core.exception.LogicException;
import com.yunshang.budget.common.security.utils.AccountUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

/**
 * 系统用户表 服务实现类
 */
@Service
public class UserChangePwdServiceImpl extends ServiceImpl<UserMapper, User> implements UserChangePwdService {

    @Autowired
    UserService userService;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    UserAuthorityCacheService userAuthorityCacheService;

    /**
     * 密码修改(管理员)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePassWord(ChangePwdForm request) {
        userService.checkUserByUserId(request.getId());
        if (!request.getNewPassWord().equals(request.getConfirmPassWord())){
            throw new LogicException(ErrorCodeConstants.NEW_PASSWORD_DIFFERENT_CONFIRMED_PASSWORD);
        }
        User user = new User();
        user.setId(request.getId());
        user.setPassWord(passwordEncoder.encode(request.getNewPassWord()));
        if (!this.updateById(user)){
            throw new LogicException(ErrorCodeConstants.UPDATE_PASSWORD_FAIL);
        }
        userAuthorityCacheService.invalidateUserSessionsAfterCommit(Collections.singleton(request.getId()));
    }

    /**
     * 密码修改(个人自己修改)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUserPassWord(ChangeUserPwdForm request) {
        User userOne = userService.checkUserByUserId(AccountUtils.getUserId());
        if (!request.getNewPassWord().equals(request.getConfirmPassWord())){
            throw new LogicException(ErrorCodeConstants.NEW_PASSWORD_DIFFERENT_CONFIRMED_PASSWORD);
        }
        if (!passwordEncoder.matches(request.getPassWord(), userOne.getPassWord())) {
            throw new LogicException(ErrorCodeConstants.OLD_PASSWORD_ERROR);
        }
        User user = new User();
        user.setId(userOne.getId());
        user.setPassWord(passwordEncoder.encode(request.getNewPassWord()));
        if (!this.updateById(user)){
            throw new LogicException(ErrorCodeConstants.UPDATE_PASSWORD_FAIL);
        }
        userAuthorityCacheService.invalidateUserSessionsAfterCommit(Collections.singleton(userOne.getId()));
    }
}
