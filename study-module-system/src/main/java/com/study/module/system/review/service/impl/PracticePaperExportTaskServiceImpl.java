package com.study.module.system.review.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.common.core.enums.ErrorCodeConstants;
import com.study.common.core.exception.LogicException;
import com.study.module.system.review.dto.request.PracticePaperExportCreateReq;
import com.study.module.system.review.dto.response.PracticePaperExportTaskResp;
import com.study.module.system.review.dto.response.PracticeSessionDetailResp;
import com.study.module.system.review.entity.PracticePaperExportTask;
import com.study.module.system.review.mapper.PracticePaperExportTaskMapper;
import com.study.module.system.review.service.PracticePaperExportAsyncService;
import com.study.module.system.review.service.PracticePaperExportTaskService;
import com.study.module.system.review.service.PracticeSessionDetailService;
import com.yunshang.budget.common.security.utils.AccountUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service public class PracticePaperExportTaskServiceImpl extends ServiceImpl<PracticePaperExportTaskMapper, PracticePaperExportTask> implements PracticePaperExportTaskService {
    @Autowired private PracticeSessionDetailService practiceSessionDetailService;
    @Autowired private PracticePaperExportAsyncService practicePaperExportAsyncService;
    @Override public Long createPracticePaperExportTask(PracticePaperExportCreateReq request) {
        String format = request.getFormat().trim().toUpperCase(Locale.ROOT);
        if (!"PDF".equals(format) && !"DOCX".equals(format)) throw new LogicException(ErrorCodeConstants.PRACTICE_PAPER_EXPORT_FORMAT_INVALID);
        Long userId = AccountUtils.getUserId();
        PracticeSessionDetailResp paper = practiceSessionDetailService.practicePaperDetail(request.getSessionId());
        PracticePaperExportTask task = new PracticePaperExportTask(); task.setUserId(userId); task.setSessionId(request.getSessionId()); task.setFormat(format); task.setAnswerMode(request.getAnswerMode()); task.setStatus(0); task.setCreateTime(LocalDateTime.now()); task.setUpdateTime(LocalDateTime.now());
        save(task); practicePaperExportAsyncService.generatePracticePaper(task.getId(), userId, paper, format, request.getAnswerMode()); return task.getId();
    }
    @Override public List<PracticePaperExportTaskResp> practicePaperExportTaskList() { return list(new LambdaQueryWrapper<PracticePaperExportTask>().eq(PracticePaperExportTask::getUserId, AccountUtils.getUserId()).orderByDesc(PracticePaperExportTask::getId).last("LIMIT 100")).stream().map(item -> { PracticePaperExportTaskResp response = new PracticePaperExportTaskResp(); BeanUtils.copyProperties(item, response); return response; }).collect(Collectors.toList()); }
    @Override public PracticePaperExportTask checkPracticePaperExportTask(Long id) { PracticePaperExportTask task = getOne(new LambdaQueryWrapper<PracticePaperExportTask>().eq(PracticePaperExportTask::getId, id).eq(PracticePaperExportTask::getUserId, AccountUtils.getUserId())); if (task == null) throw new LogicException(ErrorCodeConstants.PRACTICE_PAPER_EXPORT_NOT_EXIST); return task; }
}
