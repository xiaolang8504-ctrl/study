package com.study.module.system.guardian.service.impl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.common.core.enums.ErrorCodeConstants;
import com.study.common.core.exception.LogicException;
import com.study.module.system.guardian.dto.request.GuardianWeeklyReportSubscriptionReq;
import com.study.module.system.guardian.entity.GuardianWeeklyReportSubscription;
import com.study.module.system.guardian.mapper.GuardianWeeklyReportSubscriptionMapper;
import com.study.module.system.guardian.service.GuardianAccessAuditService;
import com.study.module.system.guardian.service.GuardianBindingService;
import com.study.module.system.guardian.service.GuardianWeeklyReportSubscriptionService;
import com.yunshang.budget.common.security.utils.AccountUtils;
import org.springframework.beans.factory.annotation.Autowired; import org.springframework.stereotype.Service;
import java.time.LocalDateTime; import java.util.List;
@Service public class GuardianWeeklyReportSubscriptionServiceImpl extends ServiceImpl<GuardianWeeklyReportSubscriptionMapper, GuardianWeeklyReportSubscription> implements GuardianWeeklyReportSubscriptionService {
    @Autowired private GuardianBindingService guardianBindingService; @Autowired private GuardianAccessAuditService guardianAccessAuditService;
    @Override public void updateGuardianWeeklyReportSubscription(GuardianWeeklyReportSubscriptionReq request) {
        Long guardianId = AccountUtils.getUserId(); if (!guardianBindingService.canCurrentGuardianAccessStudent(request.getStudentUserId())) throw new LogicException(ErrorCodeConstants.GUARDIAN_BINDING_NOT_EXIST);
        GuardianWeeklyReportSubscription entity = getOne(new LambdaQueryWrapper<GuardianWeeklyReportSubscription>().eq(GuardianWeeklyReportSubscription::getGuardianUserId, guardianId).eq(GuardianWeeklyReportSubscription::getStudentUserId, request.getStudentUserId()));
        LocalDateTime now = LocalDateTime.now(); if (entity == null) { entity = new GuardianWeeklyReportSubscription(); entity.setGuardianUserId(guardianId); entity.setStudentUserId(request.getStudentUserId()); entity.setCreateTime(now); }
        entity.setSiteNotificationEnabled(request.getSiteNotificationEnabled()); entity.setEmailEnabled(request.getEmailEnabled()); entity.setUpdateTime(now); saveOrUpdate(entity);
        guardianAccessAuditService.record(null, request.getStudentUserId(), guardianId, guardianId, "WEEKLY_REPORT_PREFERENCE_UPDATED", "SUCCESS", "更新周报提醒偏好");
    }
    @Override public List<GuardianWeeklyReportSubscription> guardianWeeklyReportSubscriptionList() { return list(new LambdaQueryWrapper<GuardianWeeklyReportSubscription>().eq(GuardianWeeklyReportSubscription::getGuardianUserId, AccountUtils.getUserId())); }
}
