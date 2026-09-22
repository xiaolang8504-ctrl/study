package com.study.module.system.questionbank.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 题库题目图片与文件资源关联。
 */
@Data
@TableName("sys_question_bank_image")
public class QuestionBankImage {
    /**
     * 题目图片关联ID。
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 题库题目ID。
     */
    private Long questionId;

    /**
     * 系统文件ID。
     */
    private Integer fileId;

    /**
     * 兼容外部图片URL。
     */
    private String imageUrl;

    /**
     * 图片原始文件名。
     */
    private String originalName;

    /**
     * 图片展示顺序。
     */
    private Integer sort;

    /**
     * 是否封面：0否，1是。
     */
    private Integer isCover;
}
