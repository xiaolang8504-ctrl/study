package com.study.module.system.menu.dto.response;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import java.util.List;

@Data
public class MenuTreeResp {

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
     * 是否有子(true有,false无)
     */
    private Boolean isSon;

    /**
     * 菜单对应子集列表
     */
    @TableField(exist = false,select=false)
    private List<MenuTreeResp> children;
}
