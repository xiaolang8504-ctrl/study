package com.study.module.system.menu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.module.system.menu.convert.MenuConvert;
import com.study.module.system.menu.dto.response.MenuListResp;
import com.study.module.system.menu.entity.Menu;
import com.study.module.system.menu.mapper.MenuMapper;
import com.study.module.system.menu.service.MenuListService;
import com.study.module.system.menu.service.MenuService;
import com.study.common.core.constants.Delete;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 系统菜单表 服务实现类
 */
@SuppressWarnings({"Duplicates", "SpringJavaInjectionPointsAutowiringInspection"})
@Service
public class MenuListServiceImpl extends ServiceImpl<MenuMapper, Menu> implements MenuListService {

    @Autowired
    MenuService menuService;

    /**
     * 菜单列表带父ID筛选
     */
    @Override
    public List<MenuListResp> menuList(Integer pid) {
        List<MenuListResp> listRes = new ArrayList<>();
        this.list(new LambdaQueryWrapper<Menu>().eq(Menu::getPid,pid).orderByAsc(Menu::getSort)).stream().map(menu->{
            MenuListResp menuListResp = MenuConvert.INSTANCE.toMenuList(menu);
            //是否有子级
            menuListResp.setIsSon(menuService.checkSonByPid(menu.getId()) ? Delete.YES : Delete.NO);
            listRes.add(menuListResp);
            return menu;
        }).collect(Collectors.toList());
        return listRes;
    }
}