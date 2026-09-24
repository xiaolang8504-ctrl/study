package com.study.module.system.wrongquestion.dto.request;

import lombok.Data;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

/** 分层查看提示、解析或答案请求。 */
@Data
public class WrongQuestionAnswerLayerRevealReq {

    @NotNull(message = "错题ID不能为空")
    @Range(min = 1, message = "错题ID需大于{min}")
    private Long wrongQuestionId;
    @NotBlank(message = "查看层级不能为空")
    @Pattern(regexp = "KEY_HINT|STEPS|COMMON_MISTAKE|ANALYSIS|ANSWER", message = "查看层级不正确")
    private String layer;
}
