package com.study.module.system.dept.service;

import com.study.module.system.dept.dto.response.DeptListResp;

import java.util.List;

/**
 * 组织机构
 */
public interface DeptListService {

    /**
     * 部门列表带父ID筛选
     */
    List<DeptListResp> deptListById(Integer id);
}