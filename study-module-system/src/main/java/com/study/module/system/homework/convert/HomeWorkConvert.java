package com.study.module.system.homework.convert;

import com.study.module.system.homework.dto.request.CreateHomeWorkReq;
import com.study.module.system.homework.dto.request.UpdateHomeWorkReq;
import com.study.module.system.homework.dto.response.HomeWorkDetailResp;
import com.study.module.system.homework.dto.response.HomeWorkPageListResp;
import com.study.module.system.homework.entity.HomeWork;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 作业转换类
 */
@Mapper
public interface HomeWorkConvert {

    HomeWorkConvert INSTANCE = Mappers.getMapper(HomeWorkConvert.class);

    List<HomeWorkPageListResp> toHomeWorkPageListResp(List<HomeWork> homeWorkList);

    HomeWorkDetailResp toHomeWorkDetailResp(HomeWork homeWork);

    /**
     * 将新增作业请求转换为作业实体。
     */
    HomeWork toHomeWork(CreateHomeWorkReq request);

    /**
     * 将修改作业请求转换为作业实体。
     */
    HomeWork toHomeWork(UpdateHomeWorkReq request);
}
