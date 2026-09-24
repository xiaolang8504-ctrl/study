package com.study.module.system.review.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 不同复习反馈下的下一次排期预估。
 *
 * <p>这是提交前的透明预览；最终时间仍以实际作答是否独立、判题结果和提交时刻为准。</p>
 */
@Data
public class ReviewFeedbackProjectionResp {

    @ApiModelProperty("反馈值：0忘记、1困难、2掌握、3很简单")
    private Integer feedback;

    @ApiModelProperty("反馈名称")
    private String feedbackName;

    @ApiModelProperty("预估后的掌握度")
    private Integer masteryScoreAfter;

    @ApiModelProperty("预估后的复习阶段")
    private Integer stageAfter;

    @ApiModelProperty("预估后的复习间隔，单位分钟")
    private Integer intervalMinutes;

    @ApiModelProperty("预估下次复习时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime nextReviewTime;

    @ApiModelProperty("预估依据与前提")
    private String explanation;
}
