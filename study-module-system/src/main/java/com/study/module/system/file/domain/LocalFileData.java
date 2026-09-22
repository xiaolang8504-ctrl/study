package com.study.module.system.file.domain;

import lombok.Data;

/**
 * 保存之后的文件数据
 */
@Data
public class LocalFileData {

    /**
     * 文件路径
     */
    private String filePath;

    /**
     * 文件名
     */
    private String saveFileName;
}
