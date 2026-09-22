package com.study.module.system.file.dto.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 题目图片分页响应
 */
@Data
public class QuestionImagePageListResp {

    @ApiModelProperty("文件ID")
    private Integer id;

    @ApiModelProperty("原始文件名")
    private String originName;

    @ApiModelProperty("文件扩展名")
    private String fileExtension;

    @ApiModelProperty("文件字节数")
    private Integer fileBytes;

    @ApiModelProperty("格式化文件大小")
    private String fileSize;

    @ApiModelProperty("上传时间")
    private LocalDateTime createTime;

    @ApiModelProperty("预览地址")
    private String imageUrl;

    @ApiModelProperty("是否已被题目引用")
    private Boolean referenced;
}
