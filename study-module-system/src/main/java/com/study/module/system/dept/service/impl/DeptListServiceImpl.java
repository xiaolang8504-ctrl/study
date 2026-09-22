package com.study.module.system.dept.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.module.system.dept.convert.DeptConvert;
import com.study.module.system.dept.dto.response.DeptListResp;
import com.study.module.system.dept.entity.Dept;
import com.study.module.system.dept.mapper.DeptMapper;
import com.study.module.system.dept.service.DeptService;
import com.study.common.core.constants.Delete;
import com.study.module.system.dept.service.DeptListService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 组织机构实现类
 */
@Service
public class DeptListServiceImpl extends ServiceImpl<DeptMapper, Dept> implements DeptListService {

    @Autowired
    DeptService deptService;

    /**
     * 部门列表带父ID筛选
     */
    @Override
    public List<DeptListResp> deptListById(Integer id) {
        List<DeptListResp> listRes = new ArrayList<>();
        Integer pid = id == null ? 0 : id;
        this.list(new LambdaQueryWrapper<Dept>().eq(Dept::getPid, pid)).stream().map(dept->{
            DeptListResp deptListResp = DeptConvert.INSTANCE.toDeptList(dept);
            //是否有子级
            deptListResp.setIsSon(deptService.checkIsSonDeptById(dept.getId()) ? Delete.YES : Delete.NO);
            Dept dept1 = deptService.getDeptById(dept.getPid());
            deptListResp.setParentName(dept1 == null ? "" : dept1.getDeptName());
            listRes.add(deptListResp);
            return dept;
        }).collect(Collectors.toList());
        return listRes;
    }
}
