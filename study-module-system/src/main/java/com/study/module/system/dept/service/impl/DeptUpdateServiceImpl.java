package com.study.module.system.dept.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.module.system.dept.convert.DeptConvert;
import com.study.module.system.dept.dto.request.UpdateDeptReq;
import com.study.module.system.dept.entity.Dept;
import com.study.module.system.dept.mapper.DeptMapper;
import com.study.module.system.dept.service.DeptService;
import com.study.module.system.dept.service.DeptUpdateService;
import com.study.common.core.enums.ErrorCodeConstants;
import com.study.common.core.exception.LogicException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

/**
 * 组织机构实现类
 */
@Slf4j
@Service
public class DeptUpdateServiceImpl extends ServiceImpl<DeptMapper, Dept> implements DeptUpdateService {

    @Autowired
    DeptService deptService;

    /**
     * 更新部门
     */
    @Override
    public void updateDept(UpdateDeptReq request) {

        // 参数逻辑校验
        updateValidate(request);

        //数据入库
        updateDeptData(request);
    }

    /**
     * 参数校验
     */
    private void updateValidate(UpdateDeptReq request){
        deptService.checkDeptById(request.getId());
        if (deptService.checkDeptNameById(request.getPid(),request.getId(),request.getDeptName())){
            throw new LogicException(ErrorCodeConstants.DEPT_NAME_EXIST);
        }
    }

    /**
     * 组合数据
     */
    private void updateDeptData(UpdateDeptReq request){
        Dept dept = DeptConvert.INSTANCE.toDept(request);
        dept.setUpdateTime(LocalDateTime.now());
        if (!this.updateById(dept)){
            throw new LogicException(ErrorCodeConstants.UPDATE_DEPT_FAIL);
        }
        deptService.updateDeptPidById(dept);
    }
}