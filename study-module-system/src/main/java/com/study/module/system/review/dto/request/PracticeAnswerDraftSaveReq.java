package com.study.module.system.review.dto.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 保存专项练习草稿请求
 */
@Data
@ApiModel("保存专项练习草稿请求")
public class PracticeAnswerDraftSaveReq {

    @ApiModelProperty(value = "练习会话ID", required = true)
    @NotNull(message = "练习会话ID不能为空")
    private Long sessionId;

    @ApiModelProperty(value = "草稿列表", required = true)
    @Valid
    @NotEmpty(message = "草稿列表不能为空")
    private List<PracticeAnswerDraftItemReq> draftList;
}
