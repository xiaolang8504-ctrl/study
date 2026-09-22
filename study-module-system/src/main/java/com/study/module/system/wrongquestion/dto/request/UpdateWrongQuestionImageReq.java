package com.study.module.system.wrongquestion.dto.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotNull;

/**
 * 错图修改请求类
 */
@Data
public class UpdateWrongQuestionImageReq {

    @ApiModelProperty(value = "错题ID", required = true)
    @NotNull(message = "错题ID不能为空")
    @Range(min = 1, message = "错题ID需大于{min}")
    private Long id;

    @ApiModelProperty("题目图片或A选项图片地址")
    private String imageUrl;

    @ApiModelProperty("B选项图片地址")
    private String imageUrl2;

    @ApiModelProperty("C选项图片地址")
    private String imageUrl3;

    @ApiModelProperty("D选项图片地址")
    private String imageUrl4;
}
