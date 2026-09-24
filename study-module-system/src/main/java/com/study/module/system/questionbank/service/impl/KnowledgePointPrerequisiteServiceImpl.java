package com.study.module.system.questionbank.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.common.core.enums.ErrorCodeConstants;
import com.study.common.core.exception.LogicException;
import com.study.module.system.questionbank.dto.request.KnowledgePointPrerequisiteSaveReq;
import com.study.module.system.questionbank.dto.response.KnowledgePointPrerequisiteResp;
import com.study.module.system.questionbank.entity.KnowledgePoint;
import com.study.module.system.questionbank.entity.KnowledgePointPrerequisite;
import com.study.module.system.questionbank.mapper.KnowledgePointPrerequisiteMapper;
import com.study.module.system.questionbank.service.KnowledgePointPrerequisiteService;
import com.study.module.system.questionbank.service.KnowledgePointService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class KnowledgePointPrerequisiteServiceImpl extends ServiceImpl<KnowledgePointPrerequisiteMapper, KnowledgePointPrerequisite>
        implements KnowledgePointPrerequisiteService {

    @Autowired private KnowledgePointService knowledgePointService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveKnowledgePointPrerequisite(KnowledgePointPrerequisiteSaveReq request) {
        if (request.getKnowledgePointId().equals(request.getPrerequisitePointId())) {
            throw new LogicException(ErrorCodeConstants.QUESTION_BANK_SAVE_FAIL);
        }
        KnowledgePoint target = knowledgePointService.getById(request.getKnowledgePointId());
        KnowledgePoint prerequisite = knowledgePointService.getById(request.getPrerequisitePointId());
        if (target == null || prerequisite == null || !target.getSubject().equals(prerequisite.getSubject())
                || !target.getGrade().equals(prerequisite.getGrade())) {
            throw new LogicException(ErrorCodeConstants.QUESTION_BANK_SAVE_FAIL);
        }
        KnowledgePointPrerequisite entity = request.getId() == null ? new KnowledgePointPrerequisite() : getById(request.getId());
        if (entity == null) throw new LogicException(ErrorCodeConstants.QUESTION_BANK_SAVE_FAIL);
        boolean duplicate = lambdaQuery().eq(KnowledgePointPrerequisite::getKnowledgePointId, request.getKnowledgePointId())
                .eq(KnowledgePointPrerequisite::getPrerequisitePointId, request.getPrerequisitePointId())
                .ne(request.getId() != null, KnowledgePointPrerequisite::getId, request.getId()).exists();
        if (duplicate) throw new LogicException(ErrorCodeConstants.QUESTION_BANK_SAVE_FAIL);
        LocalDateTime now = LocalDateTime.now();
        entity.setKnowledgePointId(request.getKnowledgePointId());
        entity.setPrerequisitePointId(request.getPrerequisitePointId());
        entity.setRelationVersion(StringUtils.hasText(request.getRelationVersion()) ? request.getRelationVersion().trim() : "v1");
        entity.setEnable(request.getEnable() == null ? 1 : request.getEnable());
        entity.setUpdateTime(now);
        if (entity.getId() == null) entity.setCreateTime(now);
        saveOrUpdate(entity);
    }

    @Override
    public List<KnowledgePointPrerequisiteResp> knowledgePointPrerequisiteList(Long knowledgePointId, String subject) {
        List<KnowledgePointPrerequisite> relations = list(new LambdaQueryWrapper<KnowledgePointPrerequisite>()
                .eq(knowledgePointId != null, KnowledgePointPrerequisite::getKnowledgePointId, knowledgePointId)
                .orderByAsc(KnowledgePointPrerequisite::getKnowledgePointId, KnowledgePointPrerequisite::getId));
        List<Long> pointIds = relations.stream().flatMap(item -> java.util.stream.Stream.of(item.getKnowledgePointId(), item.getPrerequisitePointId()))
                .distinct().collect(Collectors.toList());
        Map<Long, KnowledgePoint> pointMap = pointIds.isEmpty() ? new HashMap<>() : knowledgePointService.listByIds(pointIds).stream()
                .filter(item -> !StringUtils.hasText(subject) || subject.equals(item.getSubject()))
                .collect(Collectors.toMap(KnowledgePoint::getId, item -> item));
        return relations.stream().filter(item -> pointMap.containsKey(item.getKnowledgePointId()) && pointMap.containsKey(item.getPrerequisitePointId()))
                .map(item -> { KnowledgePointPrerequisiteResp resp = new KnowledgePointPrerequisiteResp(); resp.setId(item.getId()); resp.setKnowledgePointId(item.getKnowledgePointId()); resp.setKnowledgePointName(pointMap.get(item.getKnowledgePointId()).getPointName()); resp.setPrerequisitePointId(item.getPrerequisitePointId()); resp.setPrerequisitePointName(pointMap.get(item.getPrerequisitePointId()).getPointName()); resp.setRelationVersion(item.getRelationVersion()); resp.setEnable(item.getEnable()); return resp; })
                .collect(Collectors.toList());
    }
}
