package com.study.module.system.guardian.dto.request;

import lombok.Data;

import javax.validation.constraints.NotNull;

/** 请求或确认周计划转学生待办。 */
@Data
public class GuardianWeeklyPlanTodoReq {
    @NotNull(message = "周计划ID不能为空")
    private Long planId;

    /** 学生确认接口必传：true 接受，false 暂不接受。 */
    private Boolean confirmed;
}
