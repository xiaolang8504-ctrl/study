package com.study.module.system.guardian.service;

/** 家庭协作审计服务。 */
public interface GuardianAccessAuditService {

    void record(Long relationId, Long studentUserId, Long guardianUserId, Long operatorUserId,
                String actionType, String operationResult, String detail);
}
