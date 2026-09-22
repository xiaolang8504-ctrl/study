package com.study.module.system.questionbank.service;

import com.study.module.system.questionbank.dto.response.KnowledgePointListResp;

import java.util.List;

/**
 * 知识点树服务。
 */
public interface KnowledgePointTreeService {
    List<KnowledgePointListResp> knowledgePointTree(String grade, String subject);
}
