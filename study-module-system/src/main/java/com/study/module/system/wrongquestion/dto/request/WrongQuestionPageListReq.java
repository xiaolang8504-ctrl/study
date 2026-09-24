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

    @ApiModelProperty("结构化错因编码：READING、CONCEPT、METHOD、CALCULATION、EXPRESSION")
    private String errorCauseCode;

    @ApiModelProperty("能力层级：FOUNDATION、APPLICATION、COMPREHENSIVE")
    private String abilityLevel;

    @ApiModelProperty("教材版本")
    private String textbookVersion;

    @ApiModelProperty("章节")
    private String chapterName;

    @ApiModelProperty("个人标签")
    private String tagName;

    @ApiModelProperty("是否收藏：0否，1是")
    private Integer favorite;

    @ApiModelProperty("整理优先级：0-5")
    private Integer priorityLevel;
}
