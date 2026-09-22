package com.study.module.system.wrongquestion.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.module.system.wrongquestion.convert.WrongQuestionConvert;
import com.study.module.system.wrongquestion.dto.request.WrongQuestionPageListReq;
import com.study.module.system.wrongquestion.dto.response.WrongQuestionPageListResp;
import com.study.module.system.wrongquestion.entity.WrongQuestion;
import com.study.module.system.wrongquestion.mapper.WrongQuestionMapper;
import com.study.module.system.wrongquestion.service.WrongQuestionListService;
import com.study.common.core.domain.dto.PageResult;
import com.study.module.system.questionbank.entity.WrongQuestionKnowledgePoint;
import com.study.module.system.user.service.UserService;
import com.yunshang.budget.common.mybatis.utils.PageUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import com.study.module.system.questionbank.service.WrongQuestionKnowledgePointService;
import com.study.module.system.questionbank.service.KnowledgePointService;
import java.util.List;
import java.util.Collections;
import java.util.stream.Collectors;

/**
 * 初中生错题列表服务
 */
@Service
public class WrongQuestionListServiceImpl extends ServiceImpl<WrongQuestionMapper, WrongQuestion> implements WrongQuestionListService {

    @Autowired
    WrongQuestionKnowledgePointService wrongQuestionKnowledgePointService;

    @Autowired
    KnowledgePointService knowledgePointService;

    @Autowired
    UserService userService;

    /**
     * 错题分页列表
     */
    @Override
    public PageResult<WrongQuestionPageListResp> wrongQuestionPageList(WrongQuestionPageListReq request) {
        LambdaQueryWrapper<WrongQuestion> queryWrapper = getWrongQuestionListQueryWrapper(request);
        Page<WrongQuestion> page = new Page<>(request.getCurrent(), request.getPageSize());
        this.page(page, queryWrapper);
        PageResult<WrongQuestionPageListResp> result = PageUtils.wrap(page, WrongQuestionConvert.INSTANCE::toWrongQuestionPageListResp);
        for (int i = 0; i < page.getRecords().size(); i++) {
            List<Long> pointIds = wrongQuestionKnowledgePointService.resolvePointIds(page.getRecords().get(i));
            result.getList().get(i).setKnowledgePointNames(pointIds.isEmpty() ? Collections.emptyList()
                    : knowledgePointService.listByIds(pointIds).stream()
                    .map(item -> item.getPointName()).collect(Collectors.toList()));
        }
        return result;
    }

    /**
     * 获取错题列表查询
     */
    private LambdaQueryWrapper<WrongQuestion> getWrongQuestionListQueryWrapper(WrongQuestionPageListReq request) {
        LambdaQueryWrapper<WrongQuestion> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(WrongQuestion::getCreateId, userService.getUserIdByToken());
        if (StringUtils.hasText(request.getGrade())) {
            queryWrapper.eq(WrongQuestion::getGrade, request.getGrade());
        }
        if (StringUtils.hasText(request.getSubject())) {
            queryWrapper.eq(WrongQuestion::getSubject, request.getSubject());
        }
        if (StringUtils.hasText(request.getQuestionType())) {
            queryWrapper.eq(WrongQuestion::getQuestionType, request.getQuestionType());
        }
        if (StringUtils.hasText(request.getSource())) {
            queryWrapper.eq(WrongQuestion::getSource, request.getSource());
        }
        if (request.getStatus() != null) {
            queryWrapper.eq(WrongQuestion::getStatus, request.getStatus());
        }
        if (request.getKnowledgePointId() != null) {
            List<Long> wrongQuestionIds = wrongQuestionKnowledgePointService.lambdaQuery()
                    .eq(WrongQuestionKnowledgePoint::getKnowledgePointId, request.getKnowledgePointId())
                    .list()
                    .stream()
                    .map(WrongQuestionKnowledgePoint::getWrongQuestionId)
                    .collect(Collectors.toList());
            if (wrongQuestionIds.isEmpty()) {
                queryWrapper.eq(WrongQuestion::getId, -1L);
            } else {
                queryWrapper.in(WrongQuestion::getId, wrongQuestionIds);
            }
        }
        if (StringUtils.hasText(request.getErrorLabel())) {
            queryWrapper.like(WrongQuestion::getErrorLabels, request.getErrorLabel());
        }
        if (StringUtils.hasText(request.getKeyWord())) {
            queryWrapper.and(wrapper -> wrapper.like(WrongQuestion::getQuestionTitle, request.getKeyWord())
                    .or()
                    .like(WrongQuestion::getQuestionContent, request.getKeyWord()));
        }
        queryWrapper.orderByDesc(WrongQuestion::getId);
        return queryWrapper;
    }
}
