package com.study.module.system.menu.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.module.system.menu.convert.MenuConvert;
import com.study.module.system.menu.dto.response.MenuDetailResp;
import com.study.module.system.menu.entity.Menu;
import com.study.module.system.menu.mapper.MenuMapper;
import com.study.module.system.menu.service.MenuDetailService;
import com.study.module.system.menu.service.MenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 系统菜单表 服务实现类
 */
@SuppressWarnings({"Duplicates", "SpringJavaInjectionPointsAutowiringInspection"})
@Service
public class MenuDetailServiceImpl extends ServiceImpl<MenuMapper, Menu> implements MenuDetailService {

    @Autowired
    MenuService menuService;

    /**
     * ID获取菜单详情
     */
    @Override
    public MenuDetailResp menuDetail(Integer id) {
        MenuDetailResp menuDetailResp = MenuConvert.INSTANCE.toMenuDetail(menuService.checkMenuById(id));
        menuDetailResp.setIsSon(menuService.checkSonByPid(id) ? 1 : 0);
        return menuDetailResp;
    }
}