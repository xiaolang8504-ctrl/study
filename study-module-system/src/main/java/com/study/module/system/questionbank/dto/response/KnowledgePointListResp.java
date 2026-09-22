package com.study.module.system.questionbank.dto.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.util.List;

/**
 * 知识点列表响应
 */
@Data
public class KnowledgePointListResp {

    @ApiModelProperty("主键ID")
    private Long id;

    @ApiModelProperty("父级知识点ID")
    private Long parentId;

    @ApiModelProperty("知识点编码")
    private String pointCode;

    @ApiModelProperty("知识点名称")
    private String pointName;

    @ApiModelProperty("年级字典键值")
    private String grade;

    @ApiModelProperty("科目字典键值")
    private String subject;

    @ApiModelProperty("排序值")
    private Integer sort;

    @ApiModelProperty("启用状态")
    private Integer enable;

    @ApiModelProperty("子知识点")
    private List<KnowledgePointListResp> children;
}
