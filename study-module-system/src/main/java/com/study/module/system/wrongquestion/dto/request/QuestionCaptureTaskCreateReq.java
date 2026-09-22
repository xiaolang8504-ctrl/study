package com.study.module.system.wrongquestion.dto.request;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;
/**
 * 题目采集任务创建请求
 */
@Data
public class QuestionCaptureTaskCreateReq {
    @NotBlank(message = "年级不能为空") private String grade;
    @NotBlank(message = "科目不能为空") private String subject;
    @NotBlank(message = "题型不能为空") private String questionType;
    @NotBlank(message = "来源不能为空") private String source;
    private String learningPoint;
    private String errorLabels;
    @ApiModelProperty("客户端幂等请求标识")
    @Size(max = 64, message = "客户端请求标识长度不能超过64")
    private String clientRequestId;
    @ApiModelProperty(value = "已通过文件服务上传的采集文件ID（JPG、JPEG、PNG、PDF）", required = true)
    @NotEmpty(message = "请至少选择一个采集文件") private List<@NotNull Long> imageFileIds;
}
