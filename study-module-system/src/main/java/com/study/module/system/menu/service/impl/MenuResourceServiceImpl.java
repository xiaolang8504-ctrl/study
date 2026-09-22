package com.study.module.system.menu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.module.system.menu.entity.MenuResource;
import com.study.module.system.menu.mapper.MenuResourceMapper;
import com.study.module.system.menu.service.MenuResourceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 菜单资源 中间表 服务实现类
 */
@SuppressWarnings({"Duplicates", "SpringJavaInjectionPointsAutowiringInspection"})
@Service
@Slf4j
public class MenuResourceServiceImpl extends ServiceImpl<MenuResourceMapper, MenuResource> implements MenuResourceService {

    /**
     * 批量数据到菜单资源关联
     */
    @Override
    public boolean batchSaveMenuResource(Integer menuId, List<Integer> resourceIds, String resourceLevel) {
        List<MenuResource> list = new ArrayList<>();
        for (Integer resourceId : resourceIds) {
            MenuResource menuResource = new MenuResource();
            menuResource.setResourceLevel(resourceLevel);
            menuResource.setMenuId(menuId);
            menuResource.setResourceId(resourceId);
            menuResource.setCreateTime(LocalDateTime.now());
            list.add(menuResource);
        }
        return this.saveBatch(list);
    }

    /**
     * 删除菜单对应的资源
     */
    @Override
    public boolean deleteMenuResourceByMenuId(Integer menuId) {
        LambdaQueryWrapper<MenuResource> queryWrapper = new LambdaQueryWrapper<>();
        return this.remove(queryWrapper.eq(MenuResource::getMenuId,menuId));
    }

    /**
     * 删除资源对应的菜单关联。
     */
    @Override
    public boolean deleteMenuResourceByResourceId(Integer resourceId) {
        return this.remove(new LambdaQueryWrapper<MenuResource>().eq(MenuResource::getResourceId, resourceId));
    }

    /**
     * 获取指定api的ID对应的菜单ID
     */
    @Override
    public List<Integer> getMenuResourceByResourceId(Integer resourceId) {
        LambdaQueryWrapper<MenuResource> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MenuResource::getResourceId,resourceId);
        return this.list(queryWrapper).stream().map(MenuResource::getMenuId).collect(Collectors.toList());
    }
}
