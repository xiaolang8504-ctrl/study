package com.study.module.system.review.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 今日复习题目响应类
 */
@Data
public class ReviewTodayTaskResp {

    @ApiModelProperty("复习项目ID")
    private Long reviewItemId;

    @ApiModelProperty("错题ID")
    private Long wrongQuestionId;

    @ApiModelProperty("年级名称")
    private String gradeName;

    @ApiModelProperty("科目名称")
    private String subjectName;

    @ApiModelProperty("题目类型名称")
    private String questionTypeName;

    @ApiModelProperty("题目标题")
    private String questionTitle;

    @ApiModelProperty("题目内容")
    private String questionContent;

    @ApiModelProperty("题目图片地址")
    private String imageUrl;

    @ApiModelProperty("题目图片地址2")
    private String imageUrl2;

    @ApiModelProperty("题目图片地址3")
    private String imageUrl3;

    @ApiModelProperty("题目图片地址4")
    private String imageUrl4;

    @ApiModelProperty("知识点")
    private String learningPoint;

    @ApiModelProperty("错误类型标签")
    private String errorLabels;

    @ApiModelProperty("错题难度等级")
    private Integer level;

    @ApiModelProperty("当前复习阶段")
    private Integer stage;

    @ApiModelProperty("当前掌握度，0-100")
    private Integer masteryScore;

    @ApiModelProperty("连续掌握次数")
    private Integer correctStreak;

    @ApiModelProperty("连续错误次数")
    private Integer wrongStreak;

    @ApiModelProperty("遗忘次数")
    private Integer lapseCount;

    @ApiModelProperty("累计复习次数")
    private Integer reviewCount;

    @ApiModelProperty("下次复习时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime nextReviewTime;

    @ApiModelProperty("当前复习间隔，单位分钟")
    private Integer currentIntervalMinutes;

    @ApiModelProperty("为何今天进入题单")
    private String dueReason;

    @ApiModelProperty("预计完成本题的分钟数")
    private Integer estimatedMinutes;

    @ApiModelProperty("不同反馈下的下一次复习预估")
    private List<ReviewFeedbackProjectionResp> feedbackProjectionList;

    @ApiModelProperty("是否逾期: 0否, 1是")
    private Integer overdue;
}
