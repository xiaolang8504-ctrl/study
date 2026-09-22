package com.study.module.system.menu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.study.module.system.menu.dto.response.MenuIdResp;
import com.study.module.system.menu.entity.Menu;
import com.study.module.system.menu.dto.request.CreateMenuReq;
import org.springframework.transaction.annotation.Transactional;

/**
 * 系统菜单表 服务类
 */
public interface MenuCreateService extends IService<Menu> {

    /**
     * 添加菜单
     */
    @Transactional(rollbackFor = Exception.class)
    MenuIdResp createMenu(CreateMenuReq request);
}