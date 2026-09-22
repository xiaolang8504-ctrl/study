package com.study.module.system.msg.convert;

import com.study.module.system.msg.dto.response.MsgDetailResp;
import com.study.module.system.msg.dto.response.MsgPageListResp;
import com.study.module.system.msg.entity.Msg;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 消息转化类
 */
@Mapper
public interface MsgConvert {

    MsgConvert INSTANCE = Mappers.getMapper(MsgConvert.class);

    /**
     * 转化为消息分页列表响应类
     */
    List<MsgPageListResp> toMsgPageListResp(List<Msg> msgList);

    /**
     * 转化为消息详情响应类
     */
    MsgDetailResp toMsgDetailResp(Msg msg);
}
