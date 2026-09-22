package com.study.api.provider;

import com.study.api.dto.response.DeptData;
import com.study.api.dto.response.DeptPathData;
import com.study.common.core.exception.LogicException;

/**
 * 部门服务
 */
public interface DeptProvider {

    /**
     * 校验部门路径
     */
    DeptPathData checkDeptPathData(Integer deptId) throws LogicException;

    /**
     * 获取指定ID的部门信息
     */
    DeptData checkDept(Integer deptId) throws LogicException;
}
