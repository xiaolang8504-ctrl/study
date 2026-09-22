package com.study.module.system.questionbank.convert;

import com.study.module.system.questionbank.dto.request.KnowledgePointSaveReq;
import com.study.module.system.questionbank.dto.response.KnowledgePointListResp;
import com.study.module.system.questionbank.entity.KnowledgePoint;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;
import java.util.List;

/**
 * 知识点数据转换类
 */
@Mapper
public interface KnowledgePointConvert {

    KnowledgePointConvert INSTANCE = Mappers.getMapper(KnowledgePointConvert.class);

    /**
     * 将保存请求数据更新到知识点实体
     */
    void updateKnowledgePoint(KnowledgePointSaveReq request, @MappingTarget KnowledgePoint knowledgePoint);

    /**
     * 转换为知识点列表响应
     */
    List<KnowledgePointListResp> toKnowledgePointListResp(List<KnowledgePoint> knowledgePointList);
}
