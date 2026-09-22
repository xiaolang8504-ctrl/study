package com.study.module.system.dept.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.base.Joiner;
import com.study.module.system.dept.entity.Dept;
import com.study.module.system.dept.mapper.DeptMapper;
import com.study.module.system.dept.service.DeptService;
import com.study.api.dto.response.DeptPathData;
import com.study.common.core.enums.ErrorCodeConstants;
import com.study.common.core.exception.LogicException;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 组织机构实现类
 */
@Service
public class DeptServiceImpl extends ServiceImpl<DeptMapper, Dept> implements DeptService {

    /**
     * 部门ID获取所有的子集的id集
     */
    @Override
    public List<Integer> getDeptSonIdsById(Integer id) {
        LambdaQueryWrapper<Dept> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.apply("FIND_IN_SET ('" + id + "',dept_path)");
        return this.list(queryWrapper).stream().map(Dept::getId).collect(Collectors.toList());
    }

    /**
     * 部门ID获取部门
     */
    @Override
    public Dept checkDeptById(Integer id) {
        Dept dept = this.getOne(new LambdaQueryWrapper<Dept>().eq(Dept::getId, id));
        if (dept == null){
            throw new LogicException(ErrorCodeConstants.DEPT_NOT_EXIST);
        }
        return dept;
    }

    /**
     * 判断是否有子
     */
    @Override
    public Boolean checkIsSonDeptById(Integer id) {
        if (this.getOne(new LambdaQueryWrapper<Dept>().eq(Dept::getPid, id),false) !=null){
            return true;
        }
        return false;
    }

    /**
     * 检测同级部门里指定ID和部门名是否存在
     */
    @Override
    public boolean checkDeptNameById(Integer pid, Integer id, String deptName) {
        if ((this.getOne(new LambdaQueryWrapper<Dept>().eq(Dept::getDeptName, deptName).ne(Dept::getId,id).eq(Dept::getPid,pid).last("limit 1")) != null)){
            return true;
        }
        return false;
    }

    /**
     * 更新指定部门的路径
     */
    @Override
    public void updateDeptPidById(Dept dept) {
        Dept dept1 = new Dept();
        List<Integer> idList = getAllParents(dept);
        Collections.reverse(idList);
        idList.add(dept.getId());
        dept1.setId(dept.getId());
        dept1.setDeptPath(Joiner.on(",").join(idList));
        this.updateById(dept1);
    }

    /**
     * 检测同级部门里部门名是否存在
     */
    @Override
    public boolean checkDeptNameByPid(Integer pid, String deptName) {
        if ((this.getOne(new LambdaQueryWrapper<Dept>().eq(Dept::getDeptName, deptName).eq(Dept::getPid,pid).last("limit 1")) != null)){
            return true;
        }
        return false;
    }

    /**
     * 递归获取父路径
     */
    public List<Integer> getAllParents(Dept dept) {
        List<Integer> parents = new ArrayList<>();
        Dept parent = getDeptById(dept.getPid());
        if (parent != null) {
            parents.add(parent.getId());
            parents.addAll(getAllParents(parent));
        }
        return parents;
    }

    /**
     * 查询指定ID的部门
     */
    @Override
    public Dept getDeptById(Integer id) {
        return this.getOne(new LambdaQueryWrapper<Dept>().eq(Dept::getId, id));
    }

    /**
     * 部门ID获取部门ID路径和名称路径
     */
    @Override
    public DeptPathData checkDeptPathData(Integer deptId) {
        DeptPathData deptPathData = new DeptPathData();
        //获取ID的路径
        Dept dept = getDeptById(deptId);
        List<Integer> idList = getAllParentsPath(dept);
        Collections.reverse(idList);
        idList.add(deptId);
        deptPathData.setDeptPath(Joiner.on(",").join(idList));
        //获取名称的路径
        List<String> nameList = getAllParentsPathName(dept);
        Collections.reverse(nameList);
        if(dept.getDeptName() != null){
            nameList.add(dept.getDeptName());
        }
        if(nameList != null && nameList.size() > 0){
            deptPathData.setDeptPathName(Joiner.on("/").join(nameList));
        }
        return deptPathData;
    }

    /**
     * 部门信息递归处理
     */
    public List<Integer> getAllParentsPath(Dept dept) {
        List<Integer> parents = new ArrayList<>();
        Dept parent = getDeptById(dept.getPid());
        if (parent != null && parent.getId() != null) {
            parents.add(parent.getId());
            parents.addAll(getAllParentsPath(parent));
        }
        return parents;
    }

    /**
     * 部门信息递归处理
     */
    public List<String> getAllParentsPathName(Dept dept) {
        List<String> parents = new ArrayList<>();
        Dept parent = getDeptById(dept.getPid());
        if (parent != null && parent.getId() != null) {
            parents.add(parent.getDeptName());
            parents.addAll(getAllParentsPathName(parent));
        }
        return parents;
    }
}