package com.study.module.system.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.study.module.system.user.entity.User;
import com.study.common.core.domain.KeyValue;

import java.util.List;

/**
 * 系统用户表 服务类
 */
public interface UserOptionsService extends IService<User> {

    /**
     * 账户下拉列表
     */
    List<KeyValue<String, String>> userOptions();
}