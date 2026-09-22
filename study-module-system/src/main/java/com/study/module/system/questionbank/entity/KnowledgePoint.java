package com.study.module.system.questionbank.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 学科知识点目录实体。
 */
@Data
@TableName("sys_knowledge_point")
public class KnowledgePoint {
    /**
     * 知识点ID。
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 父知识点ID，0表示根节点。
     */
    private Long parentId;

    /**
     * 知识点唯一编码。
     */
    private String pointCode;

    /**
     * 知识点名称。
     */
    private String pointName;

    /**
     * 年级字典键值。
     */
    private String grade;

    /**
     * 科目字典键值。
     */
    private String subject;

    /**
     * 同级排序号。
     */
    private Integer sort;

    /**
     * 启用状态：0停用，1启用。
     */
    private Integer enable;

    /**
     * 创建时间。
     */
    private LocalDateTime createTime;

    /**
     * 更新时间。
     */
    private LocalDateTime updateTime;
}
