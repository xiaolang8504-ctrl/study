package com.study.module.system.wrongquestion.dto.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 错题知识点统计请求类
 */
@Data
public class WrongQuestionKnowledgePointStatisticsReq {

    @ApiModelProperty("年级字典键值")
    private String grade;

    @ApiModelProperty("科目字典键值")
    private String subject;

    @ApiModelProperty("状态: 0待订正, 1已订正, 2已掌握, 3已归档")
    private Integer status;
}
