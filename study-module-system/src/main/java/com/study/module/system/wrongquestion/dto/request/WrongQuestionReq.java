package com.study.module.system.wrongquestion.dto.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 错题归档请求基类
 */
@Data
public class WrongQuestionReq {

    @ApiModelProperty(value = "年级字典键值", required = true)
    @NotBlank(message = "年级不能为空")
    private String grade;

    @ApiModelProperty(value = "科目字典键值", required = true)
    @NotBlank(message = "科目不能为空")
    private String subject;

    @ApiModelProperty(value = "题目类型字典键值", required = true)
    @NotBlank(message = "题目类型不能为空")
    private String questionType;

    @ApiModelProperty(value = "题目标题", required = true)
    @NotBlank(message = "题目标题不能为空")
    private String questionTitle;

    @ApiModelProperty(value = "题目内容", required = true)
    @NotBlank(message = "题目内容不能为空")
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

    @ApiModelProperty("错误类型标签，多个标签用逗号分隔")
    private String errorLabels;

    @ApiModelProperty(value = "来源字典键值", required = true)
    @NotBlank(message = "来源不能为空")
    private String source;

    @ApiModelProperty("默认题目图片地址，选择题时为A选项图片地址")
    private String imageUrl;

    @ApiModelProperty("B选项图片地址")
    private String imageUrl2;

    @ApiModelProperty("C选项图片地址")
    private String imageUrl3;

    @ApiModelProperty("D选项图片地址")
    private String imageUrl4;

    @ApiModelProperty(value = "状态: 0待改, 1已改, 2已掌握, 3已归档", required = true)
    @NotNull(message = "状态不能为空")
    @Range(min = 0, max = 3, message = "状态值必须在{min}-{max}之间")
    private Integer status;
}
