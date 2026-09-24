package com.study.module.system.questionbank.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/** 知识点前置关系；每条关系带版本，便于教材版本调整后保留可追溯历史。 */
@Data
@TableName("sys_knowledge_point_prerequisite")
public class KnowledgePointPrerequisite {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long knowledgePointId;
    private Long prerequisitePointId;
    private String relationVersion;
    private Integer enable;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
