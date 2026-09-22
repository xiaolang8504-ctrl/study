package com.study.module.system.dict.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.module.system.dict.convert.DictConvert;
import com.study.module.system.dict.dto.request.DictCreateReq;
import com.study.module.system.dict.entity.Dict;
import com.study.module.system.dict.mapper.DictMapper;
import com.study.module.system.dict.service.DictService;
import com.study.common.core.enums.ErrorCodeConstants;
import com.study.common.core.exception.LogicException;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * 字典服务
 */
@Service
public class DictServiceImpl extends ServiceImpl<DictMapper, Dict> implements DictService {

    /**
     * 检验字典
     */
    public Dict checkDict(Integer id) {
        Dict dict = this.getById(id);
        if (dict == null) {
            throw new LogicException(ErrorCodeConstants.DICT_NOT_EXIST);
        }
        return dict;
    }

    /**
     * 检验字典类型
     */
    @Override
    public Dict checkDictType(String dictType) {
        LambdaQueryWrapper<Dict> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Objects.nonNull(dictType), Dict::getDictType, dictType);
        Dict dict = this.getOne(queryWrapper);
        if (dict == null) {
            throw new LogicException(ErrorCodeConstants.DICT_TYPE_NOT_EXIST);
        }
        return dict;
    }

    /**
     * 构建字典信息入库
     */
    @Override
    public Dict buildDict(DictCreateReq request) {
        return DictConvert.INSTANCE.toDict(request);
    }

    /**
     * 校验字典是否存在
     */
    @Override
    public void existDict(String dictType) {
        LambdaQueryWrapper<Dict> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Objects.nonNull(dictType), Dict::getDictType, dictType);
        if (this.getOne(queryWrapper) != null) {
            throw new LogicException(ErrorCodeConstants.DICT_TYPE_EXIST);
        }
    }

    /**
     * 校验字典是否存在
     */
    @Override
    public void existDict(Integer id, String dictType) {
        LambdaQueryWrapper<Dict> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Objects.nonNull(dictType), Dict::getDictType, dictType);
        Dict type = this.getOne(queryWrapper);
        if (type != null && !Objects.equals(type.getId(), id)) {
            throw new LogicException(ErrorCodeConstants.DICT_TYPE_EXIST);
        }
    }

    /**
     * 根据字典类型获取字典
     */
    @Override
    public Dict dictTypeData(String dictType) {
        LambdaQueryWrapper<Dict> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Objects.nonNull(dictType), Dict::getDictType, dictType);
        Dict type = this.getOne(queryWrapper);
        if (type == null) {
            throw new LogicException(ErrorCodeConstants.DICT_NOT_EXIST);
        }
        return type;
    }
}
