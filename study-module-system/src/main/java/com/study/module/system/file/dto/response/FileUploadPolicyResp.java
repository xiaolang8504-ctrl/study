package com.study.module.system.file.dto.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 文件上传签名返回
 */
@Data
public class FileUploadPolicyResp {

    @ApiModelProperty("签名")
    private String signature;
}
