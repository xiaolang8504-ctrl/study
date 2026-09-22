package com.study.module.system.menu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.study.module.system.menu.dto.request.MenuCodeReq;
import com.study.module.system.menu.entity.Menu;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * 系统菜单表 服务类
 */
public interface MenuService extends IService<Menu> {

    /**
     * 检测指定编码是否存在
     */
    boolean checkMenuCode(String code);

    /**
     * ID检测菜单是否存在
     */
    Menu checkMenuById(Integer id);

    /**
     * 检测指定ID是否存在子集
     */
    boolean checkSonByPid(Integer pid);

    /**
     * 检测指定ID和编码是否存在
     */
    boolean checkMenuCodeById(Integer id, String code);

    /**
     * 检测指定IDS对应的code
     */
    Map<Integer, String> getCodeByMenuIds(List<Integer> ids);

    /**
     * 从前端menuCode.json生成菜单
     */
    @Transactional(rollbackFor = Exception.class)
    List<Menu> createMenCode(List<MenuCodeReq> request);
}
