package com.study.module.system.file.dto.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * 上传文件请求类
 */
@Data
public class BatchUploadReq {

    @ApiModelProperty("文件")
    @NotNull(message = "上传文件不能为空")
    @NotEmpty(message = "至少需要上传一个文件")
    MultipartFile[] files;

    @ApiModelProperty("签名")
    @NotBlank(message = "上传签名不为空")
    private String signature;
}
