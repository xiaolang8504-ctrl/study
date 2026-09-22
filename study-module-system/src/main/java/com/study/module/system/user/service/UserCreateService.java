package com.study.module.system.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.study.module.system.user.dto.request.UserCreateReq;
import com.study.module.system.user.entity.User;
import org.springframework.transaction.annotation.Transactional;

/**
 * 系统用户表 服务类
 */
public interface UserCreateService extends IService<User> {

    /**
     * 添加用户
     */
    @Transactional(rollbackFor = Exception.class)
    void createUser(UserCreateReq request);
}