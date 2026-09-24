package com.study.module.system.guardian.service;

import com.study.module.system.guardian.dto.request.GuardianWeeklyPlanSaveReq;
import com.study.module.system.guardian.dto.response.GuardianWeeklyPlanResp;

import java.util.List;

/** 家长按学生维护周计划，学生确认后才成为其待办。 */
public interface GuardianWeeklyPlanService {
    void saveGuardianWeeklyPlan(GuardianWeeklyPlanSaveReq request);
    void requestGuardianWeeklyPlanTodo(Long planId);
    void confirmGuardianWeeklyPlanTodo(Long planId, boolean confirmed);
    List<GuardianWeeklyPlanResp> guardianWeeklyPlanList(Long studentUserId);
}
