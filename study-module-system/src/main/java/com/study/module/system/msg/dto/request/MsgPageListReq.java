package com.study.module.system.msg.dto.request;

import com.study.common.core.domain.dto.PageParam;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 消息分页列表请求类
 */
@Data
public class MsgPageListReq extends PageParam {

    @ApiModelProperty("是否已读: 0否, 1是")
    private Integer isRead;
}
