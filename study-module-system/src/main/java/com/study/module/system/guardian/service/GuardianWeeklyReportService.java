package com.study.module.system.guardian.service;
import com.study.module.system.guardian.dto.response.GuardianWeeklyReportResp;
public interface GuardianWeeklyReportService {
    GuardianWeeklyReportResp guardianWeeklyReport(Long studentUserId);
    GuardianWeeklyReportResp buildWeeklyReport(Long studentUserId);
}
