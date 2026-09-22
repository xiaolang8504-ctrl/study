package com.study.module.system.book.dto.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 课本请求基类
 */
@Data
public class BookReq {

    @ApiModelProperty(value = "年级字典键值", required = true)
    @NotBlank(message = "年级不能为空")
    private String grade;

    @ApiModelProperty(value = "科目字典键值", required = true)
    @NotBlank(message = "科目不能为空")
    private String subject;

    @ApiModelProperty(value = "标题", required = true)
    @NotBlank(message = "标题不能为空")
    private String title;

    @ApiModelProperty(value = "内容简介", required = true)
    @NotBlank(message = "内容简介不能为空")
    private String content;

    @ApiModelProperty("课本附件文件ID或地址")
    private String imageUrl;
}
