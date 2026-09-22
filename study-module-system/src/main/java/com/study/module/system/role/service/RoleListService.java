package com.study.module.system.role.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.study.module.system.role.dto.request.RoleListReq;
import com.study.module.system.role.dto.response.RoleListResp;
import com.study.module.system.role.entity.Role;

import java.util.List;

/**
 * 系统角色表 服务类
 */
public interface RoleListService extends IService<Role> {

    /**
     * 角色列表带名称筛选
     */
    List<RoleListResp> roleList(RoleListReq request);

    /**
     * 角色IDS对应的角色列表
     */
    List<Role> roleListById(String ids);
}