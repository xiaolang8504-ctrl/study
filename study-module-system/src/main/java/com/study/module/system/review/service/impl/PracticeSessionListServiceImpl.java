package com.study.module.system.review.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.study.common.core.domain.dto.PageResult;
import com.study.module.system.review.dto.request.PracticeSessionPageListReq;
import com.study.module.system.review.dto.response.PracticeSessionPageListResp;
import com.study.module.system.review.entity.PracticeSession;
import com.study.module.system.review.service.PracticeSessionListService;
import com.study.module.system.review.service.PracticeSessionService;
import com.yunshang.budget.common.mybatis.utils.PageUtils;
import com.yunshang.budget.common.security.utils.AccountUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.stream.Collectors;

/**
 * 专项练习历史列表服务实现
 */
@Service
public class PracticeSessionListServiceImpl implements PracticeSessionListService {

    @Autowired
    PracticeSessionService practiceSessionService;

    @Override
    public PageResult<PracticeSessionPageListResp> practiceSessionPageList(PracticeSessionPageListReq request) {
        Long userId = AccountUtils.getUserId();
        LambdaQueryWrapper<PracticeSession> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PracticeSession::getUserId, userId);
        wrapper.eq(StringUtils.hasText(request.getPracticeType()), PracticeSession::getPracticeType,
                request.getPracticeType());
        wrapper.eq(StringUtils.hasText(request.getSubject()), PracticeSession::getSubject,
                request.getSubject());
        wrapper.and(StringUtils.hasText(request.getKeyWord()), query -> query
                .like(PracticeSession::getTitle, request.getKeyWord())
                .or().like(PracticeSession::getLearningPoint, request.getKeyWord())
                .or().like(PracticeSession::getErrorLabel, request.getKeyWord()));
        wrapper.orderByDesc(PracticeSession::getCreateTime);
        Page<PracticeSession> page = new Page<>(request.getCurrent(), request.getPageSize());
        practiceSessionService.page(page, wrapper);
        return PageUtils.wrap(page, rows -> rows.stream().map(this::buildResponse)
                .collect(Collectors.toList()));
    }

    /**
     * 构建业务处理结果。
     */
    private PracticeSessionPageListResp buildResponse(PracticeSession session) {
        PracticeSessionPageListResp response = new PracticeSessionPageListResp();
        response.setId(session.getId());
        response.setTitle(session.getTitle());
        response.setPracticeType(session.getPracticeType());
        response.setSubjectName(session.getSubjectName());
        response.setLearningPoint(session.getLearningPoint());
        response.setErrorLabel(session.getErrorLabel());
        response.setQuestionCount(session.getQuestionCount());
        response.setCorrectCount(session.getCorrectCount());
        response.setWrongCount(session.getWrongCount());
        response.setAccuracyRate(session.getAccuracyRate());
        response.setStatus(session.getStatus());
        response.setFinishTime(session.getFinishTime());
        response.setCreateTime(session.getCreateTime());
        return response;
    }
}
