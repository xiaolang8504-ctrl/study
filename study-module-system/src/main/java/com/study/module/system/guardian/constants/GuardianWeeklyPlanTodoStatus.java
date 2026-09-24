package com.study.module.system.guardian.constants;

/** 家长周计划转为学生待办时的确认状态。 */
public interface GuardianWeeklyPlanTodoStatus {
    int NOT_REQUESTED = 0;
    int PENDING_STUDENT_CONFIRM = 1;
    int STUDENT_CONFIRMED = 2;
    int STUDENT_DECLINED = 3;
}
