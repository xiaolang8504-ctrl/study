package com.study.module.system.questionbank.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.study.module.system.questionbank.dto.request.KnowledgePointSaveReq;
import com.study.module.system.questionbank.entity.KnowledgePoint;

/**
 * 知识点保存服务
 */
public interface KnowledgePointSaveService extends IService<KnowledgePoint> {

    /**
     * 保存知识点
     */
    void saveKnowledgePoint(KnowledgePointSaveReq request);
}
