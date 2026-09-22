package com.study.module.system.wrongquestion.dto.request;

import com.study.common.core.domain.dto.PageParam;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 错题分页列表请求类
 */
@Data
public class WrongQuestionPageListReq extends PageParam {

    @ApiModelProperty("年级字典键值")
    private String grade;

    @ApiModelProperty("科目字典键值")
    private String subject;

    @ApiModelProperty("题目类型字典键值")
    private String questionType;

    @ApiModelProperty("来源字典键值")
    private String source;

    @ApiModelProperty("状态: 0待改, 1已改, 2已掌握, 3已归档")
    private Integer status;

    @ApiModelProperty("标准知识点ID")
    private Long knowledgePointId;

    @ApiModelProperty("错误类型标签")
    private String errorLabel;
}
