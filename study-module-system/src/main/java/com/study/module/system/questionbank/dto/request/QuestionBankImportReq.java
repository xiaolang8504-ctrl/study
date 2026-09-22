package com.study.module.system.questionbank.dto.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * A4文件识别导入题库请求。
 */
@Data
public class QuestionBankImportReq {
    @ApiModelProperty(value = "年级字典键值", required = true)
    @NotBlank(message = "年级不能为空")
    private String grade;

    @ApiModelProperty(value = "科目字典键值", required = true)
    @NotBlank(message = "科目不能为空")
    private String subject;

    @ApiModelProperty("题型字典键值，未传时按OCR结果自动识别")
    private String questionType;

    @ApiModelProperty(value = "来源字典键值", required = true)
    @NotBlank(message = "题目来源不能为空")
    private String source;

    @ApiModelProperty(value = "A4文件ID", required = true)
    @NotNull(message = "A4文件ID不能为空")
    @Range(min = 1, message = "A4文件ID需大于{min}")
    private Long fileId;

    @ApiModelProperty("判题模式：AUTO自动判题，SELF查看答案后自评")
    @NotBlank(message = "判题模式不能为空")
    private String judgeMode = "SELF";

    @ApiModelProperty("题目难度")
    @Min(value = 1, message = "题目难度不能小于{value}")
    @Max(value = 5, message = "题目难度不能超过{value}")
    private Integer difficulty = 3;

    @ApiModelProperty("关联知识点ID")
    private List<Long> knowledgePointIds;
}
