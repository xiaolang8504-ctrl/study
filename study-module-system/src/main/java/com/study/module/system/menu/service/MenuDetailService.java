package com.study.module.system.menu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.study.module.system.menu.dto.response.MenuDetailResp;
import com.study.module.system.menu.entity.Menu;

/**
 * 系统菜单表 服务类
 */
public interface MenuDetailService extends IService<Menu> {

    /**
     * ID获取菜单详情
     */
    MenuDetailResp menuDetail(Integer id);
}