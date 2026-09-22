package com.study.module.system.dict.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.time.LocalDateTime;


/**
 * 数据字典实体类
 */
@Data
public class DictData {

    /**
     * 字典ID
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 字典标签
     */
    private String dictLabel;

    /**
     * 字典键值
     */
    private String dictValue;

    /**
     * 字典类型
     */
    private String dictType;

    /**
     * 排序
     */
    private Integer dictDataSort;

    /**
     * 是否启用: 0否, 1是
     */
    private Integer isEnable;

    /**
     * 备注
     */
    private String remark;

    /**
     * 录入人ID
     */
    private Long createId;

    /**
     * 录入名
     */
    private String createName;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
