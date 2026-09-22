package com.study.module.system.menu.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.module.system.menu.convert.MenuConvert;
import com.study.module.system.menu.entity.Menu;
import com.study.module.system.menu.mapper.MenuMapper;
import com.study.module.system.menu.service.MenuService;
import com.study.module.system.menu.service.MenuUpdateService;
import com.study.common.core.enums.ErrorCodeConstants;
import com.study.common.core.exception.LogicException;
import com.study.module.system.menu.dto.request.UpdateMenuReq;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 系统菜单表 服务实现类
 */
@SuppressWarnings({"Duplicates", "SpringJavaInjectionPointsAutowiringInspection"})
@Service
public class MenuUpdateServiceImpl extends ServiceImpl<MenuMapper, Menu> implements MenuUpdateService {

    @Autowired
    MenuService menuService;

    /**
     * 修改菜单
     */
    @Override
    public void updateMenu(UpdateMenuReq request) {
        // 参数逻辑校验
        updateValidate(request);

        //数据入库
        updateMenuData(request);
    }

    /**
     * 更新排序字段值
     */
    @Override
    public void updateMenuSort(Integer id) {
        Menu menu = new Menu();
        menu.setSort(id);
        menu.setId(id);
        this.updateById(menu);
    }

    /**
     * 参数校验
     */
    private void updateValidate(UpdateMenuReq request){
        menuService.checkMenuById(request.getId());
        if (menuService.checkMenuCodeById(request.getId(),request.getCode())){
            throw new LogicException(ErrorCodeConstants.MENU_CODE_EXIST);
        }
    }

    /**
     * 组合数据
     */
    private void updateMenuData(UpdateMenuReq request){
        Menu menu = MenuConvert.INSTANCE.toMenu(request);
        if (!this.updateById(menu)){
            throw new LogicException(ErrorCodeConstants.UPDATE_MENU_FAIL);
        }
    }
}
