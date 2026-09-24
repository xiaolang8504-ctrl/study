package com.study.module.system.wrongquestion.dto.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 人工遮罩后的整页图片请求。原图不会被替换，用户可随时回退。
 */
@Data
public class QuestionCaptureManualCleanReq {

    @ApiModelProperty(value = "采集页面ID", required = true)
    @NotNull(message = "采集页面ID不能为空")
    private Long id;

    @ApiModelProperty(value = "人工遮罩整页图片文件ID", required = true)
    @NotNull(message = "人工遮罩图片不能为空")
    private Long cleanedFileId;
}
