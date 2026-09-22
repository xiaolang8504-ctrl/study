package com.study.module.system.role.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 系统角色表
 */
@Data
public class Role implements Serializable {

    /**
     * 自增ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 名称
     */
    private String roleName;

    /**
     * 是否管理员(0否 1是)
     */
    private Integer isSystem;

    /**
     * 菜单ID集合(id以逗号隔开)
     */
    private String menuIds;

    /**
     * 菜单层级id,分割：1-2-3,1-2-4
     */
    private String menuLevel;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    public LocalDateTime createTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    public LocalDateTime updateTime;
}
