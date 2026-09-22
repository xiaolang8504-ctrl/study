package com.study.module.system.role.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.study.module.system.role.entity.Role;

import java.util.List;
import java.util.Map;

/**
 * 系统角色表 服务类
 */
public interface RoleOptionsService extends IService<Role> {

    /**
     * 角色下拉列表
     */
    List<Map<String, Object>> roleOptions();
}