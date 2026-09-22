package com.study.module.system.book.dto.request;

import com.study.common.core.domain.dto.PageParam;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 课本分页请求
 */
@Data
public class BookPageListReq extends PageParam {

    @ApiModelProperty("年级字典键值")
    private String grade;

    @ApiModelProperty("科目字典键值")
    private String subject;

    @ApiModelProperty("标题或内容关键词")
    private String keyWord;
}
