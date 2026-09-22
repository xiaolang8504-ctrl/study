package com.study.module.system.menu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.study.module.system.menu.dto.response.MenuListResp;
import com.study.module.system.menu.entity.Menu;

import java.util.List;

/**
 * 系统菜单表 服务类
 */
public interface MenuListService extends IService<Menu> {

    /**
     * 菜单列表带父ID筛选
     */
    List<MenuListResp> menuList(Integer pid);
}