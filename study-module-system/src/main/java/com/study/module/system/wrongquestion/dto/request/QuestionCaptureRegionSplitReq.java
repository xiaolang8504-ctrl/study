package com.study.module.system.wrongquestion.dto.request;

import lombok.Data;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * 按指定比例切分采集题块请求。
 */
@Data
public class QuestionCaptureRegionSplitReq {

    @NotNull(message = "题块ID不能为空")
    private Long id;

    @NotNull(message = "切分比例不能为空")
    @Min(value = 10, message = "切分比例不能小于10%")
    @Max(value = 90, message = "切分比例不能大于90%")
    private Integer splitRatio;
}
