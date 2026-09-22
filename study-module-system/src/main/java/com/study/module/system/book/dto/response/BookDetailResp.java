package com.study.module.system.book.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 课本详情响应
 */
@Data
public class BookDetailResp {

    @ApiModelProperty("课本ID")
    private Long id;

    @ApiModelProperty("年级字典键值")
    private String grade;

    @ApiModelProperty("年级名称")
    private String gradeName;

    @ApiModelProperty("科目字典键值")
    private String subject;

    @ApiModelProperty("科目名称")
    private String subjectName;

    @ApiModelProperty("标题")
    private String title;

    @ApiModelProperty("内容简介")
    private String content;

    @ApiModelProperty("课本附件文件ID或地址")
    private String imageUrl;

    @ApiModelProperty("附件原文件名")
    private String fileName;

    @ApiModelProperty("附件扩展名")
    private String fileExtension;

    @ApiModelProperty("附件大小")
    private String fileSize;

    @ApiModelProperty("创建人ID")
    private Long createUserId;

    @ApiModelProperty("创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @ApiModelProperty("更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
