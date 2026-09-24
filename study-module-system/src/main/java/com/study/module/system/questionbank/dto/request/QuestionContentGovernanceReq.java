package com.study.module.system.questionbank.dto.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;

/** 题目内容治理请求。 */
@Data
public class QuestionContentGovernanceReq {
    @NotNull(message = "题目ID不能为空")
    @ApiModelProperty("题目ID")
    private Long questionId;

    @NotBlank(message = "治理动作不能为空")
    @ApiModelProperty("治理动作：DOWN下架、RESTORE恢复、LICENSE_UPDATE更新授权、RECORD_PROOF登记版权凭证")
    private String action;

    @NotBlank(message = "问题类型不能为空")
    @ApiModelProperty("问题类型：STEM_ERROR、ANSWER_ERROR、ANALYSIS_ERROR、OUT_OF_SYLLABUS、DUPLICATE、INFRINGEMENT、LICENSE_EXPIRED")
    private String issueType;

    @Size(max = 500, message = "处理说明不能超过500个字符")
    @ApiModelProperty("处理说明、下架原因或授权说明")
    private String handleRemark;

    @ApiModelProperty("版权或授权凭证的 questionBank 上传文件ID")
    private List<Integer> proofFileIds;

    @Size(max = 50, message = "授权版本不能超过50个字符")
    @ApiModelProperty("授权版本")
    private String licenseVersion;

    @ApiModelProperty("授权到期日")
    private LocalDate expireAt;
}
