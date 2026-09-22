package com.study.module.system.menu.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.module.system.menu.convert.MenuConvert;
import com.study.module.system.menu.dto.response.MenuIdResp;
import com.study.module.system.menu.entity.Menu;
import com.study.module.system.menu.mapper.MenuMapper;
import com.study.module.system.menu.service.MenuCreateService;
import com.study.module.system.menu.service.MenuService;
import com.study.module.system.menu.service.MenuUpdateService;
import com.study.common.core.enums.ErrorCodeConstants;
import com.study.common.core.exception.LogicException;
import com.study.module.system.menu.dto.request.CreateMenuReq;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

/**
 * 系统菜单表 服务实现类
 */
@SuppressWarnings({"Duplicates", "SpringJavaInjectionPointsAutowiringInspection"})
@Service
public class MenuCreateServiceImpl extends ServiceImpl<MenuMapper, Menu> implements MenuCreateService {

    @Autowired
    MenuService menuService;

    @Autowired
    MenuUpdateService menuUpdateService;

    /**
     * 添加菜单
     */
    @Override
    public MenuIdResp createMenu(CreateMenuReq request) {
        // 参数逻辑校验
        createValidate(request);

        //数据入库
        Integer id = createMenuData(request);

        //返回新增ID值
        MenuIdResp idResp = new MenuIdResp();
        idResp.setId(id);
        return idResp;
    }

    /**
     * 参数校验
     */
    private void createValidate(CreateMenuReq request){
        if (menuService.checkMenuCode(request.getCode())){
            throw new LogicException(ErrorCodeConstants.MENU_CODE_EXIST);
        }
    }

    /**
     * 组合数据
     */
    private Integer createMenuData(CreateMenuReq request){
        //组合数据包
        Menu menu = MenuConvert.INSTANCE.toMenu(request);
        menu.setCreateTime(LocalDateTime.now());
        if (!this.save(menu)){
            throw new LogicException(ErrorCodeConstants.CREATE_MENU_FAIL);
        }
        //更新sort值
        menuUpdateService.updateMenuSort(menu.getId());
        return menu.getId();
    }
}
