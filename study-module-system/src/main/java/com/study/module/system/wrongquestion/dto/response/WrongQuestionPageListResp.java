package com.study.module.system.wrongquestion.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 错题分页列表响应类
 */
@Data
public class WrongQuestionPageListResp {

    @ApiModelProperty("错题ID")
    private Long id;

    @ApiModelProperty("年级字典键值")
    private String grade;

    @ApiModelProperty("年级名称")
    private String gradeName;

    @ApiModelProperty("科目字典键值")
    private String subject;

    @ApiModelProperty("科目名称")
    private String subjectName;

    @ApiModelProperty("题目类型字典键值")
    private String questionType;

    @ApiModelProperty("题目类型名称")
    private String questionTypeName;

    @ApiModelProperty("题目标题")
    private String questionTitle;

    @ApiModelProperty("错误答案")
    private String wrongAnswer;

    @ApiModelProperty("正确答案")
    private String correctAnswer;

    @ApiModelProperty("知识点")
    private String learningPoint;

    @ApiModelProperty("标准知识点名称列表")
    private List<String> knowledgePointNames;

    @ApiModelProperty("错误类型标签")
    private String errorLabels;

    @ApiModelProperty("结构化错因编码")
    private String errorCauseCodes;

    @ApiModelProperty("能力层级：FOUNDATION、APPLICATION、COMPREHENSIVE")
    private String abilityLevel;

    @ApiModelProperty("教材版本")
    private String textbookVersion;

    @ApiModelProperty("章节")
    private String chapterName;

    @ApiModelProperty("个人标签")
    private List<String> tagNames;

    @ApiModelProperty("是否收藏：0否，1是")
    private Integer favorite;

    @ApiModelProperty("整理优先级：0-5")
    private Integer priorityLevel;

    @ApiModelProperty("累计错误次数")
    private Integer wrongOccurrenceCount;

    @ApiModelProperty("最近错误时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime latestWrongTime;

    @ApiModelProperty("最近订正时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime latestCorrectionTime;

    @ApiModelProperty("当前复习状态")
    private String reviewStatus;

    @ApiModelProperty("来源字典键值")
    private String source;

    @ApiModelProperty("来源名称")
    private String sourceName;

    @ApiModelProperty("默认题目图片地址，选择题时为A选项图片地址")
    private String imageUrl;

    @ApiModelProperty("B选项图片地址")
    private String imageUrl2;

    @ApiModelProperty("C选项图片地址")
    private String imageUrl3;

    @ApiModelProperty("D选项图片地址")
    private String imageUrl4;

    @ApiModelProperty("状态: 0待改, 1已改, 2已掌握, 3已归档")
    private Integer status;

    @ApiModelProperty("创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
