package com.study.module.system.wrongquestion.dto.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 错题知识点绑定请求类
 */
@Data
public class WrongQuestionKnowledgePointBindReq {

    @ApiModelProperty(value = "错题ID", required = true)
    @NotNull(message = "错题ID不能为空")
    @Range(min = 1, message = "错题ID需大于{min}")
    private Long id;

    @ApiModelProperty("标准知识点ID列表")
    private List<Long> knowledgePointIds;
}
