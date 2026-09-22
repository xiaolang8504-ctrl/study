package com.study.module.system.dept.service;

import com.study.module.system.dept.dto.request.UpdateDeptReq;
import org.springframework.transaction.annotation.Transactional;

/**
 * 组织机构
 */
public interface DeptUpdateService {

    /**
     * 更新部门
     */
    @Transactional(rollbackFor = Exception.class)
    void updateDept(UpdateDeptReq request);
}