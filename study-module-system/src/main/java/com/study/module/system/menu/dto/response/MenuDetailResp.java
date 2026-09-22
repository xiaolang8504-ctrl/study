package com.study.module.system.menu.dto.response;

import lombok.Data;

@Data
public class MenuDetailResp {

    /**
     * 新增ID
     */
    private Integer id;

    /**
     * 菜单编码
     */
    private String code;

    /**
     * 菜单名称
     */
    private String menuName;

    /**
     * 菜单父ID
     */
    private Integer pid;

    /**
     * 菜单的层级数
     */
    private Integer level;

    /**
     * 排序号
     */
    private Integer sort;

    /**
     * 菜单对应资源ID集(逗号隔开)
     */
    private String resourceIds;

    /**
     * 菜单对应资源层级关系
     */
    private String resourceLevel;

    /**
     * 是否有子(1有,0无)
     */
    private Integer isSon;
}