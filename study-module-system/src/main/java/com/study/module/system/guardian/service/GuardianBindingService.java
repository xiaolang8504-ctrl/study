package com.study.module.system.guardian.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.study.module.system.guardian.dto.request.GuardianInviteCreateReq;
import com.study.module.system.guardian.dto.response.GuardianBindingResp;
import com.study.module.system.guardian.dto.response.GuardianInviteResp;
import com.study.module.system.guardian.entity.StudentGuardianRel;

import java.util.List;

/** 家庭监护关系服务。 */
public interface GuardianBindingService extends IService<StudentGuardianRel> {

    GuardianInviteResp createGuardianInvitation(GuardianInviteCreateReq request);

    void acceptGuardianInvitation(String invitationCode);

    void confirmGuardianBinding(Long relationId);

    void revokeGuardianBinding(Long relationId);

    List<GuardianBindingResp> currentGuardianBindingList();

    /** 仅返回当前家长已被授权查看的学生ID，用作后续家长数据查询的数据域入口。 */
    List<Long> activeStudentIdsOfCurrentGuardian();

    /** 判断当前用户是否仍拥有某学生的数据查看权。 */
    boolean canCurrentGuardianAccessStudent(Long studentUserId);
}
