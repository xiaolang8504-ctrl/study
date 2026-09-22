package com.study.module.system.review.dto.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 当前学生学习偏好响应。
 */
@Data
public class LearningProfileResp {

    @ApiModelProperty("年级字典键值")
    private String grade;

    @ApiModelProperty("科目字典键值")
    private String subject;

    @ApiModelProperty("教材 ID")
    private Long bookId;

    @ApiModelProperty("教材标题")
    private String bookTitle;
}
