package com.study.module.system.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.study.module.system.user.dto.request.UserUpdateReq;
import com.study.module.system.user.entity.User;
import org.springframework.transaction.annotation.Transactional;

/**
 * 系统用户表 服务类
 */
public interface UserUpdateService extends IService<User> {

    /**
     * 修改用户
     */
    @Transactional(rollbackFor = Exception.class)
    void updateUser(UserUpdateReq request);
}