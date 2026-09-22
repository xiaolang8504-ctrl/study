package com.study.module.system.file.domain;

import lombok.Data;

/**
 * 保存之后的文件数据
 */
@Data
public class UrlFileData {

    /**
     * 文件路径
     */
    private String filePath;

    /**
     * 文件名
     */
    private String saveFileName;

    /**
     * 文件扩展名称
     */
    private String fileExtension;

    /**
     * 文件字节数
     */
    private Integer fileBytes;

    /**
     * 文件大小(格式化后)
     */
    private String fileSize;
}
