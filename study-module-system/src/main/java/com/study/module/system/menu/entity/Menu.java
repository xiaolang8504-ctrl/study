package com.study.module.system.menu.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 系统菜单表
 */
@Data
public class Menu implements Serializable {

    /**
     * 自增ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 菜单编码
     */
    private String code;

    /**
     * 名称
     */
    private String menuName;

    /**
     * 父级ID
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
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    public LocalDateTime createTime;
}
