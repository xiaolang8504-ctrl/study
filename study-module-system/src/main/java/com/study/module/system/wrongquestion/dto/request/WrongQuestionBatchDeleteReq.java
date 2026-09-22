package com.study.module.system.wrongquestion.dto.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * 错题批量删除请求类
 */
@Data
public class WrongQuestionBatchDeleteReq {

    @ApiModelProperty(value = "错题ID列表", required = true)
    @NotEmpty(message = "错题ID列表不能为空")
    private List<Long> ids;
}
