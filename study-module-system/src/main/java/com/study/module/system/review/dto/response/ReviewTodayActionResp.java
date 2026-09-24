package com.study.module.system.review.dto.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 今日任务包中的一个可执行学习步骤。
 */
@Data
public class ReviewTodayActionResp {

    @ApiModelProperty("步骤类型：REVIEW、CORRECTION、WEAK_POINT_PRACTICE")
    private String actionType;

    @ApiModelProperty("步骤标题")
    private String title;

    @ApiModelProperty("为什么推荐该步骤")
    private String reason;

    @ApiModelProperty("建议完成题数")
    private Integer questionCount;

    @ApiModelProperty("预计用时，单位分钟")
    private Integer estimatedMinutes;

    @ApiModelProperty("当前科目筛选，为空表示全部")
    private String subject;

    @ApiModelProperty("薄弱知识点练习的目标知识点")
    private String learningPoint;

    @ApiModelProperty("完成该步骤后的下一项建议")
    private String nextActionText;
}
