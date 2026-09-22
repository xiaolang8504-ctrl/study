package com.study.module.system.role.convert;

import com.study.module.system.role.dto.request.CreateRoleReq;
import com.study.module.system.role.dto.request.UpdateRoleReq;
import com.study.module.system.role.dto.response.RoleDetailResp;
import com.study.module.system.role.dto.response.RoleListResp;
import com.study.module.system.role.entity.Role;
import com.study.api.dto.response.RoleData;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import java.util.List;

/**
 * 系统角色 MapStruct 接口
 */
@Mapper
public interface RoleConvert {

    RoleConvert INSTANCE=Mappers.getMapper(RoleConvert.class);

    /**
     * 角色列表数据处理
     */
    List<RoleListResp> toRoleList(List<Role> role);

    /**
     * 角色入库数据处理
     */
    Role toRole(CreateRoleReq createRoleReq);

    /**
     * 角色入库数据处理
     */
    Role toRole(UpdateRoleReq updateRoleReq);

    /**
     * 角色详情数据处理
     */
    RoleDetailResp toRoleDetail(Role role);

    /**
     * 角色列表数据处理
     */
    List<RoleData> toRoleData(List<Role> role);
}