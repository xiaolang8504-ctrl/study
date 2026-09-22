package com.study.module.system.file.service;

import com.study.api.dto.response.FilePreviewData;

/**
 * 预览服务
 */
public interface PreviewFileService {

    /**
     * 预览地址
     */
    FilePreviewData previewUrl(Integer fileId, String uploadType);
}
