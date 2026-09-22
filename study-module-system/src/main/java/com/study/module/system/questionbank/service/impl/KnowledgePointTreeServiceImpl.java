package com.study.module.system.questionbank.service.impl;

import com.study.module.system.questionbank.dto.response.KnowledgePointListResp;
import com.study.module.system.questionbank.service.KnowledgePointListService;
import com.study.module.system.questionbank.service.KnowledgePointTreeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 知识点树服务实现。
 */
@Service
public class KnowledgePointTreeServiceImpl implements KnowledgePointTreeService {

    @Autowired
    KnowledgePointListService knowledgePointListService;

    @Override
    public List<KnowledgePointListResp> knowledgePointTree(String grade, String subject) {
        List<KnowledgePointListResp> points = knowledgePointListService.knowledgePointList(grade, subject);
        Map<Long, KnowledgePointListResp> pointMap = new LinkedHashMap<>();
        points.forEach(point -> {
            point.setChildren(new ArrayList<>());
            pointMap.put(point.getId(), point);
        });
        List<KnowledgePointListResp> roots = new ArrayList<>();
        points.forEach(point -> {
            KnowledgePointListResp parent = pointMap.get(point.getParentId());
            if (parent == null || point.getParentId() == null || point.getParentId() == 0L) {
                roots.add(point);
            } else {
                parent.getChildren().add(point);
            }
        });
        return roots;
    }
}
