package com.study.module.system.guardian.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.study.module.system.guardian.dto.request.GuardianAssistedCaptureCreateReq;
import com.study.module.system.guardian.dto.response.GuardianAssistedCaptureResp;
import com.study.module.system.guardian.entity.GuardianAssistedCapture;

import java.util.List;

/** 家长受控代上传服务。 */
public interface GuardianAssistedCaptureService extends IService<GuardianAssistedCapture> {
    Long createGuardianAssistedCapture(GuardianAssistedCaptureCreateReq request);
    Long confirmGuardianAssistedCapture(Long id);
    void revokeGuardianAssistedCapture(Long id);
    List<GuardianAssistedCaptureResp> guardianAssistedCaptureList();
}
