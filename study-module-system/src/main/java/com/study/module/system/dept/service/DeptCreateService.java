package com.study.module.system.dept.service;

import com.study.module.system.dept.dto.request.CreateDeptReq;
import org.springframework.transaction.annotation.Transactional;

/**
 * 组织机构
 */
public interface DeptCreateService {

    /**
     * 创建部门
     */
    @Transactional(rollbackFor = Exception.class)
    void createDept(CreateDeptReq request);
}