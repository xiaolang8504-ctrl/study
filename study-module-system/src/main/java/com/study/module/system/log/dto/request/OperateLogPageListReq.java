package com.study.module.system.log.dto.request;

import com.study.common.core.domain.dto.PageParam;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 操作日志分页请求类
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class OperateLogPageListReq extends PageParam {

    @ApiModelProperty("操作人")
    private String operateName;
}
