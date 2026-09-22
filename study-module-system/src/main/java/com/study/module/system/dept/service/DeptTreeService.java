package com.study.module.system.dept.service;

import com.study.module.system.dept.dto.response.DeptTreeResp;

import java.util.List;

/**
 * 组织机构
 */
public interface DeptTreeService {

    /**
     * 部门树形列表
     */
    List<DeptTreeResp> deptTreeData();
}