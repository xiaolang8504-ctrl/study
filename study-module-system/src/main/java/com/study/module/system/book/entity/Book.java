package com.study.module.system.book.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 课本实体
 */
@Data
public class Book {

    /**
     * 课本ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 年级字典键值
     */
    private String grade;

    /**
     * 年级字典名称
     */
    private String gradeName;

    /**
     * 科目字典键值
     */
    private String subject;

    /**
     * 科目字典名称
     */
    private String subjectName;

    /**
     * 课本标题
     */
    private String title;

    /**
     * 内容简介
     */
    private String content;

    /**
     * 课本附件文件ID或地址
     */
    private String imageUrl;

    /**
     * 创建人ID
     */
    private Long createUserId;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
