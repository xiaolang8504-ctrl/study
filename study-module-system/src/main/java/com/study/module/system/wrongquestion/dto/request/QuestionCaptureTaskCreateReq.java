package com.study.module.system.wrongquestion.dto.request;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.validation.constraints.AssertTrue;
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
    @ApiModelProperty("已通过文件服务上传的采集文件ID（JPG、JPEG、PNG、PDF、DOCX）")
    private List<@NotNull Long> imageFileIds;
    @ApiModelProperty("PC 网页复制的图文内容；保留为人工确认前可编辑文本")
    @Size(max = 100000, message = "粘贴文档不能超过100000个字符")
    private String documentContent;
    @ApiModelProperty("网页复制内容标题")
    @Size(max = 100, message = "文档标题不能超过100个字符")
    private String documentTitle;

    @AssertTrue(message = "请至少选择一个采集文件或粘贴文档内容")
    public boolean isCaptureSourcePresent() {
        return (imageFileIds != null && !imageFileIds.isEmpty())
                || (documentContent != null && !documentContent.trim().isEmpty());
    }
}
