package com.study.module.system.guardian.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.common.core.enums.ErrorCodeConstants;
import com.study.common.core.exception.LogicException;
import com.study.module.system.guardian.constants.GuardianBindingStatus;
import com.study.module.system.guardian.dto.request.GuardianInviteCreateReq;
import com.study.module.system.guardian.dto.response.GuardianBindingResp;
import com.study.module.system.guardian.dto.response.GuardianInviteResp;
import com.study.module.system.guardian.entity.StudentGuardianRel;
import com.study.module.system.guardian.mapper.StudentGuardianRelMapper;
import com.study.module.system.guardian.service.GuardianAccessAuditService;
import com.study.module.system.guardian.service.GuardianBindingService;
import com.study.module.system.user.entity.User;
import com.study.module.system.user.service.UserService;
import com.yunshang.budget.common.security.utils.AccountUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

/** 学生—家长受控绑定实现。 */
@Service
public class GuardianBindingServiceImpl extends ServiceImpl<StudentGuardianRelMapper, StudentGuardianRel>
        implements GuardianBindingService {

    private static final int INVITATION_EXPIRE_DAYS = 7;
    private static final int INVITATION_RANDOM_BYTES = 18;

    private final SecureRandom secureRandom = new SecureRandom();

    @Autowired
    private UserService userService;

    @Autowired
    private GuardianAccessAuditService guardianAccessAuditService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public GuardianInviteResp createGuardianInvitation(GuardianInviteCreateReq request) {
        Long studentUserId = AccountUtils.getUserId();
        userService.checkUserByUserId(studentUserId);
        LocalDateTime now = LocalDateTime.now();
        String invitationCode = createInvitationCode();
        StudentGuardianRel relation = new StudentGuardianRel();
        relation.setStudentUserId(studentUserId);
        relation.setRelationType(StringUtils.hasText(request.getRelationType())
                ? request.getRelationType().trim().toUpperCase() : "GUARDIAN");
        relation.setStatus(GuardianBindingStatus.PENDING_GUARDIAN_ACCEPT);
        relation.setInvitationCodeHash(sha256(invitationCode));
        relation.setInvitationExpireTime(now.plusDays(INVITATION_EXPIRE_DAYS));
        relation.setInviterUserId(studentUserId);
        relation.setCreateTime(now);
        relation.setUpdateTime(now);
        save(relation);
        guardianAccessAuditService.record(relation.getId(), studentUserId, null, studentUserId,
                "INVITATION_CREATED", "SUCCESS", "学生创建监护邀请码");

        GuardianInviteResp response = new GuardianInviteResp();
        response.setRelationId(relation.getId());
        response.setInvitationCode(invitationCode);
        response.setExpireTime(relation.getInvitationExpireTime());
        response.setStatus(relation.getStatus());
        return response;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void acceptGuardianInvitation(String invitationCode) {
        Long guardianUserId = AccountUtils.getUserId();
        userService.checkUserByUserId(guardianUserId);
        StudentGuardianRel relation = getOne(new LambdaQueryWrapper<StudentGuardianRel>()
                .eq(StudentGuardianRel::getInvitationCodeHash, sha256(invitationCode.trim()))
                .eq(StudentGuardianRel::getStatus, GuardianBindingStatus.PENDING_GUARDIAN_ACCEPT));
        if (relation == null || relation.getInvitationExpireTime() == null
                || !relation.getInvitationExpireTime().isAfter(LocalDateTime.now())) {
            throw new LogicException(ErrorCodeConstants.GUARDIAN_INVITATION_INVALID);
        }
        if (guardianUserId.equals(relation.getStudentUserId())) {
            throw new LogicException(ErrorCodeConstants.GUARDIAN_BINDING_SELF_FORBIDDEN);
        }
        boolean duplicate = count(new LambdaQueryWrapper<StudentGuardianRel>()
                .eq(StudentGuardianRel::getStudentUserId, relation.getStudentUserId())
                .eq(StudentGuardianRel::getGuardianUserId, guardianUserId)
                .in(StudentGuardianRel::getStatus, GuardianBindingStatus.PENDING_STUDENT_CONFIRM,
                        GuardianBindingStatus.ACTIVE)) > 0;
        if (duplicate) {
            throw new LogicException(ErrorCodeConstants.GUARDIAN_BINDING_DUPLICATE);
        }
        LocalDateTime now = LocalDateTime.now();
        relation.setGuardianUserId(guardianUserId);
        relation.setAccepterUserId(guardianUserId);
        relation.setAcceptTime(now);
        relation.setStatus(GuardianBindingStatus.PENDING_STUDENT_CONFIRM);
        relation.setInvitationCodeHash(null);
        relation.setUpdateTime(now);
        if (!updateById(relation)) {
            throw new LogicException(ErrorCodeConstants.GUARDIAN_INVITATION_INVALID);
        }
        guardianAccessAuditService.record(relation.getId(), relation.getStudentUserId(), guardianUserId, guardianUserId,
                "INVITATION_ACCEPTED", "SUCCESS", "家长接受邀请码，等待学生确认");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmGuardianBinding(Long relationId) {
        Long studentUserId = AccountUtils.getUserId();
        StudentGuardianRel relation = getById(relationId);
        if (relation == null || !studentUserId.equals(relation.getStudentUserId())) {
            throw new LogicException(ErrorCodeConstants.GUARDIAN_BINDING_NOT_EXIST);
        }
        if (!Integer.valueOf(GuardianBindingStatus.PENDING_STUDENT_CONFIRM).equals(relation.getStatus())) {
            throw new LogicException(ErrorCodeConstants.GUARDIAN_BINDING_STATUS_INVALID);
        }
        LocalDateTime now = LocalDateTime.now();
        relation.setStatus(GuardianBindingStatus.ACTIVE);
        relation.setConfirmerUserId(studentUserId);
        relation.setConfirmTime(now);
        relation.setUpdateTime(now);
        if (!updateById(relation)) {
            throw new LogicException(ErrorCodeConstants.GUARDIAN_BINDING_STATUS_INVALID);
        }
        guardianAccessAuditService.record(relation.getId(), studentUserId, relation.getGuardianUserId(), studentUserId,
                "BINDING_CONFIRMED", "SUCCESS", "学生确认监护绑定");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void revokeGuardianBinding(Long relationId) {
        Long userId = AccountUtils.getUserId();
        StudentGuardianRel relation = getById(relationId);
        if (relation == null || (!userId.equals(relation.getStudentUserId())
                && !userId.equals(relation.getGuardianUserId()))) {
            throw new LogicException(ErrorCodeConstants.GUARDIAN_BINDING_NOT_EXIST);
        }
        if (Integer.valueOf(GuardianBindingStatus.REVOKED).equals(relation.getStatus())) {
            throw new LogicException(ErrorCodeConstants.GUARDIAN_BINDING_STATUS_INVALID);
        }
        LocalDateTime now = LocalDateTime.now();
        relation.setStatus(GuardianBindingStatus.REVOKED);
        relation.setRevokerUserId(userId);
        relation.setRevokeTime(now);
        relation.setUpdateTime(now);
        if (!updateById(relation)) {
            throw new LogicException(ErrorCodeConstants.GUARDIAN_BINDING_STATUS_INVALID);
        }
        guardianAccessAuditService.record(relation.getId(), relation.getStudentUserId(), relation.getGuardianUserId(), userId,
                "BINDING_REVOKED", "SUCCESS", "学生或家长主动解绑，立即撤销数据访问权");
    }

    @Override
    public List<GuardianBindingResp> currentGuardianBindingList() {
        Long userId = AccountUtils.getUserId();
        return list(new LambdaQueryWrapper<StudentGuardianRel>()
                .and(wrapper -> wrapper.eq(StudentGuardianRel::getStudentUserId, userId)
                        .or().eq(StudentGuardianRel::getGuardianUserId, userId))
                .orderByDesc(StudentGuardianRel::getUpdateTime))
                .stream().map(relation -> buildResponse(relation, userId)).collect(Collectors.toList());
    }

    @Override
    public List<Long> activeStudentIdsOfCurrentGuardian() {
        Long guardianUserId = AccountUtils.getUserId();
        return list(new LambdaQueryWrapper<StudentGuardianRel>()
                .eq(StudentGuardianRel::getGuardianUserId, guardianUserId)
                .eq(StudentGuardianRel::getStatus, GuardianBindingStatus.ACTIVE))
                .stream().map(StudentGuardianRel::getStudentUserId).collect(Collectors.toList());
    }

    @Override
    public boolean canCurrentGuardianAccessStudent(Long studentUserId) {
        if (studentUserId == null) {
            return false;
        }
        Long guardianUserId = AccountUtils.getUserId();
        return count(new LambdaQueryWrapper<StudentGuardianRel>()
                .eq(StudentGuardianRel::getGuardianUserId, guardianUserId)
                .eq(StudentGuardianRel::getStudentUserId, studentUserId)
                .eq(StudentGuardianRel::getStatus, GuardianBindingStatus.ACTIVE)) > 0;
    }

    private GuardianBindingResp buildResponse(StudentGuardianRel relation, Long currentUserId) {
        GuardianBindingResp response = new GuardianBindingResp();
        response.setRelationId(relation.getId());
        response.setStudentUserId(relation.getStudentUserId());
        response.setGuardianUserId(relation.getGuardianUserId());
        response.setRelationType(relation.getRelationType());
        response.setStatus(relation.getStatus());
        response.setInvitationExpireTime(relation.getInvitationExpireTime());
        response.setAcceptTime(relation.getAcceptTime());
        response.setConfirmTime(relation.getConfirmTime());
        response.setRevokeTime(relation.getRevokeTime());
        response.setCurrentRole(currentUserId.equals(relation.getStudentUserId()) ? "STUDENT" : "GUARDIAN");
        User student = userService.getById(relation.getStudentUserId());
        response.setStudentName(userName(student));
        if (Integer.valueOf(GuardianBindingStatus.ACTIVE).equals(relation.getStatus())) {
            response.setStudentLastLoginTime(student == null ? null : student.getLastLoginTime());
        }
        response.setGuardianName(userName(relation.getGuardianUserId()));
        return response;
    }

    private String userName(Long userId) {
        if (userId == null) {
            return null;
        }
        return userName(userService.getById(userId));
    }

    private String userName(User user) {
        return user == null ? null : (StringUtils.hasText(user.getRealName()) ? user.getRealName() : user.getUserName());
    }

    private String createInvitationCode() {
        byte[] bytes = new byte[INVITATION_RANDOM_BYTES];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String sha256(String source) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(source.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder(hash.length * 2);
            for (byte item : hash) {
                builder.append(String.format("%02x", item));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 algorithm unavailable", exception);
        }
    }
}
