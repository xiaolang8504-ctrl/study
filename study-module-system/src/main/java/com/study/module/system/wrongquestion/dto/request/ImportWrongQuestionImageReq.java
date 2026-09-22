package com.study.module.system.wrongquestion.dto.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * A4图片导入错题请求类
 */
@Data
public class ImportWrongQuestionImageReq {

    @ApiModelProperty(value = "年级字典键值", required = true)
    @NotBlank(message = "年级不能为空")
    private String grade;

    @ApiModelProperty(value = "科目字典键值", required = true)
    @NotBlank(message = "科目不能为空")
    private String subject;

    @ApiModelProperty("题目类型字典键值，未传时按A4文件题型样式自动识别")
    private String questionType;

    @ApiModelProperty("知识点")
    private String learningPoint;

    @ApiModelProperty("错误类型标签，多个标签用逗号分隔")
    private String errorLabels;

    @ApiModelProperty(value = "来源字典键值", required = true)
    @NotBlank(message = "来源不能为空")
    private String source;

    @ApiModelProperty(value = "A4图片文件ID", required = true)
    @NotNull(message = "A4图片文件ID不能为空")
    @Range(min = 1, message = "A4图片文件ID需大于{min}")
    private Long imageFileId;

    @ApiModelProperty("状态: 0待改, 1已改, 2已掌握, 3已归档")
    @NotNull(message = "状态不能为空")
    @Range(min = 0, max = 3, message = "状态值必须在{min}-{max}之间")
    private Integer status = 0;
}
