package com.study.module.system.file.dto.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * 题目孤立图片删除请求
 */
@Data
public class QuestionImageDeleteReq {

    @ApiModelProperty(value = "文件ID列表", required = true)
    @NotEmpty(message = "请选择需要删除的图片")
    private List<Integer> fileIds;
}
