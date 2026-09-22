package com.study.module.system.wrongquestion.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.common.core.enums.ErrorCodeConstants;
import com.study.common.core.exception.LogicException;
import com.study.module.system.questionbank.entity.KnowledgePoint;
import com.study.module.system.questionbank.service.KnowledgePointService;
import com.study.module.system.questionbank.service.WrongQuestionKnowledgePointService;
import com.study.module.system.wrongquestion.dto.request.WrongQuestionKnowledgePointBindReq;
import com.study.module.system.wrongquestion.dto.request.WrongQuestionKnowledgePointStatisticsReq;
import com.study.module.system.wrongquestion.dto.response.WrongQuestionKnowledgePointStatisticsResp;
import com.study.module.system.wrongquestion.entity.WrongQuestion;
import com.study.module.system.wrongquestion.mapper.WrongQuestionMapper;
import com.study.module.system.wrongquestion.service.WrongQuestionKnowledgePointBindService;
import com.study.module.system.wrongquestion.service.WrongQuestionService;
import com.yunshang.budget.common.security.utils.AccountUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 错题知识点绑定服务实现
 */
@Service
public class WrongQuestionKnowledgePointBindServiceImpl
        extends ServiceImpl<WrongQuestionMapper, WrongQuestion>
        implements WrongQuestionKnowledgePointBindService {

    @Autowired
    private WrongQuestionService wrongQuestionService;

    @Autowired
    private WrongQuestionKnowledgePointService wrongQuestionKnowledgePointService;

    @Autowired
    private KnowledgePointService knowledgePointService;

    /**
     * 绑定错题知识点
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void bindWrongQuestionKnowledgePoint(WrongQuestionKnowledgePointBindReq request) {
        WrongQuestion wrongQuestion = wrongQuestionService.checkWrongQuestion(request.getId());
        checkKnowledgePoints(wrongQuestion, request.getKnowledgePointIds());
        wrongQuestionKnowledgePointService.rewrite(request.getId(), request.getKnowledgePointIds());
    }

    /**
     * 错题知识点统计
     */
    @Override
    public List<WrongQuestionKnowledgePointStatisticsResp> wrongQuestionKnowledgePointStatistics(
            WrongQuestionKnowledgePointStatisticsReq request) {
        return baseMapper.selectKnowledgePointStatistics(AccountUtils.getUserId(), request.getGrade(),
                request.getSubject(), request.getStatus());
    }

    /**
     * 校验知识点
     */
    private void checkKnowledgePoints(WrongQuestion wrongQuestion, List<Long> knowledgePointIds) {
        if (CollectionUtils.isEmpty(knowledgePointIds)) {
            return;
        }
        List<KnowledgePoint> points = knowledgePointService.lambdaQuery()
                .in(KnowledgePoint::getId, knowledgePointIds)
                .eq(KnowledgePoint::getGrade, wrongQuestion.getGrade())
                .eq(KnowledgePoint::getSubject, wrongQuestion.getSubject())
                .eq(KnowledgePoint::getEnable, 1)
                .list();
        if (points.size() != knowledgePointIds.stream().distinct().count()) {
            throw new LogicException(ErrorCodeConstants.KNOWLEDGE_POINT_NOT_EXIST);
        }
    }

}
