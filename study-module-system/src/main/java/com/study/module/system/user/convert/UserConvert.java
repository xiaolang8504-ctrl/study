package com.study.module.system.user.convert;

import com.study.module.system.user.dto.request.UserCreateReq;
import com.study.module.system.user.dto.request.UserUpdateReq;
import com.study.module.system.user.dto.response.UserDetailResp;
import com.study.module.system.user.dto.response.UserInfoResp;
import com.study.module.system.user.dto.response.UserListResp;
import com.study.module.system.user.dto.response.UserPageListResp;
import com.study.module.system.user.entity.User;
import com.study.api.dto.response.UserData;
import com.study.api.dto.response.UserInfoData;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 系统用户MapStruct 接口
 */
@Mapper
public interface UserConvert {

    UserConvert INSTANCE=Mappers.getMapper(UserConvert.class);

    /**
     * 用户详情数据处理
     */
    UserDetailResp toUserDetail(User user);

    /**
     * 用户列表数据处理
     */
    UserPageListResp toUserPageList(User user);

    /**
     * 用户入库处理
     */
    User toUser(UserCreateReq userCreateReq);

    /**
     * 用户入库处理
     */
    User toUser(UserUpdateReq userUpdateReq);

    /**
     * 用户详情数据处理
     */
    UserInfoResp toUserInfo(User user);

    /**
     * 用户ID获取用户信息(对外)
     */
    UserData toUserData(User user);

    /**
     * 用户ID获取用户信息(对外)
     */
    UserInfoData toUserInfoData(User user);

    /**
     * 用户数据转化处理
     */
    List<UserListResp> toUserListResp(List<User> list);
}