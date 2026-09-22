package com.study.module.system.dept.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.module.system.dept.convert.DeptConvert;
import com.study.module.system.dept.dto.response.DeptDetailResp;
import com.study.module.system.dept.entity.Dept;
import com.study.module.system.dept.mapper.DeptMapper;
import com.study.module.system.dept.service.DeptDetailService;
import com.study.module.system.dept.service.DeptService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 组织机构实现类
 */
@Slf4j
@Service
public class DeptDetailServiceImpl extends ServiceImpl<DeptMapper, Dept> implements DeptDetailService {

    @Autowired
    DeptService deptService;

    /**
     * 部门ID获取部门信息
     */
    @Override
    public DeptDetailResp getDeptDetailById(Integer id) {
        return DeptConvert.INSTANCE.toDeptDetailResp(deptService.checkDeptById(id));
    }
}