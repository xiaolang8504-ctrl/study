package com.study.module.system.review.dto.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Size;

/**
 * 更新学习偏好请求。
 */
@Data
public class UpdateLearningProfileReq {

    @ApiModelProperty("年级字典键值")
    @Size(max = 32, message = "年级长度不能超过32个字符")
    private String grade;

    @ApiModelProperty("科目字典键值")
    @Size(max = 32, message = "科目长度不能超过32个字符")
    private String subject;

    @ApiModelProperty("教材 ID，为空表示暂不设置教材")
    private Long bookId;
}
