package com.study.module.system.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.module.system.dept.entity.Dept;
import com.study.module.system.dept.service.DeptService;
import com.study.module.system.user.convert.UserConvert;
import com.study.module.system.user.dto.request.UserUnionRoleReq;
import com.study.module.system.user.dto.response.UserDetailResp;
import com.study.module.system.user.dto.response.UserInfoResp;
import com.study.module.system.user.entity.User;
import com.study.module.system.user.mapper.UserMapper;
import com.study.module.system.user.service.UserRoleService;
import com.study.module.system.user.service.UserService;
import com.study.api.dto.response.RoleData;
import com.study.api.dto.response.UserData;
import com.study.api.dto.response.UserInfoData;
import com.study.common.core.constants.Enable;
import com.study.common.core.enums.ErrorCodeConstants;
import com.study.common.core.exception.LogicException;
import com.yunshang.budget.common.security.component.JwtUtils;
import com.yunshang.budget.common.security.config.JwtTokenConfig;
import com.yunshang.budget.common.security.utils.AccountUtils;
import com.study.module.system.resource.service.ResourceListService;
import com.study.module.system.role.service.RoleService;
import com.study.module.system.user.constants.Constant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 系统用户表 服务实现类
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    UserMapper userMapper;

    @Autowired
    ResourceListService resourceListService;

    @Autowired
    DeptService deptService;

    @Autowired
    UserRoleService userRoleService;

    @Autowired
    RoleService roleService;

    @Autowired
    JwtTokenConfig jwtTokenConfig;

    @Autowired
    HttpServletRequest httpServletRequest;

    /**
     * 用户ID查询账号信息
     */
    private User getUserByUserId(Long id){
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getId,id).last("limit 1");
        return this.getOne(queryWrapper);
    }

    /**
     * 获得当前登录用户信息
     */
    @Override
    public UserInfoResp getCurrentUserInfo() {
        User user = checkCurrentUser();
        UserInfoResp userInfoResp = UserConvert.INSTANCE.toUserInfo(user);
        //获取对应的部门ID
        if (user.getDeptId() != null) {
            Dept dept = deptService.checkDeptById(user.getDeptId());
            userInfoResp.setDeptPath(dept.getDeptPath());
        }
        List<Integer> roleIds = userRoleService.isRoleIdsByUserId(user.getId());
        if (!roleIds.isEmpty()) {
            Set<String> roles = roleIds.stream().map(Object::toString).collect(Collectors.toSet());
            Set<String> roleNames = roleService.roleListByIds(roleIds).stream()
                    .map(RoleData::getRoleName)
                    .collect(Collectors.toSet());
            userInfoResp.setRoles(roles);
            userInfoResp.setRoleNames(roleNames);
        }
        //管理员角色有所有的菜单和API
        if (roleService.isAdminByUserId(user.getId())) {
            userInfoResp.setResources(resourceListService.resourceAllCode());
            userInfoResp.setMenu(Collections.singleton(Constant.ADMIN));
        } else {
            userInfoResp.setResources(userMapper.getUserResources(user.getId()));
            userInfoResp.setMenu(userMapper.getUserMenus(user.getId()));
        }
        return userInfoResp;
    }

    /**
     * 获取当前登录账号信息
     */
    public User checkCurrentUser() {
        return getUserByUserId(AccountUtils.getUserId());
    }

    /**
     * 检测用户ID
     */
    @Override
    public User checkUserByUserId(Long id) {
        User user = getUserByUserId(id);
        if (user == null){
            throw new LogicException(ErrorCodeConstants.USER_NOT_EXIST);
        }
        return user;
    }

    /**
     * 检测用户名
     */
    @Override
    public User checkUserByUserName(String userName) {
        User user = getUserByUserName(userName);
        if (user == null){
            throw new LogicException(ErrorCodeConstants.USER_NOT_EXIST);
        }
        return user;
    }

    /**
     * 通过用户ID获取用户信息
     */
    @Override
    public UserDetailResp getUserDetailByUserId(Long id) {
        User user = checkUserByUserId(id);
        UserDetailResp userDetailResp = UserConvert.INSTANCE.toUserDetail(user);
        userDetailResp.setId(user.getId());
        userDetailResp.setRoles(userMapper.getUserRoles(user.getId()));
        if (roleService.isAdminByUserId(id)) {
            userDetailResp.setResources(resourceListService.resourceAllCode());
        }else{
            userDetailResp.setResources(userMapper.getUserResources(user.getId()));
        }
        return userDetailResp;
    }

    /**
     * 用户名查询账号信息
     */
    @Override
    public User getUserByUserName(String userName) {
        return this.getOne(new LambdaQueryWrapper<User>().eq(User::getUserName,userName));
    }

    /**
     * 用户名ID查询账号信息
     */
    @Override
    public User getUserByUserNameId(Long id, String userName) {
        return this.getOne(new LambdaQueryWrapper<User>().eq(User::getUserName,userName).ne(User::getId,id));
    }

    /**
     * 账号设置角色
     */
    @Override
    public void userUnionRole(UserUnionRoleReq request) {
        checkUserByUserId(request.getId());
        List<Integer> ids = Arrays.stream(request.getRoleIds().split(",")).map(Integer::parseInt).collect(Collectors.toList());
        userRoleService.userUnionRole(request.getId(),ids);
    }

    /**
     * 用户ID获取用户信息(对外)
     */
    @Override
    public UserData checkUserByProvider(Long id) {
        return UserConvert.INSTANCE.toUserData(checkUserByUserId(id));
    }

    /**
     * 查询部门id对应的用户
     */
    @Override
    public User checkUserByDeptId(Integer deptId) {
        return this.getOne(new LambdaQueryWrapper<User>().eq(User::getDeptId,deptId).last("limit 1"));
    }

    /**
     * 根据真实姓名校验用户
     */
    @Override
    public UserData checkUserByRealName(String realName) {
        User user = this.getOne(new LambdaQueryWrapper<User>().eq(User::getRealName,realName));
        if (user == null){
            throw new LogicException(ErrorCodeConstants.USER_NOT_EXIST);
        }
        return UserConvert.INSTANCE.toUserData(user);
    }

    /**
     * 根据用户编号批量校验用户
     */
    @Override
    public List<UserInfoData> checkUserListByUserIds(List<Long> userIds) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(User::getId,userIds).eq(User::getStatus, Enable.ENABLE);
        List<UserInfoData> list = this.list(queryWrapper).stream().map(user -> {
            UserInfoData userInfoData = UserConvert.INSTANCE.toUserInfoData(user);
            List<Integer> roleIds = userRoleService.isRoleIdsByUserId(user.getId());
            if (roleIds != null && roleIds.size() > 0){
                List<String> roleNames = roleService.roleListByIds(roleIds).stream().map(RoleData::getRoleName).collect(Collectors.toList());
                userInfoData.setRoleNames(roleNames.stream().map(Object::toString).collect(Collectors.joining(",")));
                userInfoData.setRoleIds(roleIds.stream().map(Object::toString).collect(Collectors.joining(",")));
            }
            return userInfoData;
        }).collect(Collectors.toList());

        return list;
    }

    /**
     * 查询部门ID对应的用户IDS
     */
    @Override
    public List<Long> getUserIdsByDeptId(Integer deptId) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getDeptId,deptId);
        return this.list(queryWrapper).stream().map(User::getId).distinct().collect(Collectors.toList());
    }

    /**
     * 用户IDS获取用户姓名
     */
    @Override
    public String checkUserNameByUserIds(List<Long> userIds) {
        if (userIds.isEmpty()){
            return "";
        }
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(User::getId,userIds);
        List<String> names = this.list(queryWrapper).stream().map(User::getRealName).distinct().collect(Collectors.toList());
        return String.join(",",names);
    }

    /**
     * 从请求TOKEN中获取当前账号ID
     */
    @Override
    public Long getUserIdByToken() {
        Long userId = JwtUtils.getUserIdByToken(
                httpServletRequest,
                jwtTokenConfig.getTokenHeader(),
                jwtTokenConfig.getTokenHead(),
                jwtTokenConfig.getSecretKey()
        );
        if (userId == null) {
            throw new LogicException(ErrorCodeConstants.NO_LOGIN);
        }
        return userId;
    }

}
