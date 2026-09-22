package com.study.module.system.role.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.module.system.role.service.RoleUpdateService;
import com.study.common.core.enums.ErrorCodeConstants;
import com.study.common.core.exception.LogicException;
import com.study.module.system.role.dto.request.UpdateRoleReq;
import com.study.module.system.role.entity.Role;
import com.study.module.system.role.convert.RoleConvert;
import com.study.module.system.role.mapper.RoleMapper;
import com.study.module.system.role.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

/**
 * 系统角色表 服务实现类
 */
@SuppressWarnings({"Duplicates", "SpringJavaInjectionPointsAutowiringInspection"})
@Service
public class RoleUpdateServiceImpl extends ServiceImpl<RoleMapper, Role> implements RoleUpdateService {

    @Autowired
    RoleService roleService;

    /**
     * 修改角色
     */
    @Override
    public void updateRole(UpdateRoleReq request) {
        // 参数逻辑校验
        updateValidate(request);

        //数据入库
        updateRoleData(request);
    }

    /**
     * 参数逻辑校验
     */
    private void updateValidate(UpdateRoleReq request){
        roleService.checkRoleById(request.getId());
        if (roleService.checkRoleNameById(request.getId(),request.getRoleName())){
            throw new LogicException(ErrorCodeConstants.ROLE_NAME_EXIST);
        }
    }

    /**
     * 组合数据
     */
    private void updateRoleData(UpdateRoleReq request){
        Role role = RoleConvert.INSTANCE.toRole(request);
        role.setUpdateTime(LocalDateTime.now());
        if (!this.updateById(role)){
            throw new LogicException(ErrorCodeConstants.UPDATE_ROLE_FAIL);
        }
    }
}