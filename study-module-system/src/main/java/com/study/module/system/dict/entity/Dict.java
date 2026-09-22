package com.study.module.system.dict.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.time.LocalDateTime;


/**
 * 字典实体类
 */
@Data
public class Dict {

    /**
     * 字典类型ID
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 字典类型
     */
    private String dictType;

    /**
     * 字典类型名称
     */
    private String dictName;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
