package com.study.api.dto.response;

import lombok.Data;

import java.io.Serializable;

/**
 * 文件预览地址
 */
@Data
public class FilePreviewData implements Serializable {

    private static final long serialVersionUID = -3589876116949537740L;

    /**
     * 预览地址
     */
    private String previewUrl;
}
