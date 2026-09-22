package com.study.module.system.wrongquestion.dto.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 错题知识点统计响应类
 */
@Data
public class WrongQuestionKnowledgePointStatisticsResp {

    @ApiModelProperty("知识点ID")
    private Long knowledgePointId;

    @ApiModelProperty("知识点名称")
    private String knowledgePointName;

    @ApiModelProperty("年级字典键值")
    private String grade;

    @ApiModelProperty("科目字典键值")
    private String subject;

    @ApiModelProperty("错题总数")
    private Integer wrongQuestionCount;

    @ApiModelProperty("待订正数量")
    private Integer pendingCorrectionCount;

    @ApiModelProperty("已订正数量")
    private Integer correctedCount;

    @ApiModelProperty("已掌握数量")
    private Integer masteredCount;

    @ApiModelProperty("已归档数量")
    private Integer archivedCount;
}
