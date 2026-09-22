package com.study.module.system.homework.dto.request;

import com.study.common.core.domain.dto.PageParam;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 作业分页请求
 */
@Data
public class HomeWorkPageListReq extends PageParam {

    @ApiModelProperty("年级字典键值")
    private String grade;

    @ApiModelProperty("科目字典键值")
    private String subject;

    @ApiModelProperty("标题或内容关键词")
    private String keyWord;
}
