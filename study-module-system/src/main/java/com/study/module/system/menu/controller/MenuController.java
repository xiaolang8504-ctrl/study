package com.study.module.system.menu.controller;

import com.study.module.system.menu.dto.response.MenuDetailResp;
import com.study.module.system.menu.dto.response.MenuIdResp;
import com.study.module.system.menu.dto.response.MenuListResp;
import com.study.module.system.menu.dto.response.MenuTreeResp;
import com.study.module.system.menu.entity.Menu;
import com.study.module.system.menu.service.*;
import com.study.common.core.domain.Result;
import com.study.common.core.utils.ResultUtils;
import com.study.module.system.menu.dto.request.CreateMenuReq;
import com.study.module.system.menu.dto.request.MenuCodeReq;
import com.study.module.system.menu.dto.request.MenuIdReq;
import com.study.module.system.menu.dto.request.UpdateMenuReq;
import com.study.module.system.menu.dto.request.UpdateMenuResourceReq;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.util.List;

/**
 * 菜单前端控制器
 */
@RestController
@RequestMapping("/api/menu")
public class MenuController {

    @Autowired
    MenuCreateService menuCreateService;

    @Autowired
    MenuDeleteService menuDeleteService;

    @Autowired
    MenuDetailService menuDetailService;

    @Autowired
    MenuListService menuListService;

    @Autowired
    MenuTreeService menuTreeService;

    @Autowired
    MenuUpdateService menuUpdateService;

    @Autowired
    MenuService menuService;

    @Autowired
    MenuResourceUpdateService menuResourceUpdateService;

    /**
     * 菜单列表带父ID筛选
     */
    @PreAuthorize("hasAuthority('system:menu:menuList')")
    @GetMapping("/menuList")
    public Result<List<MenuListResp>> menuList(@RequestParam(defaultValue = "0") Integer pid) {
        return ResultUtils.success(menuListService.menuList(pid));
    }

    /**
     * 添加菜单
     */
    @PreAuthorize("hasAuthority('system:menu:createMenu')")
    @PostMapping("/createMenu")
    public Result<MenuIdResp> createMenu(@RequestBody @Validated CreateMenuReq request) {
        return ResultUtils.success(menuCreateService.createMenu(request));
    }

    /**
     * 根据前端menuCode.json生成菜单
     */
    @PreAuthorize("hasAuthority('system:menu:createMenCode')")
    @PostMapping("/createMenCode")
    public Result<List<Menu>> createMenCode(@RequestBody @Valid List<MenuCodeReq> request) {
        return ResultUtils.success(menuService.createMenCode(request));
    }

    /**
     * 更新菜单
     */
    @PreAuthorize("hasAuthority('system:menu:updateMenu')")
    @PostMapping("/updateMenu")
    public Result<Void> updateMenu(@RequestBody @Validated UpdateMenuReq request) {
        menuUpdateService.updateMenu(request);
        return ResultUtils.success();
    }

    /**
     * 更新菜单关联API
     */
    @PreAuthorize("hasAuthority('system:menu:updateMenuResource')")
    @PostMapping("/updateMenuResource")
    public Result<Void> updateMenuResource(@RequestBody @Validated UpdateMenuResourceReq request) {
        menuResourceUpdateService.updateMenuResource(request);
        return ResultUtils.success();
    }

    /**
     * 删除菜单
     */
    @PreAuthorize("hasAuthority('system:menu:deleteMenu')")
    @PostMapping("/deleteMenu")
    public Result<Void> deleteMenu(@RequestBody @Validated MenuIdReq request) {
        menuDeleteService.deleteMenu(request.getId());
        return ResultUtils.success();
    }

    /**
     * 菜单详情
     */
    @PreAuthorize("hasAuthority('system:menu:menuDetail')")
    @GetMapping("/menuDetail")
    public Result<MenuDetailResp> menuDetail(@Validated MenuIdReq request) {
        return ResultUtils.success(menuDetailService.menuDetail(request.getId()));
    }

    /**
     * 菜单树形结构
     */
    @PreAuthorize("hasAuthority('system:menu:menuTreeData')")
    @GetMapping("/menuTreeData")
    public Result<List<MenuTreeResp>> menuTreeData() {
        return ResultUtils.success(menuTreeService.menuTreeData());
    }

    /**
     * 当前用户菜单树形结构
     */
    @GetMapping("/currentMenuTree")
    public Result<List<MenuTreeResp>> currentMenuTree() {
        return ResultUtils.success(menuTreeService.currentMenuTree());
    }

}
