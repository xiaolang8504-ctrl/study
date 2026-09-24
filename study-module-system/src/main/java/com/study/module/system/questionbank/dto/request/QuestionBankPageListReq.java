package com.study.module.system.questionbank.dto.request;

import io.swagger.annotations.ApiModelProperty;
import com.study.common.core.domain.dto.PageParam;
import lombok.Data;

/**
 * 题库题目分页列表请求
 */
@Data
public class QuestionBankPageListReq extends PageParam {

    @ApiModelProperty("年级字典键值")
    private String grade;

    @ApiModelProperty("科目字典键值")
    private String subject;

    @ApiModelProperty("题型字典键值")
    private String questionType;

    private String textbookVersion;
    private String chapterName;
    private String region;
    private Integer examYear;
    private String paperType;

    @ApiModelProperty("审核状态")
    private Integer reviewStatus;

    @ApiModelProperty("启用状态")
    private Integer enable;

    @ApiModelProperty("搜索关键字")
    private String keyWord;
}
