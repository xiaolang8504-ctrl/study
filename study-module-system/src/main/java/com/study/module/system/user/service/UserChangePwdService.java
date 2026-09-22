package com.study.module.system.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.study.module.system.user.dto.request.ChangePwdForm;
import com.study.module.system.user.dto.request.ChangeUserPwdForm;
import com.study.module.system.user.entity.User;

/**
 * 系统用户表 服务类
 */
public interface UserChangePwdService extends IService<User> {

    /**
     * 密码修改(管理员)
     */
    void updatePassWord(ChangePwdForm request);

    /**
     * 密码修改(个人自己修改)
     */
    void updateUserPassWord(ChangeUserPwdForm request);
}