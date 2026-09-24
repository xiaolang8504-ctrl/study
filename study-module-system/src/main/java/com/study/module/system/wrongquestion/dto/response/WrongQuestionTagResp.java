package com.study.module.system.wrongquestion.dto.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/** 可用于筛选和编辑的个人标签。 */
@Data
public class WrongQuestionTagResp {
    private Long id;
    @ApiModelProperty("标签名称")
    private String tagName;
    @ApiModelProperty("使用该标签的错题数")
    private Long wrongQuestionCount;
}
