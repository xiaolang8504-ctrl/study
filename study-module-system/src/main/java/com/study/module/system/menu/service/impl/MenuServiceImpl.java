package com.study.module.system.menu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.module.system.menu.dto.request.MenuCodeReq;
import com.study.module.system.menu.entity.Menu;
import com.study.module.system.menu.mapper.MenuMapper;
import com.study.module.system.menu.service.MenuService;
import com.study.common.core.enums.ErrorCodeConstants;
import com.study.common.core.exception.LogicException;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 系统菜单表 服务实现类
 */
@SuppressWarnings({"Duplicates", "SpringJavaInjectionPointsAutowiringInspection"})
@Service
public class MenuServiceImpl extends ServiceImpl<MenuMapper, Menu> implements MenuService {

    /**
     * ID检测菜单是否存在
     */
    @Override
    public Menu checkMenuById(Integer id){
        Menu menu = this.getOne(new LambdaQueryWrapper<Menu>().eq(Menu::getId, id));
        if (menu == null){
            throw new LogicException(ErrorCodeConstants.MENU_NOT_EXIST);
        }
        return menu;
    }

    /**
     * 检测指定ID是否存在子集
     */
    @Override
    public boolean checkSonByPid(Integer pid) {
        if (this.getOne(new LambdaQueryWrapper<Menu>().eq(Menu::getPid, pid),false) !=null){
            return true;
        }
        return false;
    }

    /**
     * 检测指定编码是否存在
     */
    @Override
    public boolean checkMenuCode(String code){
        if ((this.getOne(new LambdaQueryWrapper<Menu>().eq(Menu::getCode, code).last("limit 1")) != null)){
            return true;
        }
        return false;
    }

    /**
     * 检测指定ID和编码是否存在
     */
    @Override
    public boolean checkMenuCodeById(Integer id,String code) {
        if ((this.getOne(new LambdaQueryWrapper<Menu>().eq(Menu::getCode, code).ne(Menu::getId,id).last("limit 1")) != null)){
            return true;
        }
        return false;
    }

    /**
     * 根据菜单编号查询菜单编码
     */
    @Override
    public Map<Integer, String> getCodeByMenuIds(List<Integer> ids) {
        return ids.isEmpty() ? new HashMap<>() : this.list(new LambdaQueryWrapper<Menu>()
                .in(Menu::getId, ids)).stream().collect(Collectors.toMap(Menu::getId, Menu::getCode));
    }

    /**
     * 从前端menuCode.json生成菜单
     */
    @Override
    public List<Menu> createMenCode(List<MenuCodeReq> request) {
        Map<String, Menu> menuMap = this.list().stream()
                .filter(menu -> menu.getCode() != null && !menu.getCode().isEmpty())
                .collect(Collectors.toMap(Menu::getCode, Function.identity(), (first, second) -> first));
        List<Menu> menuList = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        for (MenuCodeReq item : request) {
            collectMenu(item, menuMap, menuList, now);
        }
        return menuList;
    }

    /**
     * 递归解析菜单节点
     */
    private void collectMenu(MenuCodeReq menuCode, Map<String, Menu> menuMap, List<Menu> menuList, LocalDateTime now) {
        addMenu(menuMap, menuList, menuCode, now);
        if (menuCode.getChildren() != null) {
            for (MenuCodeReq item : menuCode.getChildren()) {
                collectMenu(item, menuMap, menuList, now);
            }
        }
    }

    /**
     * 保存单个菜单，并加入本次新增菜单返回列表
     */
    private void addMenu(Map<String, Menu> menuMap, List<Menu> menuList, MenuCodeReq menuCode, LocalDateTime now) {
        String code = menuCode.getCode();
        if (menuMap.containsKey(code)) {
            return;
        }
        Integer pid = getPidByCode(code, menuMap);
        if (pid == null) {
            throw new LogicException(ErrorCodeConstants.CREATE_MENU_FAIL);
        }
        Menu menu = new Menu();
        menu.setMenuName(menuCode.getName());
        menu.setCode(code);
        menu.setLevel(menuCode.getLevel());
        menu.setPid(pid);
        menu.setResourceIds("");
        menu.setResourceLevel("");
        menu.setCreateTime(now);
        if (!this.save(menu)) {
            throw new LogicException(ErrorCodeConstants.CREATE_MENU_FAIL);
        }
        Menu updateMenu = new Menu();
        updateMenu.setId(menu.getId());
        updateMenu.setSort(menu.getId());
        if (!this.updateById(updateMenu)) {
            throw new LogicException(ErrorCodeConstants.CREATE_MENU_FAIL);
        }
        menu.setSort(menu.getId());
        menuList.add(menu);
        menuMap.put(code, menu);
    }

    /**
     * 根据菜单编码中最后一个横杠前的部分查询父级菜单ID
     */
    private Integer getPidByCode(String code, Map<String, Menu> menuMap) {
        int index = code.lastIndexOf("-");
        if (index < 0) {
            return 0;
        }
        String parentCode = code.substring(0, index);
        Menu parentMenu = menuMap.get(parentCode);
        return parentMenu == null ? null : parentMenu.getId();
    }
}
