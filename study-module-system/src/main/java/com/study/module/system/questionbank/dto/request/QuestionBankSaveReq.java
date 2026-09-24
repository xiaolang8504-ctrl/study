package com.study.module.system.questionbank.dto.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * 题库题目保存请求
 */
@Data
public class QuestionBankSaveReq {

    @ApiModelProperty("主键ID")
    private Long id;

    @ApiModelProperty("年级字典键值")
    @NotBlank(message = "年级不能为空")
    private String grade;

    @ApiModelProperty("年级名称")
    private String gradeName;

    @ApiModelProperty("科目字典键值")
    @NotBlank(message = "科目不能为空")
    private String subject;

    @ApiModelProperty("科目名称")
    private String subjectName;

    @ApiModelProperty("题型字典键值")
    @NotBlank(message = "题型不能为空")
    private String questionType;

    @ApiModelProperty("题型名称")
    private String questionTypeName;

    @ApiModelProperty("题目标题")
    @NotBlank(message = "题目标题不能为空")
    private String questionTitle;

    @ApiModelProperty("题目内容")
    @NotBlank(message = "题目内容不能为空")
    private String questionContent;

    @ApiModelProperty("内容格式：TEXT或LATEX")
    private String contentFormat;

    @ApiModelProperty("题目图片地址，多个用逗号分隔")
    private String imageUrls;

    @ApiModelProperty("选项JSON")
    private String optionsJson;

    @ApiModelProperty("正确答案")
    @NotBlank(message = "正确答案不能为空")
    private String correctAnswer;

    @ApiModelProperty("判题模式：AUTO自动判题，SELF查看答案后自评")
    @NotBlank(message = "判题模式不能为空")
    private String judgeMode;

    @ApiModelProperty("答案解析")
    private String analysis;

    @ApiModelProperty("题目难度，取值1至5")
    @Min(value = 1, message = "题目难度不能小于{value}")
    @Max(value = 5, message = "题目难度不能超过{value}")
    private Integer difficulty;

    @ApiModelProperty("题目来源字典键值")
    @NotBlank(message = "题目来源不能为空")
    private String source;

    @ApiModelProperty("题目来源名称")
    private String sourceName;

    @Size(max = 100, message = "教材版本不能超过100个字符") private String textbookVersion;
    @Size(max = 200, message = "章节不能超过200个字符") private String chapterName;
    @Size(max = 100, message = "地区不能超过100个字符") private String region;
    @Min(value = 1900, message = "年份不正确") @Max(value = 2100, message = "年份不正确") private Integer examYear;
    @Size(max = 50, message = "卷型不能超过50个字符") private String paperType;

    @ApiModelProperty("内容提供方或版权方")
    private String provider;

    @ApiModelProperty("外部系统或合同中的题目唯一标识")
    private String externalId;

    @ApiModelProperty("授权说明、合同编号或适用范围")
    private String license;

    @Size(max = 50, message = "授权版本不能超过50个字符") private String licenseVersion;

    @ApiModelProperty("授权到期日，格式 yyyy-MM-dd")
    private java.time.LocalDate expireAt;

    @ApiModelProperty("启用状态")
    private Integer enable;

    @ApiModelProperty("关联知识点ID，首项作为主知识点")
    private List<Long> knowledgePointIds;

    @ApiModelProperty("题目图片文件关联，数组顺序即展示顺序")
    private List<QuestionBankImageSaveReq> images;

    @ApiModelProperty("已人工确认重复题风险")
    private Boolean duplicateConfirmed;
}
