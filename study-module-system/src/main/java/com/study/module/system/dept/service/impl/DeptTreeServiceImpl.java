package com.study.module.system.dept.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.module.system.dept.convert.DeptConvert;
import com.study.module.system.dept.dto.response.DeptTreeResp;
import com.study.module.system.dept.entity.Dept;
import com.study.module.system.dept.mapper.DeptMapper;
import com.study.module.system.dept.service.DeptTreeService;
import com.study.common.core.constants.Enable;
import com.study.module.system.dept.service.DeptListService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 组织机构实现类
 */
@Slf4j
@Service
public class DeptTreeServiceImpl extends ServiceImpl<DeptMapper, Dept> implements DeptTreeService {

    @Autowired
    DeptListService deptListService;

    /**
     * 部门树形列表
     */
    @Override
    public List<DeptTreeResp> deptTreeData() {
        LambdaQueryWrapper<Dept> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(Dept::getId,Dept::getDeptName,Dept::getPid,Dept::getDeptPath);
        queryWrapper.eq(Dept::getStatus, Enable.ENABLE);
        List<DeptTreeResp> list = DeptConvert.INSTANCE.toDeptTree(this.list(queryWrapper));
        Map<Integer, List<DeptTreeResp>> nodeListMap = list.stream().collect(Collectors.groupingBy(DeptTreeResp::getPid));
        return getChildren(nodeListMap, 0);
    }

    /**
     * 递归处理
     */
    private List<DeptTreeResp> getChildren(Map<Integer, List<DeptTreeResp>> nodeListMap, Integer pid) {
        List<DeptTreeResp> nodeList = nodeListMap.getOrDefault(pid, new ArrayList<>());
        return nodeList.stream().peek(deptTreeResp -> {
            deptTreeResp.setChildren(getChildren(nodeListMap, deptTreeResp.getId()));
        }).collect(Collectors.toList());
    }
}