package com.study.module.system.questionbank.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.module.system.questionbank.entity.KnowledgePoint;
import com.study.module.system.questionbank.entity.WrongQuestionKnowledgePoint;
import com.study.module.system.questionbank.mapper.WrongQuestionKnowledgePointMapper;
import com.study.module.system.questionbank.service.KnowledgePointService;
import com.study.module.system.questionbank.service.WrongQuestionKnowledgePointService;
import com.study.module.system.wrongquestion.entity.WrongQuestion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.Collections;

@Service
public class WrongQuestionKnowledgePointServiceImpl
        extends ServiceImpl<WrongQuestionKnowledgePointMapper, WrongQuestionKnowledgePoint>
        implements WrongQuestionKnowledgePointService {
    @Autowired
    KnowledgePointService knowledgePointService;

    /**
     * 解析错题关联的标准知识点ID列表
     */
    @Override
    public List<Long> resolvePointIds(WrongQuestion wrong) {
        List<WrongQuestionKnowledgePoint> relations = list(new LambdaQueryWrapper<WrongQuestionKnowledgePoint>()
                .eq(WrongQuestionKnowledgePoint::getWrongQuestionId, wrong.getId()));
        if (!relations.isEmpty()) {
            return relations.stream().map(WrongQuestionKnowledgePoint::getKnowledgePointId).collect(Collectors.toList());
        }
        String source = String.join(" ", Optional.ofNullable(wrong.getLearningPoint()).orElse(""),
                Optional.ofNullable(wrong.getQuestionTitle()).orElse(""),
                Optional.ofNullable(wrong.getQuestionContent()).orElse(""));
        List<Long> pointIds = knowledgePointService.list(new LambdaQueryWrapper<KnowledgePoint>()
                        .eq(KnowledgePoint::getGrade, wrong.getGrade())
                        .eq(KnowledgePoint::getSubject, wrong.getSubject()).eq(KnowledgePoint::getEnable, 1))
                .stream().filter(point -> source.contains(point.getPointName()))
                .map(KnowledgePoint::getId).collect(Collectors.toList());
        pointIds.forEach(pointId -> {
            WrongQuestionKnowledgePoint relation = new WrongQuestionKnowledgePoint();
            relation.setWrongQuestionId(wrong.getId());
            relation.setKnowledgePointId(pointId);
            relation.setRelationSource("AUTO_MIGRATION");
            save(relation);
        });
        return pointIds;
    }

    /**
     * 执行 rewrite 业务处理。
     */
    @Override
    public void rewrite(Long wrongQuestionId, List<Long> knowledgePointIds) {
        remove(new LambdaQueryWrapper<WrongQuestionKnowledgePoint>()
                .eq(WrongQuestionKnowledgePoint::getWrongQuestionId, wrongQuestionId));
        for (Long pointId : Optional.ofNullable(knowledgePointIds).orElse(Collections.emptyList())) {
            WrongQuestionKnowledgePoint relation = new WrongQuestionKnowledgePoint();
            relation.setWrongQuestionId(wrongQuestionId);
            relation.setKnowledgePointId(pointId);
            relation.setRelationSource("MANUAL");
            save(relation);
        }
    }
}
