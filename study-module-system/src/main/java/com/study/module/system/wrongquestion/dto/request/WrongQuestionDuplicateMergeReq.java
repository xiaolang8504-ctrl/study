package com.study.module.system.wrongquestion.dto.request;

import lombok.Data;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotNull;

/** 合并重复错题请求。 */
@Data
public class WrongQuestionDuplicateMergeReq {
    @NotNull(message = "重复关系ID不能为空")
    @Range(min = 1, message = "重复关系ID需大于{min}")
    private Long relationId;
    @NotNull(message = "保留错题ID不能为空")
    @Range(min = 1, message = "保留错题ID需大于{min}")
    private Long keepQuestionId;
}
