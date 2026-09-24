package com.study.module.system.guardian.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.study.common.core.exception.LogicException;
import com.study.module.system.guardian.dto.request.GuardianAssistedCaptureCreateReq;
import com.study.module.system.guardian.entity.GuardianAssistedCapture;
import com.study.module.system.guardian.mapper.GuardianAssistedCaptureMapper;
import com.study.module.system.guardian.service.GuardianBindingService;
import com.study.module.system.wrongquestion.service.QuestionCaptureService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** 家长解绑后，历史代上传不能再被列出、确认或撤销。 */
@ExtendWith(MockitoExtension.class)
class GuardianAssistedCaptureServiceImplTest {
    @Mock private GuardianAssistedCaptureMapper guardianAssistedCaptureMapper;
    @Mock private GuardianBindingService guardianBindingService;
    @Mock private QuestionCaptureService questionCaptureService;
    @InjectMocks private GuardianAssistedCaptureServiceImpl service;

    @AfterEach
    void clear() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldRejectAssistedCaptureWithoutActiveBinding() {
        authenticate(9L);
        GuardianAssistedCaptureCreateReq request = new GuardianAssistedCaptureCreateReq();
        request.setStudentUserId(10L);
        request.setImageFileIds(Collections.singletonList(1L));
        when(guardianBindingService.canCurrentGuardianAccessStudent(10L)).thenReturn(false);

        assertThrows(LogicException.class, () -> service.createGuardianAssistedCapture(request));
    }

    @Test
    void studentCannotConfirmFormerGuardiansCapture() {
        authenticate(10L);
        when(guardianAssistedCaptureMapper.selectById(5L)).thenReturn(pendingCapture());
        when(guardianBindingService.hasActiveBinding(10L, 9L)).thenReturn(false);

        assertThrows(LogicException.class, () -> service.confirmGuardianAssistedCapture(5L));
        verify(questionCaptureService, never()).createQuestionCaptureTaskByUserId(any(), any());
    }

    @Test
    void formerGuardianCannotRevokePendingCapture() {
        authenticate(9L);
        when(guardianAssistedCaptureMapper.selectById(5L)).thenReturn(pendingCapture());
        when(guardianBindingService.hasActiveBinding(10L, 9L)).thenReturn(false);

        assertThrows(LogicException.class, () -> service.revokeGuardianAssistedCapture(5L));
        verify(guardianAssistedCaptureMapper, never()).updateById(any(GuardianAssistedCapture.class));
    }

    @Test
    void formerGuardianListOnlyQueriesTheirOwnStudentRecords() {
        authenticate(9L);
        when(guardianBindingService.activeStudentIdsOfCurrentGuardian()).thenReturn(Collections.emptyList());
        when(guardianAssistedCaptureMapper.selectList(any(Wrapper.class))).thenReturn(Collections.emptyList());

        service.guardianAssistedCaptureList();

        verify(guardianBindingService).activeStudentIdsOfCurrentGuardian();
        verify(guardianAssistedCaptureMapper).selectList(any(Wrapper.class));
    }

    private GuardianAssistedCapture pendingCapture() {
        GuardianAssistedCapture capture = new GuardianAssistedCapture();
        capture.setId(5L);
        capture.setGuardianUserId(9L);
        capture.setStudentUserId(10L);
        capture.setStatus(0);
        return capture;
    }

    private void authenticate(Long userId) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userId, null));
    }
}
