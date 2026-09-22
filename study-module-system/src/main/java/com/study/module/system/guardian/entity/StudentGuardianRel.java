package com.study.module.system.guardian.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 学生与家长的受控监护关系。
 */
@Data
@TableName("student_guardian_rel")
public class StudentGuardianRel {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long studentUserId;
    private Long guardianUserId;
    private String relationType;
    private Integer status;
    private String invitationCodeHash;
    private LocalDateTime invitationExpireTime;
    private Long inviterUserId;
    private Long accepterUserId;
    private Long confirmerUserId;
    private Long revokerUserId;
    private LocalDateTime acceptTime;
    private LocalDateTime confirmTime;
    private LocalDateTime revokeTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
