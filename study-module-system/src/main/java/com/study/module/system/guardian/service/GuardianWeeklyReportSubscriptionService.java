package com.study.module.system.guardian.service;
import com.baomidou.mybatisplus.extension.service.IService;
import com.study.module.system.guardian.dto.request.GuardianWeeklyReportSubscriptionReq;
import com.study.module.system.guardian.entity.GuardianWeeklyReportSubscription;
import java.util.List;
public interface GuardianWeeklyReportSubscriptionService extends IService<GuardianWeeklyReportSubscription> {
    void updateGuardianWeeklyReportSubscription(GuardianWeeklyReportSubscriptionReq request);
    void unsubscribeGuardianWeeklyReport(Long studentUserId);
    List<GuardianWeeklyReportSubscription> guardianWeeklyReportSubscriptionList();
}
