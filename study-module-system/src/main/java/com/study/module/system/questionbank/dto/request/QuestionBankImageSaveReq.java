package com.study.module.system.questionbank.dto.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 题库图片保存请求，文件ID和图片地址至少提供一个
 */
@Data
public class QuestionBankImageSaveReq {

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
