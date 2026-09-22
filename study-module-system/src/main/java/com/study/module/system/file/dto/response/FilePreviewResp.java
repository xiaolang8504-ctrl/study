package com.study.module.system.file.dto.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 文件预览响应类
 */
@Data
public class FilePreviewResp {

    @ApiModelProperty("预览地址")
    private String previewUrl;
}
