package com.study.module.system.dept.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.module.system.dept.entity.Dept;
import com.study.module.system.dept.mapper.DeptMapper;
import com.study.module.system.dept.service.DeptEnableService;
import com.study.module.system.dept.service.DeptService;
import com.study.common.core.constants.Enable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

/**
 * 组织机构实现类
 */
@Service
public class DeptEnableServiceImpl extends ServiceImpl<DeptMapper, Dept> implements DeptEnableService {

    @Autowired
    DeptService deptService;

    /**
     * 启禁用部门
     */
    @Override
    public void enableDept(Integer id) {
        Dept dept = deptService.checkDeptById(id);
        Dept dept1 = new Dept();
        if(dept.getStatus()== Enable.DISABLE){
            dept1.setStatus(Enable.ENABLE);
        }else{
            dept1.setStatus(Enable.DISABLE);
        }
        dept1.setId(id);
        dept1.setUpdateTime(LocalDateTime.now());
        this.updateById(dept1);
    }
}