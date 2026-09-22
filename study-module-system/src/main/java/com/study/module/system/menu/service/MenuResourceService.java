package com.study.module.system.menu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.study.module.system.menu.entity.MenuResource;

import java.util.List;

/**
 * 菜单资源 中间表 服务类
 */
public interface MenuResourceService extends IService<MenuResource> {

    /**
     * 批量数据到菜单资源关联
     */
    boolean batchSaveMenuResource(Integer menuId, List<Integer> resourceIds, String resourceLevel);

    /**
     * 删除菜单对应的资源
     */
    boolean deleteMenuResourceByMenuId(Integer menuId);

    /**
     * 删除指定资源对应的所有菜单关联。
     */
    boolean deleteMenuResourceByResourceId(Integer resourceId);

    /**
     * 获取指定code的ID对应的菜单ID
     */
    List<Integer> getMenuResourceByResourceId(Integer resourceId);
}
