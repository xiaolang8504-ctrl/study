package com.study.module.system.menu.convert;

import com.study.module.system.menu.dto.response.MenuDetailResp;
import com.study.module.system.menu.dto.response.MenuListResp;
import com.study.module.system.menu.dto.response.MenuTreeResp;
import com.study.module.system.menu.entity.Menu;
import com.study.module.system.menu.dto.request.CreateMenuReq;
import com.study.module.system.menu.dto.request.UpdateMenuReq;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import java.util.List;

/**
 * 系统菜单 MapStruct 接口
 */
@Mapper
public interface MenuConvert {

    MenuConvert INSTANCE=Mappers.getMapper(MenuConvert.class);

    /**
     * 菜单列表数据处理
     */
    MenuListResp toMenuList(Menu menu);

    /**
     * 菜单入库数据处理
     */
    Menu toMenu(CreateMenuReq createMenuReq);

    /**
     * 菜单入库数据处理
     */
    Menu toMenu(UpdateMenuReq updateMenuReq);

    /**
     * 菜单详情数据处理
     */
    MenuDetailResp toMenuDetail(Menu menu);

    /**
     * 菜单树形数据处理
     */
    MenuTreeResp toMenuTree(Menu menu);

    /**
     * 菜单列表数据处理
     */
    List<MenuTreeResp> toMenuTreeResp(List<Menu> list);
}