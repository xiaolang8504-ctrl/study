package com.study.api.dto.request;

import lombok.Data;

import java.io.Serializable;
import java.util.Map;

/**
 * 文件下载地址数据
 */
@Data
public class FileUrlData implements Serializable {

    private static final long serialVersionUID = 3096697129122000375L;

    /**
     * 文件名称
     */
    private String fileName;

    /**
     * 文件下载地址
     */
    private String fileUrl;

    /**
     * 文件下载请求头
     */
    private Map<String, String> requestHeaders;
}
