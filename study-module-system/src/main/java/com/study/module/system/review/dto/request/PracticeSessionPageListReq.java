package com.study.module.system.review.dto.request;

import com.study.common.core.domain.dto.PageParam;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 专项练习历史分页请求
 */
@Data
public class PracticeSessionPageListReq extends PageParam {

    @ApiModelProperty("练习类型")
    private String practiceType;

    @ApiModelProperty("科目")
    private String subject;

    @ApiModelProperty("关键字")
    private String keyWord;
}
