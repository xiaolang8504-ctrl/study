package com.study.module.system.menu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.study.module.system.menu.entity.Menu;
import com.study.module.system.menu.dto.request.UpdateMenuReq;
import org.springframework.transaction.annotation.Transactional;

/**
 * 系统菜单表 服务类
 */
public interface MenuUpdateService extends IService<Menu> {

    /**
     * 修改菜单
     */
    @Transactional(rollbackFor = Exception.class)
    void updateMenu(UpdateMenuReq request);

    /**
     * 更新排序字段值
     */
    void updateMenuSort(Integer id);
}