package com.study.module.system.wrongquestion.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 错题订正记录实体
 */
@Data
public class WrongQuestionCorrectionRecord {

    /**
     * 订正记录ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 错题ID
     */
    private Long wrongQuestionId;

    /**
     * 订正答案
     */
    private String correctionAnswer;

    /**
     * 订正解析
     */
    private String correctionAnalysis;

    /**
     * 订正图片地址
     */
    private String correctionImageUrl;

    /**
     * 订正备注
     */
    private String correctionRemark;

    /**
     * 订正前状态
     */
    private Integer beforeStatus;

    /**
     * 订正后状态
     */
    private Integer afterStatus;

    /**
     * 创建人ID
     */
    private Long createId;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
