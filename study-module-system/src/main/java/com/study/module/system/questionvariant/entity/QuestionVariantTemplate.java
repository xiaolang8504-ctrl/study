package com.study.module.system.questionvariant.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 参数变式模板。
 */
@Data
@TableName("sys_question_variant_template")
public class QuestionVariantTemplate {
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    /**
     * 模板编码
     */
    private String templateCode;
    /**
     * 模板名称
     */
    private String templateName;
    /**
     * 原始题目ID
     */
    private Long originalQuestionId;
    /**
     * 科目字典键值
     */
    private String subject;
    /**
     * 年级字典键值
     */
    private String grade;
    /**
     * 知识点ID
     */
    private Long knowledgePointId;
    /**
     * 题型字典键值
     */
    private String questionType;
    /**
     * 题干模板
     */
    private String titleTemplate;
    /**
     * 答案模板
     */
    private String answerTemplate;
    /**
     * 解析模板
     */
    private String analysisTemplate;
    /**
     * 当前版本号
     */
    private Integer currentVersion;
    /**
     * 状态
     */
    private Integer status;
    /**
     * 审核人ID
     */
    private Long reviewerId;
    /**
     * 审核时间
     */
    private LocalDateTime reviewTime;
    /**
     * 审核备注
     */
    private String reviewRemark;
    /**
     * 创建人ID
     */
    private Long createId;
    /**
     * 更新人ID
     */
    private Long updateId;
    /**
     * 删除标识
     */
    @TableLogic
    private Integer deleted;
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
