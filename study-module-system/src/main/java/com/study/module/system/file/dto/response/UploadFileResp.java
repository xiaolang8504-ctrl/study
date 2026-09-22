package com.study.module.system.file.dto.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 上传文件响应类
 */
@Data
public class UploadFileResp {

    @ApiModelProperty("文件ID")
    private long id;

    @ApiModelProperty("文件原名称")
    private String originName;

    @ApiModelProperty("文件扩展名称")
    private String fileExtension;

    @ApiModelProperty("文件大小")
    private String fileSize;
}
