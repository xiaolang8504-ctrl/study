package com.study.module.system.dict.convert;

import com.study.module.system.dict.dto.request.DictCreateReq;
import com.study.module.system.dict.dto.request.DictDataCreateReq;
import com.study.module.system.dict.dto.response.DictDataListResp;
import com.study.module.system.dict.dto.response.DictListResp;
import com.study.module.system.dict.entity.Dict;
import com.study.module.system.dict.entity.DictData;
import com.study.api.dto.response.DictLabelData;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 字典转化类
 */
@Mapper
public interface DictConvert {

    DictConvert INSTANCE = Mappers.getMapper(DictConvert.class);

    /**
     * 转化为字典标签
     */
    DictLabelData toDictLabelData(DictData dictData);

    /**
     * 转化为字典数据列表
     */
    List<DictDataListResp> toDictDataListResp(List<DictData> dictData);

    /**
     * 转化为字典数据
     */
    DictData toDictData(DictDataCreateReq request);

    /**
     * 转化为字典列表
     */
    List<DictListResp> toDictListResp(List<Dict> dictList);

    /**
     * 转化为字典
     */
    Dict toDict(DictCreateReq request);
}