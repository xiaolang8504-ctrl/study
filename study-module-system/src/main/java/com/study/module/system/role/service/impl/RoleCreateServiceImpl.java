package com.study.module.system.role.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.common.core.enums.ErrorCodeConstants;
import com.study.common.core.exception.LogicException;
import com.study.module.system.role.dto.request.CreateRoleReq;
import com.study.module.system.role.entity.Role;
import com.study.module.system.role.convert.RoleConvert;
import com.study.module.system.role.mapper.RoleMapper;
import com.study.module.system.role.service.RoleCreateService;
import com.study.module.system.role.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

/**
 * 系统角色表 服务实现类
 */
@SuppressWarnings({"Duplicates", "SpringJavaInjectionPointsAutowiringInspection"})
@Service
public class RoleCreateServiceImpl extends ServiceImpl<RoleMapper, Role> implements RoleCreateService {

    @Autowired
    RoleService roleService;

    /**
     * 添加角色
     */
    @Override
    public void createRole(CreateRoleReq request) {
        // 参数逻辑校验
        createValidate(request);

        //数据入库
        createRoleData(request);
    }

    /**
     * 参数逻辑校验
     */
    private void createValidate(CreateRoleReq request){
        if (roleService.checkRoleName(request.getRoleName())){
            throw new LogicException(ErrorCodeConstants.ROLE_NAME_EXIST);
        }
    }

    /**
     * 组合数据
     */
    private void createRoleData(CreateRoleReq request){
        Role role = RoleConvert.INSTANCE.toRole(request);
        role.setCreateTime(LocalDateTime.now());
        if (!this.save(role)){
            throw new LogicException(ErrorCodeConstants.CREATE_ROLE_FAIL);
        }
    }
}