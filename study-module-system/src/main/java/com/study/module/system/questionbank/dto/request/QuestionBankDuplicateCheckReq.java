package com.study.module.system.questionbank.dto.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 题库重复题检测请求
 */
@Data
public class QuestionBankDuplicateCheckReq {

    @ApiModelProperty("编辑时排除的题库题目ID")
    private Long id;

    @ApiModelProperty("年级字典键值")
    @NotBlank(message = "年级不能为空")
    private String grade;

    @ApiModelProperty("科目字典键值")
    @NotBlank(message = "科目不能为空")
    private String subject;

    @ApiModelProperty("题目内容")
    @NotBlank(message = "题目内容不能为空")
    private String questionContent;
}
