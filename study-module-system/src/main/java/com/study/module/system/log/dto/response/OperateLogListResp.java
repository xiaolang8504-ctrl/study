package com.study.module.system.log.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 操作日志分页响应类
 */
@Data
public class OperateLogListResp {

    @ApiModelProperty("操作日志ID")
    private Long id;

    @ApiModelProperty("操作人")
    private String operateName;

    @ApiModelProperty("操作所属模块文本")
    private String operateModuleText;

    @ApiModelProperty("操作类型文本")
    private String operateTypeText;

    @ApiModelProperty("操作内容")
    private String operateContent;

    @ApiModelProperty("IP地址")
    private String ipAddress;

    @ApiModelProperty("操作时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private String createTime;
}
