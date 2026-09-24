package com.study.module.system.wrongquestion.dto.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/** 保存错题列表筛选条件。 */
@Data
public class WrongQuestionSavedFilterSaveReq {

    @ApiModelProperty("已有筛选ID；传入时覆盖同一筛选")
    private Long id;

    @NotBlank(message = "筛选名称不能为空")
    @Size(max = 50, message = "筛选名称不能超过50个字符")
    private String filterName;

    @NotBlank(message = "筛选条件不能为空")
    @Size(max = 4000, message = "筛选条件过长")
    private String filterJson;

    private Integer sortNo;
    private Integer defaultFlag;
}
