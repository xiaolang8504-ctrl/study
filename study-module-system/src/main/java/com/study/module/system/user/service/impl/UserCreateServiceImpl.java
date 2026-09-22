package com.study.module.system.user.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.module.system.dept.service.DeptService;
import com.study.module.system.user.convert.UserConvert;
import com.study.module.system.user.dto.request.UserCreateReq;
import com.study.module.system.user.entity.User;
import com.study.module.system.user.mapper.UserMapper;
import com.study.module.system.user.service.EmailService;
import com.study.module.system.user.service.UserCreateService;
import com.study.module.system.user.service.UserRoleService;
import com.study.module.system.user.service.UserService;
import com.study.common.core.enums.ErrorCodeConstants;
import com.study.common.core.exception.LogicException;
import com.study.common.core.utils.CollUtils;
import com.study.module.system.msg.constants.MsgType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

/**
 * 系统用户表 服务实现类
 */
@Service
public class UserCreateServiceImpl extends ServiceImpl<UserMapper, User> implements UserCreateService {

    @Autowired
    UserService userService;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    UserRoleService userRoleService;

    @Autowired
    DeptService deptService;

    @Autowired
    EmailService emailService;

    /**
     * 添加用户
     */
    @Override
    public void createUser(UserCreateReq request) {
        // 参数逻辑校验
        createValidate(request);

        //数据入库
        createUserData(request);
    }

    /**
     * 参数校验
     */
    private void createValidate(UserCreateReq request){
        if (userService.getUserByUserName(request.getUserName()) != null){
            throw new LogicException(ErrorCodeConstants.USER_NAME_EXIST);
        }
    }

    /**
     * 组合数据
     */
    private void createUserData(UserCreateReq request){
        //组合数据包
        User user = UserConvert.INSTANCE.toUser(request);
        user.setCreateTime(LocalDateTime.now());
        String passWord = CollUtils.roundingPassWordStr();
        emailService.sendEmail(MsgType.ADD_MEMBER,request.getEmail(),passWord);
        user.setPassWord(passwordEncoder.encode(passWord));
        if (request.getDeptId() != null){
            user.setDeptName(deptService.checkDeptById(request.getDeptId()).getDeptName());
        }
        if (!this.save(user)){
            throw new LogicException(ErrorCodeConstants.CREATE_USER_FAIL);
        }
        //写入账号角色关联表
        if (!request.getRoleIds().isEmpty()){
            userRoleService.bathSaveUserRole(user.getId(),request.getRoleIds());
        }
    }
}