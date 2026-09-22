package com.study.module.system.review.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * 提交复习反馈请求类
 */
@Data
public class SubmitReviewFeedbackReq {

    @ApiModelProperty(value = "客户端幂等请求ID", required = true)
    @NotBlank(message = "请求ID不能为空")
    @Length(max = 64, message = "请求ID最长为{max}位")
    private String requestId;

    @ApiModelProperty(value = "复习项目ID", required = true)
    @NotNull(message = "复习项目ID不能为空")
    private Long reviewItemId;

    @ApiModelProperty(value = "开始主动回忆时间", required = true)
    @NotNull(message = "开始主动回忆时间不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @ApiModelProperty(value = "服务端答案解锁凭证", required = true)
    @NotBlank(message = "答案解锁凭证不能为空")
    @Length(max = 64, message = "答案解锁凭证最长为{max}位")
    private String revealToken;

    @ApiModelProperty(value = "学生本次作答", required = true)
    @NotBlank(message = "请先填写本次答案")
    @Length(max = 4000, message = "本次答案最长为{max}位")
    private String studentAnswer;

    @ApiModelProperty(value = "学生自评是否正确: 0错误, 1正确；可自动判题时由服务端覆盖")
    @Min(value = 0, message = "作答结果不正确")
    @Max(value = 1, message = "作答结果不正确")
    private Integer selfCorrect;

    @ApiModelProperty(value = "反馈: 0忘记, 1困难, 2掌握, 3很简单", required = true)
    @NotNull(message = "复习反馈不能为空")
    @Min(value = 0, message = "复习反馈不正确")
    @Max(value = 3, message = "复习反馈不正确")
    private Integer feedback;
}
