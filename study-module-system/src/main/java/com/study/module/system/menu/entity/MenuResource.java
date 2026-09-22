package com.study.module.system.menu.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 菜单资源 中间表
 */
@Data
public class MenuResource implements Serializable {

    /**
     * 自增ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 菜单ID
     */
    private Integer menuId;

    /**
     * 资源id
     */
    private Integer resourceId;

    /**
     * 资源层级关系
     */
    private String resourceLevel;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    public LocalDateTime createTime;
}
