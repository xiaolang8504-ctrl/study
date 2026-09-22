package com.study.module.system.role.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.study.module.system.role.dto.request.RoleDetailReq;
import com.study.module.system.role.dto.response.RoleDetailResp;
import com.study.module.system.role.entity.Role;

/**
 * 系统角色表 服务类
 */
public interface RoleDetailService extends IService<Role> {

    /**
     * 角色详情
     */
    RoleDetailResp roleDetail(RoleDetailReq request);
}