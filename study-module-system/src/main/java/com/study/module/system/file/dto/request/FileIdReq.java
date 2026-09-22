package com.study.module.system.file.dto.request;

import lombok.Data;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotNull;

/**
 * 文件ID请求类
 */
@Data
public class FileIdReq {

    /**
     * 文件ID
     */
    @NotNull(message = "文件ID不为空")
    @Range(min = 1)
    private Integer fileId;
}
