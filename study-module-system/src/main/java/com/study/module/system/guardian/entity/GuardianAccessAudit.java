package com.study.module.system.guardian.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 家庭协作受控操作审计记录。
 */
@Data
@TableName("guardian_access_audit")
public class GuardianAccessAudit {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long relationId;
    private Long studentUserId;
    private Long guardianUserId;
    private Long operatorUserId;
    private String actionType;
    private String operationResult;
    private String source;
    private String detail;
    private LocalDateTime createTime;
}
