package com.study.module.system.guardian.dto.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

/** 当前用户可见的学生—家长绑定信息。 */
@Data
public class GuardianBindingResp {

    private Long relationId;
    private Long studentUserId;
    private String studentName;
    @ApiModelProperty("学生最近一次成功登录时间；仅在监护关系有效时向家长返回")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime studentLastLoginTime;
    private Long guardianUserId;
    private String guardianName;
    private String relationType;
    @ApiModelProperty("0待家长接受，1待学生确认，2有效，3已解绑")
    private Integer status;
    @ApiModelProperty("当前用户在此关系中的身份：STUDENT 或 GUARDIAN")
    private String currentRole;
    private LocalDateTime invitationExpireTime;
    private LocalDateTime acceptTime;
    private LocalDateTime confirmTime;
    private LocalDateTime revokeTime;
}
