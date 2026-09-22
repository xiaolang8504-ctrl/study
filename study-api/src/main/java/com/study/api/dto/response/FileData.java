package com.study.api.dto.response;

import lombok.Data;
import java.io.Serializable;

/**
 * 公共文件信息
 */
@Data
public class FileData implements Serializable {

    private static final long serialVersionUID = 7955916170675189260L;

    /**
     * 文件ID
     */
    private Integer id;

    /**
     * 文件原名称
     */
    private String originName;

    /**
     * 文件扩展名称
     */
    private String fileExtension;

    /**
     * 文件大小
     */
    private String fileSize;
}
