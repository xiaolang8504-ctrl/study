package com.study.module.system.user.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.module.system.dept.entity.Dept;
import com.study.module.system.dept.service.DeptService;
import com.study.module.system.user.convert.UserConvert;
import com.study.module.system.user.dto.request.UserListReq;
import com.study.module.system.user.dto.request.UserPageListReq;
import com.study.module.system.user.dto.response.UserListResp;
import com.study.module.system.user.dto.response.UserPageListResp;
import com.study.module.system.user.entity.User;
import com.study.module.system.user.mapper.UserMapper;
import com.study.module.system.user.service.UserListService;
import com.study.module.system.user.service.UserRoleService;
import com.study.module.system.user.service.UserService;
import com.study.common.core.domain.dto.PageResult;
import com.yunshang.budget.common.mybatis.utils.PageUtils;
import com.study.module.system.role.entity.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 用户实现类
 */
@Service
public class UserListServiceImpl extends ServiceImpl<UserMapper, User> implements UserListService {

    @Autowired
    UserMapper userMapper;

    @Autowired
    DeptService deptService;

    @Autowired
    UserService userService;

    @Autowired
    UserRoleService userRoleService;

    /**
     * 用户列表带筛选
     */
    @Override
    public PageResult userPageList(UserPageListReq request) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(ObjectUtil.isNotEmpty(request.getUserName()),User::getUserName,request.getUserName());
        queryWrapper.like(ObjectUtil.isNotEmpty(request.getRealName()),User::getRealName,request.getRealName());
        queryWrapper.eq(Objects.nonNull(request.getStatus()),User::getStatus,request.getStatus());
        if (Objects.nonNull(request.getDeptId())){
            List<Integer> ids = deptService.getDeptSonIdsById(request.getDeptId());
            if (ids.isEmpty()){
                queryWrapper.eq(User::getDeptId,request.getDeptId());
            }else{
                queryWrapper.in(User::getDeptId,ids);
            }
        }
        queryWrapper.orderByAsc(User::getId);
        Page<User> page = new Page<>(request.getCurrent(), request.getPageSize());
        this.page(page, queryWrapper);
        return PageUtils.wrap(page, this::userPageRecords);
    }

    /**
     * 账号列表
     */
    @Override
    public List<UserListResp> userList(UserListReq request) {
        boolean status = false;
        if(ObjectUtil.isEmpty(request.getDeptId()) && ObjectUtil.isEmpty(request.getIds()) && ObjectUtil.isEmpty(request.getRealName())){
            return Collections.emptyList();
        }
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(ObjectUtil.isNotEmpty(request.getRealName()),User::getRealName,request.getRealName());
        if(ObjectUtil.isNotEmpty(request.getRealName())){
            status = true;
        }
        if(ObjectUtil.isNotEmpty(request.getIds())){
            List<Long> ids = Arrays.stream(request.getIds().split(",")).map(Long::parseLong).collect(Collectors.toList());
            queryWrapper.in(User::getId,ids);
            status = true;
        }
        if(ObjectUtil.isNotEmpty(request.getDeptId())){
            List<Long> ids1 = userService.getUserIdsByDeptId(request.getDeptId());
            if(ids1 != null && ids1.size() > 0){
                queryWrapper.in(User::getId,ids1);
                status = true;
            }
        }
        if(ObjectUtil.isNotEmpty(request.getRoleIds())){
            List<Long> idsUser = userRoleService.isUserIdsByRoleIds(request.getRoleIds());
            if(idsUser != null && idsUser.size() > 0){
                queryWrapper.in(User::getId,idsUser);
                status = true;
            }
        }
        if(status){
            return UserConvert.INSTANCE.toUserListResp(this.list(queryWrapper));
        }else{
            return Collections.emptyList();
        }
    }

    /**
     * 处理数据
     */
    private List<UserPageListResp> userPageRecords(List<User> list) {
        List<UserPageListResp> listRes = new ArrayList<>();
        list.forEach((item)-> {
            UserPageListResp userPageListResp = UserConvert.INSTANCE.toUserPageList(item);
            //获取账号对应的角色信息
            List<Role> list1 = userMapper.getUserRolesByUserId(item.getId());
            if (!list1.isEmpty()){
                List<String> roleNameList = list1.stream().map(Role::getRoleName).collect(Collectors.toList());
                userPageListResp.setRoleNames(String.join(",", roleNameList));
                List<Integer> roleIdList = list1.stream().map(Role::getId).collect(Collectors.toList());
                userPageListResp.setRoleIds(roleIdList);
            }
            if (item.getDeptId()!= null){
                Dept dept = deptService.getDeptById(item.getDeptId());
                userPageListResp.setDeptPath(dept != null ? dept.getDeptPath() : "");
            }
            listRes.add(userPageListResp);
        });
        return listRes;
    }
}
