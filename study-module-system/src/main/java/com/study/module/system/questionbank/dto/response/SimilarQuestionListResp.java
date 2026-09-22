package com.study.module.system.questionbank.dto.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.util.List;

/**
 * 学生相似题列表响应，提交作答前不返回答案和解析
 */
@Data
public class SimilarQuestionListResp {

    @ApiModelProperty("推荐记录ID")
    private Long recommendationId;

    @ApiModelProperty("题库题目ID")
    private Long questionId;

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

    @ApiModelProperty("题型字典键值")
    private String questionType;

    @ApiModelProperty("判题模式：AUTO自动判题，SELF查看答案后自评")
    private String judgeMode;

    @ApiModelProperty("题目难度，取值1至5")
    private Integer difficulty;

    @ApiModelProperty("知识点名称列表")
    private List<String> knowledgePointNames;

    @ApiModelProperty("推荐分数")
    private Double recommendScore;

    @ApiModelProperty("匹配类型")
    private String matchType;

    @ApiModelProperty("推荐原因")
    private String recommendReason;

    @ApiModelProperty("推荐实验分组")
    private String experimentGroup;

    @ApiModelProperty("题目图片列表")
    private List<QuestionBankImageListResp> images;
}
