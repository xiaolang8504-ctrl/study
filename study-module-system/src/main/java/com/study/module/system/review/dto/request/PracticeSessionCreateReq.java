package com.study.module.system.review.dto.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import javax.validation.Valid;
import java.util.List;

/**
 * 创建专项练习请求
 */
@Data
@ApiModel("创建专项练习请求")
public class PracticeSessionCreateReq {

    @ApiModelProperty("练习卷名称")
    @Size(max = 100, message = "练习卷名称不能超过100个字符")
    private String title;

    @ApiModelProperty(value = "练习类型：KNOWLEDGE知识点，ERROR_LABEL错因，TYPICAL典型错题", required = true)
    @NotBlank(message = "练习类型不能为空")
    private String practiceType;

    @ApiModelProperty("题目来源：WRONG_QUESTION错题本，QUESTION_BANK题库，MIXED混合")
    private String questionSource;

    @ApiModelProperty("科目")
    private String subject;

    @ApiModelProperty("知识点")
    private String learningPoint;

    @ApiModelProperty("错因标签")
    private String errorLabel;

    @ApiModelProperty("难度")
    private Integer difficulty;

    @ApiModelProperty("教材版本；题库选题时精确匹配")
    @Size(max = 100, message = "教材版本不能超过100个字符")
    private String textbookVersion;

    @ApiModelProperty("教材章节；题库选题时包含匹配")
    @Size(max = 200, message = "章节不能超过200个字符")
    private String chapterName;

    @ApiModelProperty("真题地区；题库选题时精确匹配")
    @Size(max = 100, message = "地区不能超过100个字符")
    private String region;

    @ApiModelProperty("真题年份；题库选题时精确匹配")
    @Min(value = 1900, message = "年份不正确")
    @Max(value = 2100, message = "年份不正确")
    private Integer examYear;

    @ApiModelProperty("卷型；题库选题时精确匹配")
    @Size(max = 50, message = "卷型不能超过50个字符")
    private String paperType;

    @ApiModelProperty("是否包含已掌握错题，默认否")
    private Boolean includeMastered = false;

    @ApiModelProperty(value = "题目数量", required = true)
    @NotNull(message = "题目数量不能为空")
    @Min(value = 1, message = "题目数量不能小于1")
    @Max(value = 50, message = "题目数量不能超过50")
    private Integer questionCount;

    @ApiModelProperty("每题作答留白行数")
    @Min(value = 1, message = "作答留白不能小于1行")
    @Max(value = 10, message = "作答留白不能超过10行")
    private Integer blankLineCount = 3;

    @ApiModelProperty("答案位置：AFTER_EACH逐题显示，END卷末集中显示")
    @Pattern(regexp = "AFTER_EACH|END", message = "答案位置不正确")
    private String answerPosition = "END";

    @ApiModelProperty("题面图片：ORIGINAL原图，GRAYSCALE灰度预览，TEXT_ONLY仅文字")
    @Pattern(regexp = "ORIGINAL|GRAYSCALE|TEXT_ONLY", message = "题面图片模式不正确")
    private String imageMode = "ORIGINAL";

    @ApiModelProperty("排版栏数：1单栏，2双栏")
    @Min(value = 1, message = "排版栏数不能小于1")
    @Max(value = 2, message = "排版栏数不能超过2")
    private Integer columnCount = 1;

    @ApiModelProperty("手工选题列表；传入后按列表顺序组卷")
    @Valid
    @Size(max = 50, message = "手工选题不能超过50道")
    private List<PracticeQuestionSelectReq> selectedQuestionList;
}
