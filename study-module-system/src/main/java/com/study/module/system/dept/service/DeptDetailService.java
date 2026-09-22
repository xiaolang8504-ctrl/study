package com.study.module.system.dept.service;

import com.study.module.system.dept.dto.response.DeptDetailResp;

/**
 * 组织机构
 */
public interface DeptDetailService {

    /**
     * 部门ID获取部门信息
     */
    DeptDetailResp getDeptDetailById(Integer id);
}