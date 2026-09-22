package com.study.module.system.guardian.service.impl;
import com.study.common.core.exception.LogicException;
import com.study.module.system.guardian.dto.request.GuardianAssistedCaptureCreateReq;
import com.study.module.system.guardian.mapper.GuardianAssistedCaptureMapper;
import com.study.module.system.guardian.service.GuardianBindingService;
import org.junit.jupiter.api.AfterEach; import org.junit.jupiter.api.Test; import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks; import org.mockito.Mock; import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken; import org.springframework.security.core.context.SecurityContextHolder;
import java.util.Collections; import static org.junit.jupiter.api.Assertions.assertThrows; import static org.mockito.Mockito.when;
/** 未建立有效绑定的账号不能为任意学生代上传。 */
@ExtendWith(MockitoExtension.class) class GuardianAssistedCaptureServiceImplTest {
 @Mock private GuardianAssistedCaptureMapper guardianAssistedCaptureMapper; @Mock private GuardianBindingService guardianBindingService; @InjectMocks private GuardianAssistedCaptureServiceImpl service;
 @AfterEach void clear() { SecurityContextHolder.clearContext(); }
 @Test void shouldRejectAssistedCaptureWithoutActiveBinding() { SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(9L, null)); GuardianAssistedCaptureCreateReq request = new GuardianAssistedCaptureCreateReq(); request.setStudentUserId(10L); request.setImageFileIds(Collections.singletonList(1L)); when(guardianBindingService.canCurrentGuardianAccessStudent(10L)).thenReturn(false); assertThrows(LogicException.class, () -> service.createGuardianAssistedCapture(request)); }
}
