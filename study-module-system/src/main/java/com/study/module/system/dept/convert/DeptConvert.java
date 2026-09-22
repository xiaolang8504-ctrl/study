package com.study.module.system.dept.convert;

import com.study.module.system.dept.dto.request.CreateDeptReq;
import com.study.module.system.dept.dto.request.UpdateDeptReq;
import com.study.module.system.dept.dto.response.DeptDetailResp;
import com.study.module.system.dept.dto.response.DeptListResp;
import com.study.module.system.dept.dto.response.DeptResp;
import com.study.module.system.dept.dto.response.DeptTreeResp;
import com.study.module.system.dept.entity.Dept;
import com.study.api.dto.response.DeptData;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import java.util.List;

/**
 * 系统组织架构 MapStruct 接口
 */
@Mapper
public interface DeptConvert {

    DeptConvert INSTANCE=Mappers.getMapper(DeptConvert.class);

    /**
     * 部门列表数据处理
     */
    DeptListResp toDeptList(Dept dept);

    /**
     * 部门列表数据处理
     */
    List<DeptTreeResp> toDeptTree(List<Dept> list);

    /**
     * 部门列表数据处理
     */
    List<DeptResp> toDept(List<DeptDetailResp> list);

    /**
     * 部门详情数据处理
     */
    DeptResp toDept(DeptDetailResp deptDetailResp);

    /**
     * 部门详情数据处理
     */
    DeptDetailResp toDeptDetailResp(Dept dept);

    /**
     * 部门入库处理
     */
    Dept toDept(CreateDeptReq createDeptReq);

    /**
     * 部门入库处理
     */
    Dept toDept(UpdateDeptReq updateDeptReq);

    /**
     * 部门处理
     */
    DeptData toDeptData(Dept dept);
}