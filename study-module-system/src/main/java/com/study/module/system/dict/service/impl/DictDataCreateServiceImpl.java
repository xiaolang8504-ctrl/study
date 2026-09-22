package com.study.module.system.dict.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.module.system.dict.dto.request.DictDataCreateReq;
import com.study.module.system.dict.entity.DictData;
import com.study.module.system.dict.mapper.DictDataMapper;
import com.study.module.system.dict.service.DictDataCreateService;
import com.study.module.system.dict.service.DictDataService;
import com.study.module.system.dict.service.DictService;
import com.study.module.system.user.service.UserService;
import com.study.api.dto.response.DictLabelData;
import com.study.api.dto.response.UserData;
import com.study.common.core.constants.Enable;
import com.study.common.core.enums.ErrorCodeConstants;
import com.study.common.core.exception.LogicException;
import com.yunshang.budget.common.security.utils.AccountUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 字典数据创建服务
 */
@Service
public class DictDataCreateServiceImpl extends ServiceImpl<DictDataMapper, DictData> implements DictDataCreateService {

    @Autowired
    DictService dictService;

    @Autowired
    DictDataService dictDataService;

    @Autowired
    UserService userService;

    /**
     * 创建字典数据
     */
    @Override
    public void createDictData(DictDataCreateReq request) {
        // 校验字典数据类型
        dictService.checkDictType(request.getDictType());
        // 校验字典键值唯一
        dictDataService.existDictDataValue(request.getDictType(), request.getDictValue());
        // 字典数据名唯一
        DictLabelData dictLabelData = dictDataService.existDictDataLabel(request.getDictType(), request.getDictLabel());
        if (dictLabelData != null) {
            throw new LogicException(ErrorCodeConstants.DICT_DATA_LABEL_EXIST);
        }
        // 构建字典数据
        DictData dictData = dictDataService.buildDictData(request);
        // 创建人
        Long userId = AccountUtils.getUserId();
        UserData userData = userService.checkUserByProvider(userId);
        dictData.setCreateId(userData.getId());
        dictData.setCreateName(userData.getRealName());
        dictData.setIsEnable(Enable.ENABLE);
        // 获取字典数据最大排序
        dictData.setDictDataSort(dictMaxSort());
        dictData.setCreateTime(LocalDateTime.now());
        dictData.setUpdateTime(LocalDateTime.now());
        // 创建字典数据
        if (!this.save(dictData)) {
            throw new LogicException(ErrorCodeConstants.DICT_DATA_CREATE_FAIL);
        }
    }

    /**
     * 获取字典数据最大排序
     */
    private int dictMaxSort() {
        LambdaQueryWrapper<DictData> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(DictData::getDictDataSort)
                .last("limit 1");
        DictData dictDataLast = this.getOne(wrapper);
        return dictDataLast == null ? 1 : dictDataLast.getDictDataSort() + 1;
    }
}
