package com.study.module.system.guardian.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.module.system.guardian.entity.GuardianAccessAudit;
import com.study.module.system.guardian.mapper.GuardianAccessAuditMapper;
import com.study.module.system.guardian.service.GuardianAccessAuditService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/** 家庭协作审计持久化实现。 */
@Slf4j
@Service
public class GuardianAccessAuditServiceImpl extends ServiceImpl<GuardianAccessAuditMapper, GuardianAccessAudit>
        implements GuardianAccessAuditService {

    @Override
    public void record(Long relationId, Long studentUserId, Long guardianUserId, Long operatorUserId,
                       String actionType, String operationResult, String detail) {
        try {
            GuardianAccessAudit audit = new GuardianAccessAudit();
            audit.setRelationId(relationId);
            audit.setStudentUserId(studentUserId);
            audit.setGuardianUserId(guardianUserId);
            audit.setOperatorUserId(operatorUserId);
            audit.setActionType(actionType);
            audit.setOperationResult(operationResult);
            audit.setSource("PC_WEB");
            audit.setDetail(abbreviate(detail));
            audit.setCreateTime(LocalDateTime.now());
            save(audit);
        } catch (Exception exception) {
            log.error("记录家庭协作审计失败，relationId={}, actionType={}", relationId, actionType, exception);
        }
    }

    private String abbreviate(String detail) {
        return detail == null || detail.length() <= 500 ? detail : detail.substring(0, 500);
    }
}
