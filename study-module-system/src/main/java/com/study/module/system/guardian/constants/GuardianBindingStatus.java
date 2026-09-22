package com.study.module.system.guardian.constants;

/**
 * 家长—学生绑定状态。
 */
public interface GuardianBindingStatus {

    /** 等待家长接受邀请码。 */
    int PENDING_GUARDIAN_ACCEPT = 0;
    /** 家长已接受，等待学生确认。 */
    int PENDING_STUDENT_CONFIRM = 1;
    /** 双方确认后的有效监护关系。 */
    int ACTIVE = 2;
    /** 任一方解绑后的失效关系。 */
    int REVOKED = 3;
}
