package com.study.module.system.questionbank.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.module.system.questionbank.entity.KnowledgePoint;
import com.study.module.system.questionbank.mapper.KnowledgePointMapper;
import com.study.module.system.questionbank.service.KnowledgePointService;
import org.springframework.stereotype.Service;

/**
 * 知识点公共服务实现
 */
@Service
public class KnowledgePointServiceImpl extends ServiceImpl<KnowledgePointMapper, KnowledgePoint>
        implements KnowledgePointService {
}
