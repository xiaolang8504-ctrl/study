package com.study.module.system.file.domain;

import lombok.Data;

import java.io.Serializable;

/**
 * 文件下载凭证
 */
@Data
public class FileSignatureData implements Serializable {

    private static final long serialVersionUID = 5764423841088540774L;

    /**
     * 凭证
     */
    private String signature;
}