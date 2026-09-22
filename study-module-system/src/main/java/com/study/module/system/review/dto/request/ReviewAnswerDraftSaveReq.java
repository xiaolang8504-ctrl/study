package com.study.module.system.review.dto.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 在查看参考答案前保存的主动回忆答案。
 */
@Data
public class ReviewAnswerDraftSaveReq {

    @ApiModelProperty(value = "复习项目ID", required = true)
    @NotNull(message = "复习项目ID不能为空")
    private Long reviewItemId;

    @ApiModelProperty(value = "学生主动回忆答案", required = true)
    @NotBlank(message = "请先填写本次答案")
    @Length(max = 4000, message = "本次答案最长为{max}位")
    private String studentAnswer;
}
