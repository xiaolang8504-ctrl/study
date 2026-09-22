package com.study.module.system.wrongquestion.dto.request;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 人工补充采集题块请求。
 */
@Data
public class QuestionCaptureRegionCreateReq {

    @NotNull(message = "采集任务ID不能为空")
    private Long taskId;

    @NotNull(message = "采集页面ID不能为空")
    private Long pageId;

    @NotNull(message = "题块左坐标不能为空")
    private Integer leftPosition;

    @NotNull(message = "题块上坐标不能为空")
    private Integer topPosition;

    @NotNull(message = "题块宽度不能为空")
    private Integer width;

    @NotNull(message = "题块高度不能为空")
    private Integer height;
}
