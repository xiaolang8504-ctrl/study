package com.study.module.system.user.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.module.system.user.entity.User;
import com.study.module.system.user.mapper.UserMapper;
import com.study.module.system.user.service.UserEnableService;
import com.study.module.system.user.service.UserAuthorityCacheService;
import com.study.module.system.user.service.UserService;
import com.study.common.core.constants.Enable;
import com.study.common.core.enums.ErrorCodeConstants;
import com.study.common.core.exception.LogicException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.Collections;

/**
 * 账户实现类
 */
@Service
public class UserEnableServiceImpl extends ServiceImpl<UserMapper, User> implements UserEnableService {

    @Autowired
    UserService userService;

    @Autowired
    UserAuthorityCacheService userAuthorityCacheService;

    /**
     * 启禁用账户
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void enableUser(Long id) {
        User user = userService.checkUserByUserId(id);
        User user1 = new User();
        if(user.getStatus()== Enable.DISABLE){
            user1.setStatus(Enable.ENABLE);
        }else{
            user1.setStatus(Enable.DISABLE);
        }
        user1.setId(id);
        user1.setUpdateTime(LocalDateTime.now());
        if (!this.updateById(user1)){
            throw new LogicException(ErrorCodeConstants.UPDATE_USER_FAIL);
        }
        // 启禁用后不沿用旧会话；启用的账号也需要重新登录获取最新资料和权限。
        userAuthorityCacheService.invalidateUserSessionsAfterCommit(Collections.singleton(id));
    }
}
