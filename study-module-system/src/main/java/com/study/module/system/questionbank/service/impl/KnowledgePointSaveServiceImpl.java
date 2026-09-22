package com.study.module.system.questionbank.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.study.common.core.enums.ErrorCodeConstants;
import com.study.common.core.exception.LogicException;
import com.study.module.system.questionbank.dto.request.KnowledgePointSaveReq;
import com.study.module.system.questionbank.entity.KnowledgePoint;
import com.study.module.system.questionbank.mapper.KnowledgePointMapper;
import com.study.module.system.questionbank.service.KnowledgePointSaveService;
import com.study.module.system.questionbank.convert.KnowledgePointConvert;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

/**
 * 知识点保存服务实现
 */
@Service
public class KnowledgePointSaveServiceImpl extends ServiceImpl<KnowledgePointMapper, KnowledgePoint>
        implements KnowledgePointSaveService {

    /**
     * 保存知识点
     */
    @Override
    public void saveKnowledgePoint(KnowledgePointSaveReq request) {
        KnowledgePoint entity = request.getId() == null ? new KnowledgePoint() : getById(request.getId());
        if (entity == null) {
            throw new LogicException(ErrorCodeConstants.KNOWLEDGE_POINT_NOT_EXIST);
        }
        KnowledgePointConvert.INSTANCE.updateKnowledgePoint(request, entity);
        entity.setParentId(entity.getParentId() == null ? 0L : entity.getParentId());
        validateParent(entity);
        entity.setSort(entity.getSort() == null ? 0 : entity.getSort());
        entity.setEnable(entity.getEnable() == null ? 1 : entity.getEnable());
        entity.setUpdateTime(LocalDateTime.now());
        if (request.getId() == null) {
            entity.setCreateTime(LocalDateTime.now());
        }
        if (!saveOrUpdate(entity)) {
            throw new LogicException(ErrorCodeConstants.KNOWLEDGE_POINT_SAVE_FAIL);
        }
    }

    /**
     * 校验业务数据。
     */
    private void validateParent(KnowledgePoint entity) {
        long duplicateCount = count(new LambdaQueryWrapper<KnowledgePoint>()
                .eq(KnowledgePoint::getParentId, entity.getParentId())
                .eq(KnowledgePoint::getPointName, entity.getPointName())
                .ne(entity.getId() != null, KnowledgePoint::getId, entity.getId()));
        if (duplicateCount > 0) {
            throw new LogicException(ErrorCodeConstants.KNOWLEDGE_POINT_NAME_EXIST);
        }
        if (entity.getParentId() == 0L) {
            return;
        }
        if (entity.getId() != null && entity.getId().equals(entity.getParentId())) {
            throw new LogicException(ErrorCodeConstants.KNOWLEDGE_POINT_PARENT_INVALID);
        }
        KnowledgePoint parent = getById(entity.getParentId());
        if (parent == null || !parent.getGrade().equals(entity.getGrade())
                || !parent.getSubject().equals(entity.getSubject())) {
            throw new LogicException(ErrorCodeConstants.KNOWLEDGE_POINT_PARENT_INVALID);
        }
        Long parentId = parent.getParentId();
        while (parentId != null && parentId != 0L) {
            if (entity.getId() != null && entity.getId().equals(parentId)) {
                throw new LogicException(ErrorCodeConstants.KNOWLEDGE_POINT_PARENT_INVALID);
            }
            KnowledgePoint ancestor = getById(parentId);
            if (ancestor == null) {
                break;
            }
            parentId = ancestor.getParentId();
        }
    }
}
