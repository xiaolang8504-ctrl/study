package com.study.module.system.user.service;

import com.study.module.system.user.dto.request.UserListReq;
import com.study.module.system.user.dto.request.UserPageListReq;
import com.study.module.system.user.dto.response.UserListResp;
import com.study.common.core.domain.dto.PageResult;

import java.util.List;

public interface UserListService {

    /**
     * 用户列表带筛选
     */
    PageResult userPageList(UserPageListReq request);

    /**
     * 账号列表
     */
    List<UserListResp> userList(UserListReq request);
}