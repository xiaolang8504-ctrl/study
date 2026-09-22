package com.study.module.system.user.controller;

import com.study.module.system.user.dto.request.*;
import com.study.module.system.user.dto.response.UserInfoResp;
import com.study.module.system.user.dto.response.UserListResp;
import com.study.module.system.user.service.*;
import com.study.common.core.domain.KeyValue;
import com.study.common.core.domain.Result;
import com.study.common.core.domain.dto.PageResult;
import com.study.common.core.utils.ResultUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    UserService userService;

    @Autowired
    UserListService userListService;

    @Autowired
    UserCreateService userCreateService;

    @Autowired
    UserUpdateService userUpdateService;

    @Autowired
    UserDeleteService userDeleteService;

    @Autowired
    UserChangePwdService userChangePwdService;

    @Autowired
    UserOptionsService userOptionsService;

    @Autowired
    UserEnableService userEnableService;

    /**
     * 用户分页列表
     */
    @PreAuthorize("hasAuthority('system:user:userPageList')")
    @GetMapping("/userPageList")
    public Result<PageResult> userPageList(@Validated UserPageListReq request) {
        return ResultUtils.success(userListService.userPageList(request));
    }

    /**
     * 添加用户
     */
    @PreAuthorize("hasAuthority('system:user:createUser')")
    @PostMapping("/createUser")
    public Result<Void> createUser(@RequestBody @Validated UserCreateReq request) {
        userCreateService.createUser(request);
        return ResultUtils.success();
    }

    /**
     * 修改用户
     */
    @PreAuthorize("hasAuthority('system:user:updateUser')")
    @PostMapping("/updateUser")
    public Result<Void> updateUser(@RequestBody @Validated UserUpdateReq request) {
        userUpdateService.updateUser(request);
        return ResultUtils.success();
    }

    /**
     * 删除用户
     */
    @PreAuthorize("hasAuthority('system:user:deleteUser')")
    @PostMapping("/deleteUser")
    public Result<Void> deleteUser(@RequestBody @Validated UserIdReq request) {
        userDeleteService.deleteUser(request.getId());
        return ResultUtils.success();
    }

    /**
     * 密码修改(管理员)
     */
    @PreAuthorize("hasAuthority('system:user:updatePassWord')")
    @PostMapping("/updatePassWord")
    public Result<Void> updatePassWord(@RequestBody @Validated ChangePwdForm request) {
        userChangePwdService.updatePassWord(request);
        return ResultUtils.success();
    }

    /**
     * 密码修改(个人自己修改)
     */
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/updateUserPassWord")
    public Result<Void> updateUserPassWord(@RequestBody @Validated ChangeUserPwdForm request) {
        userChangePwdService.updateUserPassWord(request);
        return ResultUtils.success();
    }

    /**
     * 账号下拉列表
     */
    @PreAuthorize("hasAuthority('system:user:userOptions')")
    @GetMapping("/userOptions")
    public Result<List<KeyValue<String, String>>> userOptions() {
        return ResultUtils.success(userOptionsService.userOptions());
    }

    /**
     * 获得当前登录用户信息(前端)
     */
    @GetMapping("/getCurrentUserInfo")
    public Result<UserInfoResp> getCurrentUserInfo() {
        return ResultUtils.success(userService.getCurrentUserInfo());
    }

    /**
     * 账号设置角色
     */
    @PreAuthorize("hasAuthority('system:user:userUnionRole')")
    @PostMapping("/userUnionRole")
    public Result<Void> userUnionRole(@RequestBody @Validated UserUnionRoleReq request) {
        userService.userUnionRole(request);
        return ResultUtils.success();
    }

    /**
     * 启禁用账号
     */
    @PreAuthorize("hasAuthority('system:user:enableUser')")
    @PostMapping("/enableUser")
    public Result<Void> enableUser(@RequestBody @Validated UserIdReq request) {
        userEnableService.enableUser(request.getId());
        return ResultUtils.success();
    }

    /**
     * 账号列表
     */
    @PreAuthorize("hasAuthority('system:user:userList')")
    @GetMapping("/userList")
    public Result<List<UserListResp>> userList(@Validated UserListReq request) {
        return ResultUtils.success(userListService.userList(request));
    }

}
