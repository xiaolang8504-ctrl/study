package com.study.module.system.guardian.service;

import com.study.module.system.guardian.dto.response.GuardianStudentOverviewResp;

/** 家长只读学习概览服务。 */
public interface GuardianOverviewService {

    GuardianStudentOverviewResp studentOverview(Long studentUserId);

    GuardianStudentOverviewResp exportStudentOverview(Long studentUserId);
}
