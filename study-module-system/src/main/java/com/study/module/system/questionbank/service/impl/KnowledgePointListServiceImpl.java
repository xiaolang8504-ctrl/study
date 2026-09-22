package com.study.module.system.questionbank.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.study.module.system.questionbank.convert.KnowledgePointConvert;
import com.study.module.system.questionbank.dto.response.KnowledgePointListResp;
import com.study.module.system.questionbank.entity.KnowledgePoint;
import com.study.module.system.questionbank.mapper.KnowledgePointMapper;
import com.study.module.system.questionbank.service.KnowledgePointListService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 知识点列表服务实现
 */
@Service
public class KnowledgePointListServiceImpl extends ServiceImpl<KnowledgePointMapper, KnowledgePoint>
        implements KnowledgePointListService {

    /**
     * 查询知识点列表
     */
    @Override
    public List<KnowledgePointListResp> knowledgePointList(String grade, String subject) {
        List<KnowledgePoint> knowledgePointList = list(new LambdaQueryWrapper<KnowledgePoint>()
                .eq(StringUtils.hasText(grade), KnowledgePoint::getGrade, grade)
                .eq(StringUtils.hasText(subject), KnowledgePoint::getSubject, subject)
                .orderByAsc(KnowledgePoint::getSort, KnowledgePoint::getId));
        return KnowledgePointConvert.INSTANCE.toKnowledgePointListResp(knowledgePointList);
    }
}
