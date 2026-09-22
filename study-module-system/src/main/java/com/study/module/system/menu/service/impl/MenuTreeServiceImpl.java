package com.study.module.system.menu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.module.system.menu.convert.MenuConvert;
import com.study.module.system.menu.dto.response.MenuTreeResp;
import com.study.module.system.menu.entity.Menu;
import com.study.module.system.menu.mapper.MenuMapper;
import com.study.module.system.menu.service.MenuResourceService;
import com.study.module.system.menu.service.MenuTreeService;
import com.study.module.system.user.constants.Constant;
import com.study.module.system.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 系统菜单表 服务实现类
 */
@SuppressWarnings({"Duplicates", "SpringJavaInjectionPointsAutowiringInspection"})
@Service
public class MenuTreeServiceImpl extends ServiceImpl<MenuMapper, Menu> implements MenuTreeService {

    @Autowired
    MenuResourceService menuResourceService;

    @Autowired
    UserService userService;

    /**
     * 菜单树形结构
     */
    @Override
    public List<MenuTreeResp> menuTreeData() {
        LambdaQueryWrapper<Menu> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(Menu::getId, Menu::getCode, Menu::getMenuName, Menu::getPid, Menu::getSort,
                        Menu::getResourceIds, Menu::getResourceLevel)
                .orderByAsc(Menu::getSort);
        List<MenuTreeResp> list = MenuConvert.INSTANCE.toMenuTreeResp(this.list(queryWrapper));
        Map<Integer, List<MenuTreeResp>> nodeListMap = list.stream().collect(Collectors.groupingBy(MenuTreeResp::getPid));
        return getChildren(nodeListMap, 0);
    }

    /**
     * 当前用户菜单树形结构
     */
    @Override
    public List<MenuTreeResp> currentMenuTree() {
        List<MenuTreeResp> menuTree = menuTreeData();
        Set<String> menuCodes = userService.getCurrentUserInfo().getMenu();
        if (menuCodes != null && menuCodes.contains(Constant.ADMIN)) {
            return menuTree;
        }
        return filterMenuTree(menuTree, menuCodes);
    }

    /**
     * 递归处理
     */
    private List<MenuTreeResp> getChildren(Map<Integer, List<MenuTreeResp>> nodeListMap, Integer parentId) {
        List<MenuTreeResp> nodeList = nodeListMap.getOrDefault(parentId, new ArrayList<>());
        return nodeList.stream().peek(menuTreeResp -> {
            menuTreeResp.setChildren(getChildren(nodeListMap, menuTreeResp.getId()));
            menuTreeResp.setIsSon(menuTreeResp.getChildren().size() > 0);
        }).collect(Collectors.toList());
    }

    /**
     * 保留当前用户拥有的菜单，子菜单可见时同时保留父菜单
     */
    private List<MenuTreeResp> filterMenuTree(List<MenuTreeResp> menuTree, Set<String> menuCodes) {
        if (menuCodes == null || menuCodes.isEmpty()) {
            return new ArrayList<>();
        }
        return menuTree.stream().filter(menu -> {
            List<MenuTreeResp> children = filterMenuTree(menu.getChildren(), menuCodes);
            menu.setChildren(children);
            menu.setIsSon(!children.isEmpty());
            return menuCodes.contains(menu.getCode()) || !children.isEmpty();
        }).collect(Collectors.toList());
    }
}
