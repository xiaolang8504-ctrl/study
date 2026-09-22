package com.study.module.system.questionbank.dto.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 题库图片列表响应
 */
@Data
public class QuestionBankImageListResp {

    @ApiModelProperty("题库图片ID")
    private Long id;

    @ApiModelProperty("文件ID")
    private Integer fileId;

    @ApiModelProperty("图片地址")
    private String imageUrl;

    @ApiModelProperty("图片原文件名")
    private String originalName;

    @ApiModelProperty("排序值")
    private Integer sort;

    @ApiModelProperty("是否为封面")
    private Boolean cover;
}
