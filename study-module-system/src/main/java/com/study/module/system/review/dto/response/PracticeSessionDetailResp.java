package com.study.module.system.review.dto.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 专项练习详情响应
 */
@Data
public class PracticeSessionDetailResp {

    @ApiModelProperty("练习会话ID")
    private Long sessionId;

    @ApiModelProperty("标题")
    private String title;

    @ApiModelProperty("组卷规则快照")
    private String generationReason;

    @ApiModelProperty("每题作答留白行数")
    private Integer blankLineCount;

    @ApiModelProperty("答案位置：AFTER_EACH逐题显示，END卷末集中显示")
    private String answerPosition;

    @ApiModelProperty("题面图片：ORIGINAL原图，GRAYSCALE灰度预览，TEXT_ONLY仅文字")
    private String imageMode;

    @ApiModelProperty("练习卷版本")
    private Integer paperVersion;

    @ApiModelProperty("排版栏数")
    private Integer columnCount;

    @ApiModelProperty("练习类型")
    private String practiceType;

    @ApiModelProperty("题目数量")
    private Integer questionCount;

    @ApiModelProperty("已答数量")
    private Integer answeredCount;

    @ApiModelProperty("正确数量")
    private Integer correctCount;

    @ApiModelProperty("错误数量")
    private Integer wrongCount;

    @ApiModelProperty("正确率")
    private Integer accuracyRate;

    @ApiModelProperty("状态")
    private Integer status;

    @ApiModelProperty("题目列表")
    private List<PracticeQuestionResp> questionList;
}
