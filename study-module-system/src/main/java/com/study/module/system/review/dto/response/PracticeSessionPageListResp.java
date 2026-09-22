package com.study.module.system.review.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 专项练习历史分页响应
 */
@Data
public class PracticeSessionPageListResp {

    @ApiModelProperty("练习会话ID")
    private Long id;

    @ApiModelProperty("标题")
    private String title;

    @ApiModelProperty("练习类型")
    private String practiceType;

    @ApiModelProperty("科目名称")
    private String subjectName;

    @ApiModelProperty("知识点")
    private String learningPoint;

    @ApiModelProperty("错因标签")
    private String errorLabel;

    @ApiModelProperty("题目数量")
    private Integer questionCount;

    @ApiModelProperty("正确数量")
    private Integer correctCount;

    @ApiModelProperty("错误数量")
    private Integer wrongCount;

    @ApiModelProperty("正确率")
    private Integer accuracyRate;

    @ApiModelProperty("状态")
    private Integer status;

    @ApiModelProperty("完成时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime finishTime;

    @ApiModelProperty("创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
