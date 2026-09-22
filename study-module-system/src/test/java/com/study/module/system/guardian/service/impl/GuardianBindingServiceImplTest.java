package com.study.module.system.guardian.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.study.module.system.guardian.constants.GuardianBindingStatus;
import com.study.module.system.guardian.dto.response.GuardianBindingResp;
import com.study.module.system.guardian.entity.StudentGuardianRel;
import com.study.module.system.guardian.mapper.StudentGuardianRelMapper;
import com.study.module.system.user.entity.User;
import com.study.module.system.user.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * 家长数据域边界测试。
 */
@ExtendWith(MockitoExtension.class)
class GuardianBindingServiceImplTest {

    @Mock
    private StudentGuardianRelMapper studentGuardianRelMapper;
    @Mock
    private UserService userService;
    @InjectMocks
    private GuardianBindingServiceImpl guardianBindingService;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldOnlyGrantActiveGuardianBindingAccess() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(200L, null));
        when(studentGuardianRelMapper.selectCount(any(Wrapper.class))).thenReturn(1L);

        assertTrue(guardianBindingService.canCurrentGuardianAccessStudent(100L));
    }

    @Test
    void shouldDenyWhenNoActiveGuardianBindingExists() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(200L, null));
        when(studentGuardianRelMapper.selectCount(any(Wrapper.class))).thenReturn(0L);

        assertFalse(guardianBindingService.canCurrentGuardianAccessStudent(100L));
        assertFalse(guardianBindingService.canCurrentGuardianAccessStudent(null));
    }

    @Test
    void shouldExposeStudentLastLoginOnlyForAnActiveBinding() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(200L, null));
        StudentGuardianRel relation = new StudentGuardianRel();
        relation.setId(1L);
        relation.setStudentUserId(100L);
        relation.setGuardianUserId(200L);
        relation.setStatus(GuardianBindingStatus.ACTIVE);
        User student = new User();
        LocalDateTime lastLogin = LocalDateTime.of(2026, 9, 11, 9, 30);
        student.setRealName("学生A");
        student.setLastLoginTime(lastLogin);
        User guardian = new User();
        guardian.setRealName("家长A");
        when(studentGuardianRelMapper.selectList(any(Wrapper.class))).thenReturn(Collections.singletonList(relation));
        when(userService.getById(100L)).thenReturn(student);
        when(userService.getById(200L)).thenReturn(guardian);

        GuardianBindingResp response = guardianBindingService.currentGuardianBindingList().get(0);

        assertEquals(lastLogin, response.getStudentLastLoginTime());
    }
}
