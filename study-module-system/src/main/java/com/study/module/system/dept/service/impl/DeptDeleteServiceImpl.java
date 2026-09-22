package com.study.module.system.dept.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.module.system.dept.entity.Dept;
import com.study.module.system.dept.mapper.DeptMapper;
import com.study.module.system.dept.service.DeptDeleteService;
import com.study.module.system.dept.service.DeptService;
import com.study.common.core.enums.ErrorCodeConstants;
import com.study.common.core.exception.LogicException;
import com.study.module.system.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 组织机构实现类
 */
@Slf4j
@Service
public class DeptDeleteServiceImpl extends ServiceImpl<DeptMapper, Dept> implements DeptDeleteService {

    @Autowired
    DeptService deptService;

    @Autowired
    UserService userService;

    /**
     * 删除部门
     */
    @Override
    public void deleteDept(Integer id) {
        // 参数逻辑校验
        deleteValidate(id);

        //数据入库
        deleteDeptData(id);
    }

    /**
     * 参数校验
     */
    private void deleteValidate(Integer id){
        deptService.checkDeptById(id);
        if (deptService.checkIsSonDeptById(id)){
            throw new LogicException(ErrorCodeConstants.EXIST_SUB_DEPT);
        }
        if (userService.checkUserByDeptId(id) != null){
            throw new LogicException(ErrorCodeConstants.DEPT_USER_EXIST);
        }
    }

    /**
     * 组合数据
     */
    private void deleteDeptData(Integer id){
        if (!this.removeById(id)){
            throw new LogicException(ErrorCodeConstants.DELETE_DEPT_FAIL);
        }
    }
}