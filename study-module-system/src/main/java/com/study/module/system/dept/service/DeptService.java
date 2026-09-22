package com.study.module.system.dept.service;

import com.study.module.system.dept.entity.Dept;
import com.study.api.dto.response.DeptPathData;

import java.util.List;

/**
 * 组织机构
 */
public interface DeptService {

    /**
     * 部门ID获取所有的子集的id集
     */
    List<Integer> getDeptSonIdsById(Integer id);

    /**
     * 部门ID获取部门
     */
    Dept checkDeptById(Integer id);

    /**
     * 判断是否有子
     */
    Boolean checkIsSonDeptById(Integer id);

    /**
     * 检测同级部门里指定ID和部门名是否存在
     */
    boolean checkDeptNameById(Integer pid, Integer id, String deptName);

    /**
     * 更新指定部门的路径
     */
    void updateDeptPidById(Dept dept);

    /**
     * 检测同级部门里部门名是否存在
     */
    boolean checkDeptNameByPid(Integer pid, String deptName);

    /**
     * 部门ID获取部门
     */
    Dept getDeptById(Integer id);

    /**
     * 部门ID获取部门ID路径和名称路径
     */
    DeptPathData checkDeptPathData(Integer deptId);
}