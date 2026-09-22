package com.study.module.system.menu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.study.module.system.menu.entity.Menu;
import org.springframework.transaction.annotation.Transactional;
/**
 * 系统菜单表 服务类
 */
public interface MenuDeleteService extends IService<Menu> {

    /**
     * 删除菜单
     */
    @Transactional(rollbackFor = Exception.class)
    void deleteMenu(Integer id);
}