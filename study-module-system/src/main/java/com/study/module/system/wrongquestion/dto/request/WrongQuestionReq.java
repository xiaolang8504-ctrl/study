package com.study.module.system.wrongquestion.dto.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
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

    @ApiModelProperty("内容格式：TEXT普通文本，LATEX公式，RICH_TEXT基础富文本")
    @Pattern(regexp = "TEXT|LATEX|RICH_TEXT", message = "内容格式不正确")
    private String contentFormat = "TEXT";

    @ApiModelProperty("结构化选项JSON")
    private String optionsJson;

    @ApiModelProperty("错误答案")
    private String wrongAnswer;

    @ApiModelProperty("正确答案")
    private String correctAnswer;

    @ApiModelProperty("错误原因")
    private String wrongReason;

    @ApiModelProperty("题目解析")
    private String analysis;

    @ApiModelProperty("关键提示，只给出思考方向")
    private String keyHint;

    @ApiModelProperty("分步解题过程")
    private String solutionSteps;

    @ApiModelProperty("本题常见易错点")
    private String commonMistake;

    @ApiModelProperty("知识点")
    private String learningPoint;

    @ApiModelProperty("标准知识点ID列表")
    private List<Long> knowledgePointIds;

    @ApiModelProperty("错误类型标签，多个标签用逗号分隔")
    private String errorLabels;

    @ApiModelProperty("结构化错因编码，多个用逗号分隔：READING审题、CONCEPT概念、METHOD方法、CALCULATION计算、EXPRESSION表达")
    @Pattern(regexp = "^$|^(READING|CONCEPT|METHOD|CALCULATION|EXPRESSION)(,(READING|CONCEPT|METHOD|CALCULATION|EXPRESSION))*$", message = "结构化错因编码不正确")
    private String errorCauseCodes;

    @ApiModelProperty("能力层级：FOUNDATION基础、APPLICATION应用、COMPREHENSIVE综合")
    @Pattern(regexp = "FOUNDATION|APPLICATION|COMPREHENSIVE", message = "能力层级不正确")
    private String abilityLevel;

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
