package com.study.module.system.file.dto.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 文件上传类型请求类
 */
@Data
public class FileUploadTypeReq {

    /**
     * 文件上传类型请求类
     */
    @NotBlank(message = "上传类型不为空")
    private String uploadType;
}
