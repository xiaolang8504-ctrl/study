package com.study.module.system.wrongquestion.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 错题详情响应类
 */
@Data
public class WrongQuestionDetailResp {

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

    @ApiModelProperty("题目内容")
    private String questionContent;

    @ApiModelProperty("错误答案")
    private String wrongAnswer;

    @ApiModelProperty("正确答案")
    private String correctAnswer;

    @ApiModelProperty("错误原因")
    private String wrongReason;

    @ApiModelProperty("题目解析")
    private String analysis;

    @ApiModelProperty("知识点")
    private String learningPoint;

    @ApiModelProperty("标准知识点ID列表")
    private List<Long> knowledgePointIds;

    @ApiModelProperty("标准知识点名称列表")
    private List<String> knowledgePointNames;

    @ApiModelProperty("错误类型标签")
    private String errorLabels;

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

    @ApiModelProperty("采集任务ID")
    private Long captureTaskId;
    @ApiModelProperty("采集页面ID")
    private Long capturePageId;
    @ApiModelProperty("采集题块ID")
    private Long captureRegionId;
    @ApiModelProperty("原文件页码")
    private Integer captureSourcePageNo;
    @ApiModelProperty("题块归一化坐标")
    private Integer captureLeftPosition;
    private Integer captureTopPosition;
    private Integer captureWidth;
    private Integer captureHeight;

    @ApiModelProperty("状态: 0待改, 1已改, 2已掌握, 3已归档")
    private Integer status;

    @ApiModelProperty("最新订正记录")
    private WrongQuestionCorrectionRecordResp latestCorrectionRecord;

    @ApiModelProperty("订正记录列表")
    private List<WrongQuestionCorrectionRecordResp> correctionRecordList;

    @ApiModelProperty("学习证据时间线")
    private List<WrongQuestionTimelineResp> timelineList;

    @ApiModelProperty("创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @ApiModelProperty("更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
