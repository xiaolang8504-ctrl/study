package com.study.module.system.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.study.module.system.user.entity.User;

/**
 * 系统用户表 服务类
 */
public interface UserDeleteService extends IService<User> {

    /**
     * 删除用户
     */
    void deleteUser(Long id);
}