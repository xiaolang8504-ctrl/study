package com.study.module.system.review.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 提交纸面练习卷逐题回填请求。
 */
@Data
@ApiModel("提交纸面练习卷逐题回填请求")
public class PracticePaperAnswerFillReq {

    @ApiModelProperty(value = "纸面短码，如P0000000012", required = true)
    @NotBlank(message = "纸面短码不能为空")
    @Size(max = 32, message = "纸面短码长度不能超过32个字符")
    private String paperCode;

    @ApiModelProperty(value = "NEW新增一次、OVERWRITE覆盖最近一次", required = true)
    @NotBlank(message = "请选择重复回填方式")
    @Size(max = 16, message = "重复回填方式不正确")
    private String fillMode;

    @ApiModelProperty("做完的纸面练习卷附件文件ID；仅做关联，不自动判题")
    private Long answerFileId;

    @ApiModelProperty("学生实际纸面作答时间，空则取当前时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime answerTime;

    @ApiModelProperty(value = "逐题作答结果", required = true)
    @Valid
    @NotEmpty(message = "逐题作答结果不能为空")
    private List<PracticePaperAnswerFillItemReq> answerList;
}
