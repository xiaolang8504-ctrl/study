package com.study.module.system.log.convert;

import com.study.module.system.log.dto.response.OperateLogListResp;
import com.study.module.system.log.entity.OperateLog;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 操作日志转化类
 */
@Mapper
public interface OperateLogConvert {

    OperateLogConvert INSTANCE = Mappers.getMapper(OperateLogConvert.class);

    /**
     * 转化为操作日志分页列表响应类
     */
    List<OperateLogListResp> toOperateLogListResp(List<OperateLog> operateLogList);
}
