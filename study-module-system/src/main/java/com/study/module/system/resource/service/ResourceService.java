package com.study.module.system.resource.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.study.module.system.resource.entity.Resource;

import java.util.List;

/**
 * 系统资源表 服务类
 */
public interface ResourceService extends IService<Resource> {

    /**
     * 检测指定code存在
     */
    boolean checkResourceCode(String code);

    /**
     * 检测指定code和ID存在
     */
    boolean checkResourceCodeById(Integer id, String code);

    /**
     * 检测指定ID存在
     */
    void checkResourceById(Integer id);

    /**
     * 检测指定ID是否有子
     */
    boolean checkSonByPid(Integer pid);

    /**
     * 获取指定code信息
     */
    Resource getResourceCode(String code);

    /**
     * 从Controller权限注解生成资源
     */
    List<Resource> createPre();
}
