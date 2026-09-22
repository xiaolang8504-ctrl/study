package com.study.module.system.guardian.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.api.contants.UploadType;
import com.study.common.core.enums.ErrorCodeConstants;
import com.study.common.core.exception.LogicException;
import com.study.module.system.file.entity.File;
import com.study.module.system.file.service.FileService;
import com.study.module.system.guardian.dto.request.GuardianAssistedCaptureCreateReq;
import com.study.module.system.guardian.dto.response.GuardianAssistedCaptureResp;
import com.study.module.system.guardian.entity.GuardianAssistedCapture;
import com.study.module.system.guardian.mapper.GuardianAssistedCaptureMapper;
import com.study.module.system.guardian.service.GuardianAccessAuditService;
import com.study.module.system.guardian.service.GuardianAssistedCaptureService;
import com.study.module.system.guardian.service.GuardianBindingService;
import com.study.module.system.user.entity.User;
import com.study.module.system.user.service.UserService;
import com.study.module.system.wrongquestion.dto.request.QuestionCaptureTaskCreateReq;
import com.study.module.system.wrongquestion.service.QuestionCaptureService;
import com.yunshang.budget.common.security.utils.AccountUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 家长只能提交文件与来源信息；学生确认后才会创建学生名下的采集任务。
 */
@Service
public class GuardianAssistedCaptureServiceImpl extends ServiceImpl<GuardianAssistedCaptureMapper, GuardianAssistedCapture>
        implements GuardianAssistedCaptureService {
    private static final int PENDING_STUDENT_CONFIRM = 0;
    private static final int CONFIRMED = 1;
    private static final int REVOKED = 2;

    @Autowired private GuardianBindingService guardianBindingService;
    @Autowired private GuardianAccessAuditService guardianAccessAuditService;
    @Autowired private FileService fileService;
    @Autowired private QuestionCaptureService questionCaptureService;
    @Autowired private UserService userService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createGuardianAssistedCapture(GuardianAssistedCaptureCreateReq request) {
        Long guardianUserId = AccountUtils.getUserId();
        if (!guardianBindingService.canCurrentGuardianAccessStudent(request.getStudentUserId())) {
            throw new LogicException(ErrorCodeConstants.GUARDIAN_BINDING_NOT_EXIST);
        }
        for (Long fileId : request.getImageFileIds()) {
            fileService.checkUserFile(Math.toIntExact(fileId), UploadType.WRONG_QUESTION, guardianUserId);
        }
        LocalDateTime now = LocalDateTime.now();
        GuardianAssistedCapture capture = new GuardianAssistedCapture();
        BeanUtils.copyProperties(request, capture);
        capture.setGuardianUserId(guardianUserId);
        capture.setSourceFileIds(request.getImageFileIds().stream().map(String::valueOf).collect(Collectors.joining(",")));
        capture.setStatus(PENDING_STUDENT_CONFIRM);
        capture.setCreateTime(now);
        capture.setUpdateTime(now);
        save(capture);
        guardianAccessAuditService.record(null, request.getStudentUserId(), guardianUserId, guardianUserId,
                "ASSISTED_CAPTURE_CREATED", "SUCCESS", "家长代上传，等待学生确认");
        return capture.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long confirmGuardianAssistedCapture(Long id) {
        Long studentUserId = AccountUtils.getUserId();
        GuardianAssistedCapture capture = getById(id);
        if (capture == null || !studentUserId.equals(capture.getStudentUserId())) {
            throw new LogicException(ErrorCodeConstants.GUARDIAN_ASSISTED_CAPTURE_NOT_EXIST);
        }
        if (!Integer.valueOf(PENDING_STUDENT_CONFIRM).equals(capture.getStatus())) {
            throw new LogicException(ErrorCodeConstants.GUARDIAN_ASSISTED_CAPTURE_STATUS_INVALID);
        }
        List<Long> copiedFileIds = copyFilesForStudent(capture, studentUserId);
        QuestionCaptureTaskCreateReq request = new QuestionCaptureTaskCreateReq();
        request.setGrade(capture.getGrade()); request.setSubject(capture.getSubject());
        request.setQuestionType(capture.getQuestionType()); request.setSource(capture.getSource());
        request.setLearningPoint(capture.getLearningPoint()); request.setErrorLabels(capture.getErrorLabels());
        request.setImageFileIds(copiedFileIds);
        request.setClientRequestId("guardian-assist-" + capture.getId());
        Long taskId = questionCaptureService.createQuestionCaptureTaskByUserId(request, studentUserId);
        LocalDateTime now = LocalDateTime.now();
        capture.setStatus(CONFIRMED); capture.setCaptureTaskId(taskId); capture.setConfirmedByUserId(studentUserId);
        capture.setConfirmTime(now); capture.setUpdateTime(now);
        updateById(capture);
        guardianAccessAuditService.record(null, studentUserId, capture.getGuardianUserId(), studentUserId,
                "ASSISTED_CAPTURE_CONFIRMED", "SUCCESS", "学生确认并创建自己的采集任务=" + taskId);
        return taskId;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void revokeGuardianAssistedCapture(Long id) {
        Long userId = AccountUtils.getUserId();
        GuardianAssistedCapture capture = getById(id);
        if (capture == null || (!userId.equals(capture.getGuardianUserId()) && !userId.equals(capture.getStudentUserId()))) {
            throw new LogicException(ErrorCodeConstants.GUARDIAN_ASSISTED_CAPTURE_NOT_EXIST);
        }
        if (!Integer.valueOf(PENDING_STUDENT_CONFIRM).equals(capture.getStatus())) {
            throw new LogicException(ErrorCodeConstants.GUARDIAN_ASSISTED_CAPTURE_STATUS_INVALID);
        }
        capture.setStatus(REVOKED); capture.setRevokeTime(LocalDateTime.now()); capture.setUpdateTime(LocalDateTime.now());
        updateById(capture);
        guardianAccessAuditService.record(null, capture.getStudentUserId(), capture.getGuardianUserId(), userId,
                "ASSISTED_CAPTURE_REVOKED", "SUCCESS", "学生或家长撤销待确认代上传");
    }

    @Override
    public List<GuardianAssistedCaptureResp> guardianAssistedCaptureList() {
        Long userId = AccountUtils.getUserId();
        return list(new LambdaQueryWrapper<GuardianAssistedCapture>().and(wrapper -> wrapper
                .eq(GuardianAssistedCapture::getStudentUserId, userId).or()
                .eq(GuardianAssistedCapture::getGuardianUserId, userId)).orderByDesc(GuardianAssistedCapture::getCreateTime))
                .stream().map(capture -> toResp(capture)).collect(Collectors.toList());
    }

    private List<Long> copyFilesForStudent(GuardianAssistedCapture capture, Long studentUserId) {
        List<Long> result = new ArrayList<>();
        for (String idText : StringUtils.commaDelimitedListToSet(capture.getSourceFileIds())) {
            File sourceFile = fileService.checkUserFile(Integer.valueOf(idText), UploadType.WRONG_QUESTION, capture.getGuardianUserId());
            File targetFile = new File();
            BeanUtils.copyProperties(sourceFile, targetFile, "id", "createId", "createTime");
            targetFile.setCreateId(studentUserId); targetFile.setCreateTime(LocalDateTime.now());
            result.add(Long.valueOf(fileService.createFile(targetFile).getId()));
        }
        return result;
    }

    private GuardianAssistedCaptureResp toResp(GuardianAssistedCapture capture) {
        GuardianAssistedCaptureResp response = new GuardianAssistedCaptureResp();
        BeanUtils.copyProperties(capture, response);
        User user = userService.getById(capture.getStudentUserId());
        response.setStudentName(user == null ? null : (StringUtils.hasText(user.getRealName()) ? user.getRealName() : user.getUserName()));
        return response;
    }
}
