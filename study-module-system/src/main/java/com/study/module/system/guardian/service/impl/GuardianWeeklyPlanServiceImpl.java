package com.study.module.system.guardian.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.study.common.core.enums.ErrorCodeConstants;
import com.study.common.core.exception.LogicException;
import com.study.module.system.guardian.constants.GuardianWeeklyPlanTodoStatus;
import com.study.module.system.guardian.dto.request.GuardianWeeklyPlanSaveReq;
import com.study.module.system.guardian.dto.response.GuardianWeeklyPlanResp;
import com.study.module.system.guardian.dto.response.GuardianWeeklyReportResp;
import com.study.module.system.guardian.entity.GuardianWeeklyPlan;
import com.study.module.system.guardian.mapper.GuardianWeeklyPlanMapper;
import com.study.module.system.guardian.service.GuardianAccessAuditService;
import com.study.module.system.guardian.service.GuardianBindingService;
import com.study.module.system.guardian.service.GuardianWeeklyPlanService;
import com.study.module.system.guardian.service.GuardianWeeklyReportService;
import com.study.module.system.user.entity.User;
import com.study.module.system.user.service.UserService;
import com.yunshang.budget.common.security.utils.AccountUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 家长周计划实现。家长只能提出建议，学生必须明确确认后才显示为学生待办。
 */
@Service
public class GuardianWeeklyPlanServiceImpl implements GuardianWeeklyPlanService {

    @Autowired
    GuardianWeeklyPlanMapper guardianWeeklyPlanMapper;

    @Autowired
    GuardianBindingService guardianBindingService;

    @Autowired
    GuardianWeeklyReportService guardianWeeklyReportService;

    @Autowired
    GuardianAccessAuditService guardianAccessAuditService;

    @Autowired
    UserService userService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveGuardianWeeklyPlan(GuardianWeeklyPlanSaveReq request) {
        Long guardianUserId = AccountUtils.getUserId();
        requireGuardianBinding(request.getStudentUserId());
        LocalDate weekStartDate = normalizeWeekStart(request.getWeekStartDate());
        GuardianWeeklyPlan plan = request.getId() == null ? guardianWeeklyPlanMapper.selectOne(
                new LambdaQueryWrapper<GuardianWeeklyPlan>()
                        .eq(GuardianWeeklyPlan::getGuardianUserId, guardianUserId)
                        .eq(GuardianWeeklyPlan::getStudentUserId, request.getStudentUserId())
                        .eq(GuardianWeeklyPlan::getWeekStartDate, weekStartDate).last("LIMIT 1"))
                : guardianWeeklyPlanMapper.selectById(request.getId());
        if (request.getId() != null && (plan == null || !guardianUserId.equals(plan.getGuardianUserId())
                || !request.getStudentUserId().equals(plan.getStudentUserId()))) {
            throw new LogicException(ErrorCodeConstants.GUARDIAN_WEEKLY_PLAN_NOT_EXIST);
        }
        LocalDateTime now = LocalDateTime.now();
        if (plan == null) {
            plan = new GuardianWeeklyPlan();
            plan.setGuardianUserId(guardianUserId);
            plan.setStudentUserId(request.getStudentUserId());
            plan.setTodoStatus(GuardianWeeklyPlanTodoStatus.NOT_REQUESTED);
            plan.setCreateTime(now);
        } else if (!Integer.valueOf(GuardianWeeklyPlanTodoStatus.NOT_REQUESTED).equals(plan.getTodoStatus())) {
            // 已请求或已确认的计划一旦改动，旧确认不再适用于新内容，必须重新请求学生确认。
            plan.setTodoStatus(GuardianWeeklyPlanTodoStatus.NOT_REQUESTED);
            plan.setTodoRequestTime(null);
            plan.setStudentConfirmUserId(null);
            plan.setStudentConfirmTime(null);
        }
        plan.setWeekStartDate(weekStartDate);
        plan.setPlanTitle(request.getPlanTitle().trim());
        plan.setPlanContent(request.getPlanContent().trim());
        plan.setTargetReviewCount(request.getTargetReviewCount() == null ? 0 : request.getTargetReviewCount());
        GuardianWeeklyReportResp report = guardianWeeklyReportService.buildWeeklyReport(request.getStudentUserId());
        plan.setReportSuggestionSnapshot(report.getNextWeekSuggestion());
        plan.setUpdateTime(now);
        if (plan.getId() == null) {
            guardianWeeklyPlanMapper.insert(plan);
        } else {
            guardianWeeklyPlanMapper.updateById(plan);
        }
        guardianAccessAuditService.record(null, request.getStudentUserId(), guardianUserId, guardianUserId,
                "WEEKLY_PLAN_SAVED", "SUCCESS", "保存学生独立周计划=" + plan.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void requestGuardianWeeklyPlanTodo(Long planId) {
        Long guardianUserId = AccountUtils.getUserId();
        GuardianWeeklyPlan plan = requireGuardianPlan(planId, guardianUserId);
        requireGuardianBinding(plan.getStudentUserId());
        if (Integer.valueOf(GuardianWeeklyPlanTodoStatus.PENDING_STUDENT_CONFIRM).equals(plan.getTodoStatus())
                || Integer.valueOf(GuardianWeeklyPlanTodoStatus.STUDENT_CONFIRMED).equals(plan.getTodoStatus())) {
            throw new LogicException(ErrorCodeConstants.GUARDIAN_WEEKLY_PLAN_TODO_STATUS_INVALID);
        }
        plan.setTodoStatus(GuardianWeeklyPlanTodoStatus.PENDING_STUDENT_CONFIRM);
        plan.setTodoRequestTime(LocalDateTime.now());
        plan.setStudentConfirmUserId(null);
        plan.setStudentConfirmTime(null);
        plan.setUpdateTime(LocalDateTime.now());
        guardianWeeklyPlanMapper.updateById(plan);
        guardianAccessAuditService.record(null, plan.getStudentUserId(), guardianUserId, guardianUserId,
                "WEEKLY_PLAN_TODO_REQUESTED", "SUCCESS", "家长请求学生确认周计划待办=" + planId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmGuardianWeeklyPlanTodo(Long planId, boolean confirmed) {
        Long studentUserId = AccountUtils.getUserId();
        GuardianWeeklyPlan plan = guardianWeeklyPlanMapper.selectById(planId);
        if (plan == null || !studentUserId.equals(plan.getStudentUserId())
                || !guardianBindingService.hasActiveBinding(studentUserId, plan.getGuardianUserId())) {
            throw new LogicException(ErrorCodeConstants.GUARDIAN_WEEKLY_PLAN_NOT_EXIST);
        }
        if (!Integer.valueOf(GuardianWeeklyPlanTodoStatus.PENDING_STUDENT_CONFIRM).equals(plan.getTodoStatus())) {
            throw new LogicException(ErrorCodeConstants.GUARDIAN_WEEKLY_PLAN_TODO_STATUS_INVALID);
        }
        LocalDateTime now = LocalDateTime.now();
        plan.setTodoStatus(confirmed ? GuardianWeeklyPlanTodoStatus.STUDENT_CONFIRMED
                : GuardianWeeklyPlanTodoStatus.STUDENT_DECLINED);
        plan.setStudentConfirmUserId(studentUserId);
        plan.setStudentConfirmTime(now);
        plan.setUpdateTime(now);
        guardianWeeklyPlanMapper.updateById(plan);
        guardianAccessAuditService.record(null, studentUserId, plan.getGuardianUserId(), studentUserId,
                confirmed ? "WEEKLY_PLAN_TODO_CONFIRMED" : "WEEKLY_PLAN_TODO_DECLINED", "SUCCESS",
                "学生" + (confirmed ? "确认" : "暂不接受") + "周计划待办=" + planId);
    }

    @Override
    public List<GuardianWeeklyPlanResp> guardianWeeklyPlanList(Long studentUserId) {
        Long currentUserId = AccountUtils.getUserId();
        LambdaQueryWrapper<GuardianWeeklyPlan> query = new LambdaQueryWrapper<>();
        boolean currentStudent = currentUserId.equals(studentUserId);
        if (studentUserId != null && currentStudent) {
            query.eq(GuardianWeeklyPlan::getStudentUserId, currentUserId);
        } else if (studentUserId != null) {
            requireGuardianBinding(studentUserId);
            query.eq(GuardianWeeklyPlan::getGuardianUserId, currentUserId)
                    .eq(GuardianWeeklyPlan::getStudentUserId, studentUserId);
        } else {
            List<Long> activeStudentIds = guardianBindingService.activeStudentIdsOfCurrentGuardian();
            if (activeStudentIds.isEmpty()) {
                query.eq(GuardianWeeklyPlan::getStudentUserId, currentUserId);
            } else {
                query.eq(GuardianWeeklyPlan::getGuardianUserId, currentUserId)
                        .in(GuardianWeeklyPlan::getStudentUserId, activeStudentIds);
            }
        }
        List<GuardianWeeklyPlan> plans = guardianWeeklyPlanMapper.selectList(query
                .orderByDesc(GuardianWeeklyPlan::getWeekStartDate, GuardianWeeklyPlan::getId));
        return plans.stream().filter(this::hasActiveBinding).map(this::toResponse).collect(Collectors.toList());
    }

    private GuardianWeeklyPlan requireGuardianPlan(Long planId, Long guardianUserId) {
        GuardianWeeklyPlan plan = guardianWeeklyPlanMapper.selectById(planId);
        if (plan == null || !guardianUserId.equals(plan.getGuardianUserId())) {
            throw new LogicException(ErrorCodeConstants.GUARDIAN_WEEKLY_PLAN_NOT_EXIST);
        }
        return plan;
    }

    private void requireGuardianBinding(Long studentUserId) {
        if (!guardianBindingService.canCurrentGuardianAccessStudent(studentUserId)) {
            throw new LogicException(ErrorCodeConstants.GUARDIAN_BINDING_NOT_EXIST);
        }
    }

    private boolean hasActiveBinding(GuardianWeeklyPlan plan) {
        return guardianBindingService.hasActiveBinding(plan.getStudentUserId(), plan.getGuardianUserId());
    }

    private GuardianWeeklyPlanResp toResponse(GuardianWeeklyPlan plan) {
        GuardianWeeklyPlanResp response = new GuardianWeeklyPlanResp();
        BeanUtils.copyProperties(plan, response);
        User student = userService.getById(plan.getStudentUserId());
        response.setStudentName(student == null ? null : (StringUtils.hasText(student.getRealName())
                ? student.getRealName() : student.getUserName()));
        return response;
    }

    private LocalDate normalizeWeekStart(LocalDate weekStartDate) {
        LocalDate date = weekStartDate == null ? LocalDate.now() : weekStartDate;
        return date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }
}
