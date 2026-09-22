package com.study.module.system.file.service;

import java.io.InputStream;

/**
 * 文件实际内容校验服务。
 */
public interface FileContentValidationService {

    /**
     * 校验文件头与声明的扩展名是否一致。
     */
    void validate(String extension, InputStream inputStream);
}
