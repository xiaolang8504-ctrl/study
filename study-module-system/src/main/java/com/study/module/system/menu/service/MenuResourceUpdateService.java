package com.study.module.system.menu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.study.module.system.menu.dto.request.UpdateMenuResourceReq;
import com.study.module.system.menu.entity.Menu;

/**
 * 菜单关联API更新服务
 */
public interface MenuResourceUpdateService extends IService<Menu> {

    /**
     * 更新菜单关联API
     */
    void updateMenuResource(UpdateMenuResourceReq request);
}
