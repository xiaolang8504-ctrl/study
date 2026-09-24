package com.study.module.system.questionbank.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.study.module.system.questionbank.dto.request.KnowledgePointPrerequisiteSaveReq;
import com.study.module.system.questionbank.dto.response.KnowledgePointPrerequisiteResp;
import com.study.module.system.questionbank.entity.KnowledgePointPrerequisite;

import java.util.List;

public interface KnowledgePointPrerequisiteService extends IService<KnowledgePointPrerequisite> {
    void saveKnowledgePointPrerequisite(KnowledgePointPrerequisiteSaveReq request);
    List<KnowledgePointPrerequisiteResp> knowledgePointPrerequisiteList(Long knowledgePointId, String subject);
}
