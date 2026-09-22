package com.study.module.system.questionbank.dto.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import javax.validation.constraints.NotBlank;

/**
 * 知识点保存请求
 */
@Data
public class KnowledgePointSaveReq {

    @ApiModelProperty("知识点ID")
    private Long id;

    @ApiModelProperty("父级知识点ID")
    private Long parentId;

    @ApiModelProperty("知识点编码")
    @NotBlank(message = "知识点编码不能为空")
    private String pointCode;

    @ApiModelProperty("知识点名称")
    @NotBlank(message = "知识点名称不能为空")
    private String pointName;

    @ApiModelProperty("年级字典键值")
    @NotBlank(message = "年级不能为空")
    private String grade;

    @ApiModelProperty("科目字典键值")
    @NotBlank(message = "科目不能为空")
    private String subject;

    @ApiModelProperty("排序值")
    private Integer sort;

    @ApiModelProperty("启用状态")
    private Integer enable;
}
