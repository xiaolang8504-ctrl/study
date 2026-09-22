package com.study.module.system.review.dto.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 专项练习题目响应
 */
@Data
public class PracticeQuestionResp {

    @ApiModelProperty("练习题目ID")
    private Long sessionQuestionId;

    @ApiModelProperty("错题ID")
    private Long wrongQuestionId;

    @ApiModelProperty("题目来源")
    private String questionSource;

    @ApiModelProperty("入选依据")
    private String sourceReason;

    @ApiModelProperty("题库题目ID")
    private Long bankQuestionId;

    @ApiModelProperty("题目标题")
    private String questionTitle;

    @ApiModelProperty("题目内容")
    private String questionContent;

    @ApiModelProperty("科目名称")
    private String subjectName;

    @ApiModelProperty("题型名称")
    private String questionTypeName;

    @ApiModelProperty("知识点")
    private String learningPoint;

    @ApiModelProperty("错因标签快照")
    private String errorLabelSnapshot;

    @ApiModelProperty("难度")
    private Integer difficulty;

    @ApiModelProperty("题目图片")
    private String imageUrl;

    @ApiModelProperty("题目图片地址，多个用逗号分隔")
    private String imageUrls;

    @ApiModelProperty("B选项图片")
    private String imageUrl2;

    @ApiModelProperty("C选项图片")
    private String imageUrl3;

    @ApiModelProperty("D选项图片")
    private String imageUrl4;

    @ApiModelProperty("内容格式")
    private String contentFormat;

    @ApiModelProperty("选项JSON")
    private String optionsJson;

    @ApiModelProperty("判题模式")
    private String judgeMode;

    @ApiModelProperty("是否由系统自动判题")
    private Boolean autoJudge;

    @ApiModelProperty("是否已作答")
    private Boolean answered;

    @ApiModelProperty("学生答案")
    private String studentAnswer;

    @ApiModelProperty("是否正确")
    private Integer isCorrect;

    @ApiModelProperty("正确答案")
    private String correctAnswer;

    @ApiModelProperty("解析")
    private String analysis;

    @ApiModelProperty("作答用时，秒")
    private Integer durationSeconds;

    @ApiModelProperty("作答时间")
    private LocalDateTime answerTime;
}
