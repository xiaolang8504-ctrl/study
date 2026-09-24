package com.study.module.system.questionbank.dto.response;

import io.swagger.annotations.ApiModelProperty;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 题库题目分页列表响应
 */
@Data
public class QuestionBankPageListResp {
    private String textbookVersion;
    private String chapterName;
    private String region;
    private Integer examYear;
    private String paperType;

    @ApiModelProperty("题库题目ID")
    private Long id;

    @ApiModelProperty("年级字典键值")
    private String grade;

    @ApiModelProperty("年级名称")
    private String gradeName;

    @ApiModelProperty("科目字典键值")
    private String subject;

    @ApiModelProperty("科目名称")
    private String subjectName;

    @ApiModelProperty("题型字典键值")
    private String questionType;

    @ApiModelProperty("题型名称")
    private String questionTypeName;

    @ApiModelProperty("题目标题")
    private String questionTitle;

    @ApiModelProperty("题目内容")
    private String questionContent;

    @ApiModelProperty("内容格式：TEXT或LATEX")
    private String contentFormat;

    @ApiModelProperty("题目图片地址，多个用逗号分隔")
    private String imageUrls;

    @ApiModelProperty("题目选项JSON")
    private String optionsJson;

    @ApiModelProperty("正确答案")
    private String correctAnswer;

    @ApiModelProperty("判题模式")
    private String judgeMode;

    @ApiModelProperty("答案解析")
    private String analysis;

    @ApiModelProperty("题目难度，取值1至5")
    private Integer difficulty;

    @ApiModelProperty("题目来源字典键值")
    private String source;

    @ApiModelProperty("题目来源名称")
    private String sourceName;

    @ApiModelProperty("内容提供方或版权方")
    private String provider;

    @ApiModelProperty("授权到期日")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private java.time.LocalDate expireAt;

    @ApiModelProperty("审核状态")
    private Integer reviewStatus;

    @ApiModelProperty("审核备注")
    private String reviewRemark;

    @ApiModelProperty("启用状态")
    private Integer enable;

    @ApiModelProperty("关联知识点ID列表，首项为主知识点")
    private List<Long> knowledgePointIds;

    @ApiModelProperty("知识点名称列表")
    private List<String> knowledgePointNames;

    @ApiModelProperty("题目图片列表")
    private List<QuestionBankImageListResp> images;

    @ApiModelProperty("创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @ApiModelProperty("审核时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime reviewTime;
}
