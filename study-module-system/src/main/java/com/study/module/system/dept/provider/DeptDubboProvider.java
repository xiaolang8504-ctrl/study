package com.study.module.system.dept.provider;

import com.study.module.system.dept.convert.DeptConvert;
import com.study.module.system.dept.service.DeptService;
import com.study.api.dto.response.DeptData;
import com.study.api.dto.response.DeptPathData;
import com.study.api.provider.DeptProvider;
import com.study.common.core.exception.LogicException;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 部门服务
 */
@DubboService
public class DeptDubboProvider implements DeptProvider {

    @Autowired
    DeptService deptService;

    @Override
    public DeptPathData checkDeptPathData(Integer deptId) throws LogicException {
        return deptService.checkDeptPathData(deptId);
    }

    /**
     * 获取指定ID的部门信息
     */
    @Override
    public DeptData checkDept(Integer deptId) throws LogicException {
        return DeptConvert.INSTANCE.toDeptData(deptService.checkDeptById(deptId));
    }
}
