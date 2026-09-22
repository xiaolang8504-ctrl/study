package com.study.module.system.guardian.dto.request;

import com.study.module.system.wrongquestion.dto.request.QuestionCaptureTaskCreateReq;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/** 家长为已绑定学生发起的待确认采集请求。 */
@Data
public class GuardianAssistedCaptureCreateReq extends QuestionCaptureTaskCreateReq {
    @ApiModelProperty(value = "已绑定学生用户ID", required = true)
    @NotNull(message = "学生用户ID不能为空")
    private Long studentUserId;
}
