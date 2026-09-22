package com.study.module.system.review.dto.response;

import com.study.common.core.domain.dto.PageResult;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 复习提醒首页响应
 */
@Data
public class ReviewReminderHomeResp {

    @ApiModelProperty("未读提醒数量")
    private Long unreadCount;

    @ApiModelProperty("提醒分页")
    private PageResult<ReviewReminderPageListResp> pageResult;
}
