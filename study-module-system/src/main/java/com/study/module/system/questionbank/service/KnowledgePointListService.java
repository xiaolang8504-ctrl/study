package com.study.module.system.questionbank.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.study.module.system.questionbank.dto.response.KnowledgePointListResp;
import com.study.module.system.questionbank.entity.KnowledgePoint;

import java.util.List;

/**
 * 知识点列表服务
 */
public interface KnowledgePointListService extends IService<KnowledgePoint> {

    /**
     * 查询知识点列表
     */
    List<KnowledgePointListResp> knowledgePointList(String grade, String subject);
}
