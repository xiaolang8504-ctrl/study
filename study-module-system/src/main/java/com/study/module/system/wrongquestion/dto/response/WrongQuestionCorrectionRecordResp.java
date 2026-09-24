package com.study.module.system.wrongquestion.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 错题订正记录响应类
 */
@Data
public class WrongQuestionCorrectionRecordResp {

    @ApiModelProperty("订正记录ID")
    private Long id;

    @ApiModelProperty("错题ID")
    private Long wrongQuestionId;

    @ApiModelProperty("订正版本号")
    private Integer revisionNo;

    @ApiModelProperty("提交前独立思路")
    private String thinking;

    @ApiModelProperty("学生自述错因")
    private String errorReason;

    @ApiModelProperty("订正答案")
    private String correctionAnswer;

    @ApiModelProperty("订正解析")
    private String correctionAnalysis;

    @ApiModelProperty("订正图片地址")
    private String correctionImageUrl;

    @ApiModelProperty("订正备注")
    private String correctionRemark;

    @ApiModelProperty("订正前状态")
    private Integer beforeStatus;

    @ApiModelProperty("订正后状态")
    private Integer afterStatus;

    @ApiModelProperty("创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
