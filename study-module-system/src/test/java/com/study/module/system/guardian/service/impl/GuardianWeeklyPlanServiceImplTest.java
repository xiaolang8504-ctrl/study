package com.study.module.system.guardian.service.impl;

import com.study.common.core.exception.LogicException;
import com.study.module.system.guardian.constants.GuardianWeeklyPlanTodoStatus;
import com.study.module.system.guardian.entity.GuardianWeeklyPlan;
import com.study.module.system.guardian.mapper.GuardianWeeklyPlanMapper;
import com.study.module.system.guardian.service.GuardianBindingService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** 家长周计划不得绕过有效绑定或代替学生确认。 */
@ExtendWith(MockitoExtension.class)
class GuardianWeeklyPlanServiceImplTest {

    @Mock
    private GuardianWeeklyPlanMapper guardianWeeklyPlanMapper;
    @Mock
    private GuardianBindingService guardianBindingService;
    @InjectMocks
    private GuardianWeeklyPlanServiceImpl guardianWeeklyPlanService;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldRejectStudentConfirmationAfterBindingWasRevoked() {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(100L, null));
        GuardianWeeklyPlan plan = new GuardianWeeklyPlan();
        plan.setId(1L);
        plan.setStudentUserId(100L);
        plan.setGuardianUserId(200L);
        plan.setTodoStatus(GuardianWeeklyPlanTodoStatus.PENDING_STUDENT_CONFIRM);
        when(guardianWeeklyPlanMapper.selectById(1L)).thenReturn(plan);
        when(guardianBindingService.hasActiveBinding(100L, 200L)).thenReturn(false);

        assertThrows(LogicException.class,
                () -> guardianWeeklyPlanService.confirmGuardianWeeklyPlanTodo(1L, true));

        verify(guardianWeeklyPlanMapper, never()).updateById(any(GuardianWeeklyPlan.class));
    }
}
