package com.study.module.system.dept.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.module.system.dept.convert.DeptConvert;
import com.study.module.system.dept.dto.request.CreateDeptReq;
import com.study.module.system.dept.entity.Dept;
import com.study.module.system.dept.mapper.DeptMapper;
import com.study.module.system.dept.service.DeptCreateService;
import com.study.module.system.dept.service.DeptService;
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
public class DeptCreateServiceImpl extends ServiceImpl<DeptMapper, Dept> implements DeptCreateService {

    @Autowired
    DeptService deptService;

    /**
     * 创建部门
     */
    @Override
    public void createDept(CreateDeptReq request) {
        // 参数逻辑校验
        createValidate(request);

        //数据入库
        createDeptData(request);
    }

    /**
     * 参数校验
     */
    private void createValidate(CreateDeptReq request){
        if (deptService.checkDeptNameByPid(request.getPid(),request.getDeptName())){
            throw new LogicException(ErrorCodeConstants.DEPT_NAME_EXIST);
        }
    }

    /**
     * 组合数据
     */
    private void createDeptData(CreateDeptReq request){
        //组合数据包
        Dept dept = DeptConvert.INSTANCE.toDept(request);
        dept.setCreateTime(LocalDateTime.now());
        if (!this.save(dept)){
            throw new LogicException(ErrorCodeConstants.CREATE_DEPT_FAIL);
        }
        deptService.updateDeptPidById(dept);
    }
}