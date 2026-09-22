package com.study.module.system.questionbank.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.common.core.enums.ErrorCodeConstants;
import com.study.common.core.exception.LogicException;
import com.study.module.system.questionbank.dto.response.QuestionBankDetailResp;
import com.study.module.system.questionbank.dto.response.QuestionBankPageListResp;
import com.study.module.system.questionbank.entity.KnowledgePoint;
import com.study.module.system.questionbank.entity.QuestionKnowledgePoint;
import com.study.module.system.questionbank.mapper.QuestionKnowledgePointMapper;
import com.study.module.system.questionbank.service.KnowledgePointService;
import com.study.module.system.questionbank.service.QuestionKnowledgePointService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class QuestionKnowledgePointServiceImpl extends ServiceImpl<QuestionKnowledgePointMapper, QuestionKnowledgePoint>
        implements QuestionKnowledgePointService {
    @Autowired
    KnowledgePointService knowledgePointService;

    /**
     * 重写实体关联数据
     */
    @Override
    public void rewrite(Long questionId, List<Long> pointIds) {
        remove(new LambdaQueryWrapper<QuestionKnowledgePoint>().eq(QuestionKnowledgePoint::getQuestionId, questionId));
        if (pointIds == null) return;
        for (int i = 0; i < pointIds.size(); i++) {
            if (knowledgePointService.getById(pointIds.get(i)) == null) {
                throw new LogicException(ErrorCodeConstants.KNOWLEDGE_POINT_NOT_EXIST);
            }
            QuestionKnowledgePoint relation = new QuestionKnowledgePoint();
            relation.setQuestionId(questionId);
            relation.setKnowledgePointId(pointIds.get(i));
            relation.setIsPrimary(i == 0 ? 1 : 0);
            save(relation);
        }
    }

    /**
     * 填充题目的知识点信息
     */
    @Override
    public void fill(QuestionBankDetailResp response) {
        List<QuestionKnowledgePoint> relations = list(new LambdaQueryWrapper<QuestionKnowledgePoint>()
                .eq(QuestionKnowledgePoint::getQuestionId, response.getId())
                .orderByDesc(QuestionKnowledgePoint::getIsPrimary));
        response.setKnowledgePointIds(relations.stream().map(QuestionKnowledgePoint::getKnowledgePointId).collect(Collectors.toList()));
        response.setKnowledgePointNames(relations.stream().map(item -> knowledgePointService.getById(item.getKnowledgePointId()))
                .filter(Objects::nonNull).map(KnowledgePoint::getPointName).collect(Collectors.toList()));
    }

    /**
     * 填充题目分页列表的知识点信息
     */
    @Override
    public void fill(QuestionBankPageListResp response) {
        List<QuestionKnowledgePoint> relations = relationList(response.getId());
        response.setKnowledgePointIds(relations.stream().map(QuestionKnowledgePoint::getKnowledgePointId)
                .collect(Collectors.toList()));
        response.setKnowledgePointNames(pointNameList(relations));
    }

    /**
     * 查询题目关联的知识点ID列表
     */
    @Override
    public List<Long> pointIds(Long questionId) {
        return list(new LambdaQueryWrapper<QuestionKnowledgePoint>().eq(QuestionKnowledgePoint::getQuestionId, questionId))
                .stream().map(QuestionKnowledgePoint::getKnowledgePointId).collect(Collectors.toList());
    }

    /**
     * 查询题目关联的知识点名称列表
     */
    @Override
    public List<String> pointNames(Long questionId) {
        return pointNameList(relationList(questionId));
    }

    /**
     * 查询题目知识点关联列表
     */
    private List<QuestionKnowledgePoint> relationList(Long questionId) {
        return list(new LambdaQueryWrapper<QuestionKnowledgePoint>()
                .eq(QuestionKnowledgePoint::getQuestionId, questionId)
                .orderByDesc(QuestionKnowledgePoint::getIsPrimary));
    }

    /**
     * 根据关联列表查询知识点名称
     */
    private List<String> pointNameList(List<QuestionKnowledgePoint> relations) {
        return relations.stream().map(item -> knowledgePointService.getById(item.getKnowledgePointId()))
                .filter(Objects::nonNull).map(KnowledgePoint::getPointName).collect(Collectors.toList());
    }
}
